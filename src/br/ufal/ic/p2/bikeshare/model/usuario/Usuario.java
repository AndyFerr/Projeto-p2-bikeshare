package br.ufal.ic.p2.bikeshare.model.usuario;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;
import br.ufal.ic.p2.bikeshare.model.pagamento.Cartao;
import br.ufal.ic.p2.bikeshare.model.pagamento.MetodoPagamento;
import br.ufal.ic.p2.bikeshare.model.pagamento.Pix;
import br.ufal.ic.p2.bikeshare.model.pagamento.SaldoPreCarregado;
import br.ufal.ic.p2.bikeshare.model.pagamento.SemMetodoPagamento;
import br.ufal.ic.p2.bikeshare.model.penalidade.Penalidade;
import br.ufal.ic.p2.bikeshare.model.plano.Plano;
import br.ufal.ic.p2.bikeshare.util.NumeroUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pessoa cadastrada no sistema, identificada por um id gerado automaticamente.
 *
 * <p>Concentra o que vale para qualquer ciclista: credenciais, plano contratado,
 * metodo de pagamento, penalidades em aberto e historico de corridas. O que distingue
 * pessoa fisica de juridica &mdash; o documento e a razao social &mdash; fica nas
 * subclasses, que apenas estendem {@link #getAtributo(String)} com os atributos que
 * lhes pertencem.</p>
 */
public abstract class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private String nome;
    private String email;
    private String senha;

    private Plano plano;
    /** Mensalidade efetivamente contratada; {@code null} em planos sem mensalidade. */
    private BigDecimal mensalidadeContratada;

    private MetodoPagamento metodoPagamento = new SemMetodoPagamento();
    /** Carteira pre-paga do usuario, preservada mesmo quando outro metodo esta ativo. */
    private SaldoPreCarregado carteira;

    private final List<Penalidade> penalidades = new ArrayList<>();
    private final List<String> historicoAlugueis = new ArrayList<>();
    private String aluguelAtivo;

    protected Usuario(String id, String nome, String email, String senha,
                      Plano plano, BigDecimal mensalidadeContratada) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.plano = plano;
        this.mensalidadeContratada = mensalidadeContratada;
    }

    /** Nome do tipo do usuario, tal como aceito pelo comando de cadastro. */
    public abstract String getTipo();

    // ---- identidade e dados basicos ------------------------------------

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    // ---- plano ---------------------------------------------------------

    public Plano getPlano() {
        return plano;
    }

    /**
     * Troca o plano do usuario.
     *
     * <p>Reescolher o plano vigente nao mexe na mensalidade ja contratada; migrar para
     * outro plano passa a valer a mensalidade padrao do plano de destino.</p>
     */
    public void contratarPlano(Plano novoPlano) {
        if (plano != null && plano.getNome().equals(novoPlano.getNome())) {
            return;
        }
        this.plano = novoPlano;
        this.mensalidadeContratada = novoPlano.getMensalidade();
    }

    // ---- metodo de pagamento -------------------------------------------

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void usarCartao(String numero, String validade) {
        this.metodoPagamento = new Cartao(numero, validade);
    }

    public void usarPix(String chave) {
        this.metodoPagamento = new Pix(chave);
    }

    /** Ativa a carteira pre-paga, criando-a na primeira vez e preservando o saldo. */
    public void usarSaldoPreCarregado() {
        if (carteira == null) {
            carteira = new SaldoPreCarregado();
        }
        this.metodoPagamento = carteira;
    }

    /** Indica se a carteira pre-paga e o metodo de pagamento ativo. */
    public boolean usaSaldoPreCarregado() {
        return metodoPagamento instanceof SaldoPreCarregado;
    }

    public void recarregarSaldo(BigDecimal valor) {
        ((SaldoPreCarregado) metodoPagamento).recarregar(valor);
    }

    // ---- penalidades e inadimplencia -----------------------------------

    public void lancarPenalidade(Penalidade penalidade) {
        penalidades.add(penalidade);
    }

    public BigDecimal getTotalPendencias() {
        BigDecimal total = NumeroUtil.zero();
        for (Penalidade penalidade : penalidades) {
            total = total.add(penalidade.getValor());
        }
        return NumeroUtil.arredondar(total);
    }

    /** Um usuario com penalidade em aberto fica bloqueado para novas retiradas. */
    public boolean estaInadimplente() {
        return getTotalPendencias().compareTo(BigDecimal.ZERO) > 0;
    }

    public String getEstado() {
        return estaInadimplente() ? "inadimplente" : "ativo";
    }

    // ---- alugueis ------------------------------------------------------

    public String getAluguelAtivo() {
        return aluguelAtivo;
    }

    public boolean possuiAluguelEmAndamento() {
        return aluguelAtivo != null;
    }

    public void iniciarAluguel(String idAluguel) {
        this.aluguelAtivo = idAluguel;
        this.historicoAlugueis.add(idAluguel);
    }

    public void encerrarAluguel() {
        this.aluguelAtivo = null;
    }

    public List<String> getHistoricoAlugueis() {
        return Collections.unmodifiableList(historicoAlugueis);
    }

    // ---- consulta de atributos -----------------------------------------

    /**
     * Responde um atributo do usuario.
     *
     * <p>Atributos de pagamento sao delegados ao metodo de pagamento ativo e os
     * atributos de documento sao tratados nas subclasses; aqui eles so produzem a
     * mensagem que explica por que nao se aplicam a este usuario.</p>
     */
    public String getAtributo(String atributo) throws BikeshareException {
        return switch (atributo) {
            case "nome" -> nome;
            case "email" -> email;
            case "senha" -> senha;
            case "tipo" -> getTipo();
            case "estado" -> getEstado();
            case "plano" -> plano.getNome();
            case "mensalidade" -> {
                if (mensalidadeContratada == null) {
                    throw new RegraDeNegocioException(Mensagens.PLANO_SEM_MENSALIDADE);
                }
                yield NumeroUtil.formatarMoeda(mensalidadeContratada);
            }
            case "metodoPagamento" -> metodoPagamento.getNome();
            case "numeroCartao", "validadeCartao", "chavePix", "saldo" ->
                    metodoPagamento.getAtributo(atributo);
            case "aluguelAtivo" -> aluguelAtivo == null ? "nenhum" : aluguelAtivo;
            case "totalPendencias" -> NumeroUtil.formatarMoeda(getTotalPendencias());
            case "cpf" -> throw new RegraDeNegocioException(Mensagens.USUARIO_NAO_EH_PESSOA_FISICA);
            case "cnpj", "razaoSocial" ->
                    throw new RegraDeNegocioException(Mensagens.USUARIO_NAO_EH_PESSOA_JURIDICA);
            default -> throw new ValidacaoException(Mensagens.ATRIBUTO_NAO_EXISTE);
        };
    }
}
