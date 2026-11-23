package br.ufal.ic.p2.bikeshare.service;

import br.ufal.ic.p2.bikeshare.config.ParametrosSistema;
import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.EntidadeNaoEncontradaException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;
import br.ufal.ic.p2.bikeshare.model.plano.Plano;
import br.ufal.ic.p2.bikeshare.model.usuario.PessoaFisica;
import br.ufal.ic.p2.bikeshare.model.usuario.PessoaJuridica;
import br.ufal.ic.p2.bikeshare.model.usuario.Usuario;
import br.ufal.ic.p2.bikeshare.util.TextoUtil;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Regras de cadastro, consulta, alteracao e remocao de usuarios (US01 a US04).
 */
public class UsuarioService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+$");
    private static final Pattern VALIDADE_CARTAO = Pattern.compile("^(0[1-9]|1[0-2])/\\d{4}$");

    private static final String TIPO_PESSOA_FISICA = "pessoaFisica";
    private static final String TIPO_PESSOA_JURIDICA = "pessoaJuridica";

    private final SistemaBikeshare sistema;

    public UsuarioService(SistemaBikeshare sistema) {
        this.sistema = sistema;
    }

    // =====================================================================
    // US01 - cadastro
    // =====================================================================

    /** Cadastro de pessoa fisica; {@code plano} nulo significa plano nao informado. */
    public String criarPessoaFisica(String nome, String email, String senha, String tipo,
                                    String cpf, String plano, String mensalidade)
            throws BikeshareException {
        validarDadosComuns(nome, email, senha);
        exigirTipo(tipo, TIPO_PESSOA_FISICA);
        ValidadorEntrada.exigirPreenchido(cpf, Mensagens.CPF_NULO);

        Plano planoContratado = resolverPlano(plano);
        BigDecimal mensalidadeContratada = resolverMensalidade(planoContratado, plano, mensalidade);
        exigirEmailInedito(email, null);

        Usuario usuario = new PessoaFisica(sistema.gerarIdUsuario(), TextoUtil.limpar(nome),
                TextoUtil.limpar(email), senha, planoContratado, mensalidadeContratada,
                TextoUtil.limpar(cpf));
        sistema.adicionarUsuario(usuario);
        return usuario.getId();
    }

    /** Cadastro de pessoa juridica; {@code plano} nulo significa plano nao informado. */
    public String criarPessoaJuridica(String nome, String email, String senha, String tipo,
                                      String cnpj, String razaoSocial, String plano, String mensalidade)
            throws BikeshareException {
        validarDadosComuns(nome, email, senha);
        exigirTipo(tipo, TIPO_PESSOA_JURIDICA);
        ValidadorEntrada.exigirPreenchido(cnpj, Mensagens.CNPJ_NULO);
        ValidadorEntrada.exigirPreenchido(razaoSocial, Mensagens.RAZAO_SOCIAL_NULA);

        Plano planoContratado = resolverPlano(plano);
        BigDecimal mensalidadeContratada = resolverMensalidade(planoContratado, plano, mensalidade);
        exigirEmailInedito(email, null);

        Usuario usuario = new PessoaJuridica(sistema.gerarIdUsuario(), TextoUtil.limpar(nome),
                TextoUtil.limpar(email), senha, planoContratado, mensalidadeContratada,
                TextoUtil.limpar(cnpj), TextoUtil.limpar(razaoSocial));
        sistema.adicionarUsuario(usuario);
        return usuario.getId();
    }

    private void validarDadosComuns(String nome, String email, String senha) throws BikeshareException {
        ValidadorEntrada.exigirPreenchido(nome, Mensagens.NOME_NULO);
        ValidadorEntrada.exigirPreenchido(email, Mensagens.EMAIL_NULO);
        if (!EMAIL.matcher(TextoUtil.limpar(email)).matches()) {
            throw new ValidacaoException(Mensagens.EMAIL_INVALIDO);
        }
        ValidadorEntrada.exigirPreenchido(senha, Mensagens.SENHA_NULA);
    }

    /** O tipo precisa casar exatamente com o esperado para a assinatura usada. */
    private void exigirTipo(String tipo, String esperado) throws BikeshareException {
        if (!esperado.equals(tipo)) {
            throw new ValidacaoException(Mensagens.TIPO_INVALIDO);
        }
    }

    private Plano resolverPlano(String nomePlano) throws BikeshareException {
        if (nomePlano == null) {
            return sistema.buscarPlano(ParametrosSistema.planoPadrao());
        }
        Plano plano = sistema.buscarPlano(nomePlano);
        if (plano == null) {
            throw new ValidacaoException(Mensagens.PLANO_INVALIDO);
        }
        return plano;
    }

    /**
     * Apura a mensalidade a ser contratada: planos com mensalidade exigem o valor,
     * planos sem mensalidade recusam qualquer valor informado.
     */
    private BigDecimal resolverMensalidade(Plano plano, String nomePlanoInformado, String mensalidade)
            throws BikeshareException {
        if (nomePlanoInformado == null) {
            return plano.getMensalidade();
        }
        if (!plano.possuiMensalidade()) {
            if (!TextoUtil.vazio(mensalidade)) {
                throw new ValidacaoException(Mensagens.PLANO_SEM_MENSALIDADE);
            }
            return null;
        }
        BigDecimal valor = ValidadorEntrada.exigirDecimal(mensalidade,
                Mensagens.MENSALIDADE_NULA, Mensagens.MENSALIDADE_NAO_NUMERICA);
        ValidadorEntrada.exigirNaoNegativo(valor, Mensagens.MENSALIDADE_NEGATIVA);
        return valor;
    }

    /** Garante a unicidade do email, ignorando caixa e desconsiderando o proprio dono. */
    private void exigirEmailInedito(String email, Usuario proprioDono) throws BikeshareException {
        Usuario existente = sistema.buscarUsuarioPorEmail(email);
        if (existente != null && existente != proprioDono) {
            throw new RegraDeNegocioException(Mensagens.EMAIL_JA_CADASTRADO);
        }
    }

    // =====================================================================
    // US02 - consulta
    // =====================================================================

    public String getAtributo(String idUsuario, String atributo) throws BikeshareException {
        return exigirUsuario(idUsuario).getAtributo(TextoUtil.limpar(atributo));
    }

    public String getUsuarioPorEmail(String email) throws BikeshareException {
        ValidadorEntrada.exigirPreenchido(email, Mensagens.EMAIL_NULO);
        Usuario usuario = sistema.buscarUsuarioPorEmail(email);
        if (usuario == null) {
            throw new EntidadeNaoEncontradaException(Mensagens.USUARIO_NAO_EXISTE);
        }
        return usuario.getId();
    }

    /**
     * Recupera um usuario pelo identificador, aplicando a ordem de validacao comum a
     * todos os comandos: primeiro a identificacao, depois a existencia.
     */
    public Usuario exigirUsuario(String idUsuario) throws BikeshareException {
        ValidadorEntrada.exigirPreenchido(idUsuario, Mensagens.USUARIO_ID_NULO);
        Usuario usuario = sistema.buscarUsuario(idUsuario);
        if (usuario == null) {
            throw new EntidadeNaoEncontradaException(Mensagens.USUARIO_NAO_EXISTE);
        }
        return usuario;
    }

    // =====================================================================
    // US03 - alteracao
    // =====================================================================

    /** Alteracao de um atributo simples ou escolha da carteira pre-paga. */
    public void alterar(String idUsuario, String atributo, String valor) throws BikeshareException {
        Usuario usuario = exigirUsuario(idUsuario);
        switch (TextoUtil.limpar(atributo)) {
            case "nome" -> {
                ValidadorEntrada.exigirPreenchido(valor, Mensagens.NOME_NULO);
                usuario.setNome(TextoUtil.limpar(valor));
            }
            case "email" -> {
                ValidadorEntrada.exigirPreenchido(valor, Mensagens.EMAIL_NULO);
                if (!EMAIL.matcher(TextoUtil.limpar(valor)).matches()) {
                    throw new ValidacaoException(Mensagens.EMAIL_INVALIDO);
                }
                exigirEmailInedito(valor, usuario);
                usuario.setEmail(TextoUtil.limpar(valor));
            }
            case "senha" -> {
                ValidadorEntrada.exigirPreenchido(valor, Mensagens.SENHA_NULA);
                usuario.setSenha(valor);
            }
            case "plano" -> {
                Plano plano = sistema.buscarPlano(valor);
                if (TextoUtil.vazio(valor) || plano == null) {
                    throw new ValidacaoException(Mensagens.PLANO_INVALIDO);
                }
                usuario.contratarPlano(plano);
            }
            case "metodoPagamento" -> {
                if (!"saldoPreCarregado".equals(valor)) {
                    throw new ValidacaoException(Mensagens.METODO_PAGAMENTO_INVALIDO);
                }
                usuario.usarSaldoPreCarregado();
            }
            case "saldo" -> recarregarSaldo(usuario, valor);
            default -> throw new ValidacaoException(Mensagens.ATRIBUTO_NAO_ALTERAVEL);
        }
    }

    /** Cadastro do Pix como metodo de pagamento. */
    public void alterarMetodoPix(String idUsuario, String metodo, String chave) throws BikeshareException {
        Usuario usuario = exigirMetodoPagamento(idUsuario, metodo, "pix");
        ValidadorEntrada.exigirPreenchido(chave, Mensagens.PIX_CHAVE_NULA);
        usuario.usarPix(TextoUtil.limpar(chave));
    }

    /** Cadastro do cartao como metodo de pagamento. */
    public void alterarMetodoCartao(String idUsuario, String metodo, String numero, String validade)
            throws BikeshareException {
        Usuario usuario = exigirMetodoPagamento(idUsuario, metodo, "cartao");
        ValidadorEntrada.exigirPreenchido(numero, Mensagens.CARTAO_NUMERO_NULO);
        ValidadorEntrada.exigirPreenchido(validade, Mensagens.CARTAO_VALIDADE_NULA);
        if (!VALIDADE_CARTAO.matcher(TextoUtil.limpar(validade)).matches()) {
            throw new ValidacaoException(Mensagens.CARTAO_VALIDADE_INVALIDA);
        }
        usuario.usarCartao(TextoUtil.limpar(numero), TextoUtil.limpar(validade));
    }

    /**
     * Valida a parte comum das duas assinaturas de cadastro de metodo de pagamento: o
     * atributo precisa ser {@code metodoPagamento} e o metodo informado precisa ser o
     * compativel com a quantidade de parametros recebida.
     */
    private Usuario exigirMetodoPagamento(String idUsuario, String metodoInformado, String metodoEsperado)
            throws BikeshareException {
        Usuario usuario = exigirUsuario(idUsuario);
        if (!metodoEsperado.equals(metodoInformado)) {
            throw new ValidacaoException(Mensagens.METODO_PAGAMENTO_INVALIDO);
        }
        return usuario;
    }

    private void recarregarSaldo(Usuario usuario, String valor) throws BikeshareException {
        if (!usuario.usaSaldoPreCarregado()) {
            throw new RegraDeNegocioException(Mensagens.NAO_USA_SALDO);
        }
        BigDecimal recarga = ValidadorEntrada.exigirDecimal(valor,
                Mensagens.VALOR_NULO, Mensagens.VALOR_NAO_NUMERICO);
        ValidadorEntrada.exigirPositivo(recarga, Mensagens.VALOR_NAO_POSITIVO);
        usuario.recarregarSaldo(recarga);
    }

    // =====================================================================
    // US04 - remocao
    // =====================================================================

    public void remover(String idUsuario) throws BikeshareException {
        Usuario usuario = exigirUsuario(idUsuario);
        if (usuario.possuiAluguelEmAndamento()) {
            throw new RegraDeNegocioException(Mensagens.USUARIO_COM_ALUGUEL_EM_ANDAMENTO);
        }
        sistema.removerUsuario(usuario.getId());
    }
}
