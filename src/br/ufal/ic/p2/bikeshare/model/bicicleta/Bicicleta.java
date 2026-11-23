package br.ufal.ic.p2.bikeshare.model.bicicleta;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;
import br.ufal.ic.p2.bikeshare.util.NumeroUtil;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Veiculo compartilhado, identificado pelo codigo informado no cadastro.
 *
 * <p>Enquanto nao esta em uso, a bicicleta pertence a uma estacao e ocupa uma vaga
 * nela &mdash; inclusive quando esta em manutencao. Os dois tipos suportados diferem
 * em tres pontos, todos resolvidos por polimorfismo: a cobranca adicional por minuto,
 * a exigencia de bateria para a retirada e os atributos que sabem responder.</p>
 */
public abstract class Bicicleta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private EstadoBicicleta estado = EstadoBicicleta.DISPONIVEL;

    /** Estacao onde a bicicleta se encontra; {@code null} enquanto esta em uso. */
    private String estacaoAtual;

    protected Bicicleta(String id, String estacaoAtual) {
        this.id = id;
        this.estacaoAtual = estacaoAtual;
    }

    /** Nome do tipo da bicicleta, tal como aceito pelo comando de cadastro. */
    public abstract String getTipo();

    /**
     * Adicional cobrado pelo tipo de bicicleta ao longo da corrida.
     *
     * <p>Convencionais nao cobram adicional; o padrao e zero e so a eletrica
     * sobrescreve.</p>
     */
    public BigDecimal calcularAdicionalPorTempo(long minutos) {
        return NumeroUtil.zero();
    }

    /**
     * Verifica se o tipo da bicicleta permite inicia-la agora.
     *
     * <p>Serve para as restricoes proprias do tipo, como o nivel minimo de bateria;
     * convencionais nao impoem nenhuma.</p>
     */
    public void validarRetirada() throws BikeshareException {
        // Bicicleta convencional nao tem restricao propria de retirada.
    }

    /** Indica se o tipo da bicicleta a considera pronta para uso neste momento. */
    protected boolean prontaParaUso() {
        return true;
    }

    public String getId() {
        return id;
    }

    public EstadoBicicleta getEstado() {
        return estado;
    }

    public String getEstacaoAtual() {
        return estacaoAtual;
    }

    public boolean estaEm(String idEstacao) {
        return estacaoAtual != null && estacaoAtual.equals(idEstacao);
    }

    /**
     * Indica se a bicicleta conta como disponivel para retirada: precisa estar em bom
     * estado e atender as exigencias do seu tipo.
     */
    public boolean estaDisponivel() {
        return estado == EstadoBicicleta.DISPONIVEL && prontaParaUso();
    }

    // ---- transicoes de estado ------------------------------------------

    /** Retirada: a bicicleta sai da estacao e passa a ser usada. */
    public void retirar() {
        this.estado = EstadoBicicleta.EM_USO;
        this.estacaoAtual = null;
    }

    /** Devolucao: a bicicleta chega a estacao informada, pronta para uso. */
    public void devolverEm(String idEstacao) {
        this.estado = EstadoBicicleta.DISPONIVEL;
        this.estacaoAtual = idEstacao;
    }

    /** Devolucao com problema: a bicicleta chega a estacao, mas vai para manutencao. */
    public void recolherParaManutencaoEm(String idEstacao) {
        this.estado = EstadoBicicleta.EM_MANUTENCAO;
        this.estacaoAtual = idEstacao;
    }

    public void entrarEmManutencao() {
        this.estado = EstadoBicicleta.EM_MANUTENCAO;
    }

    public void concluirManutencao() {
        this.estado = EstadoBicicleta.DISPONIVEL;
    }

    // ---- consulta de atributos -----------------------------------------

    public String getAtributo(String atributo) throws BikeshareException {
        return switch (atributo) {
            case "tipo" -> getTipo();
            case "estado" -> estado.getDescricao();
            case "estacao" -> estacaoAtual == null ? "nenhuma" : estacaoAtual;
            case "nivelBateria" -> throw new RegraDeNegocioException(Mensagens.BICICLETA_SEM_BATERIA);
            default -> throw new ValidacaoException(Mensagens.ATRIBUTO_NAO_EXISTE);
        };
    }
}
