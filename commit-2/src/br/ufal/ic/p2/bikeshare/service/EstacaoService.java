package br.ufal.ic.p2.bikeshare.service;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.EntidadeNaoEncontradaException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;
import br.ufal.ic.p2.bikeshare.model.bicicleta.Bicicleta;
import br.ufal.ic.p2.bikeshare.model.bicicleta.BicicletaConvencional;
import br.ufal.ic.p2.bikeshare.model.bicicleta.BicicletaEletrica;
import br.ufal.ic.p2.bikeshare.model.bicicleta.EstadoBicicleta;
import br.ufal.ic.p2.bikeshare.model.estacao.Estacao;
import br.ufal.ic.p2.bikeshare.util.TextoUtil;

/**
 * Regras de cadastro de estacoes, gestao do parque de bicicletas e consulta de
 * disponibilidade (US05 a US07).
 */
public class EstacaoService {

    private static final String TIPO_CONVENCIONAL = "convencional";
    private static final String TIPO_ELETRICA = "eletrica";

    private static final int BATERIA_MINIMA = 0;
    private static final int BATERIA_MAXIMA = 100;

    private final SistemaBikeshare sistema;

    public EstacaoService(SistemaBikeshare sistema) {
        this.sistema = sistema;
    }

    // =====================================================================
    // US05 - cadastro de estacao
    // =====================================================================

    public String criarEstacao(String nome, String endereco, String capacidade) throws BikeshareException {
        ValidadorEntrada.exigirPreenchido(nome, Mensagens.NOME_NULO);
        ValidadorEntrada.exigirPreenchido(endereco, Mensagens.ENDERECO_NULO);
        int vagas = ValidadorEntrada.exigirInteiro(capacidade,
                Mensagens.CAPACIDADE_NULA, Mensagens.CAPACIDADE_NAO_NUMERICA);
        ValidadorEntrada.exigirPositivo(vagas, Mensagens.CAPACIDADE_NAO_POSITIVA);

        Estacao estacao = new Estacao(sistema.gerarIdEstacao(), TextoUtil.limpar(nome),
                TextoUtil.limpar(endereco), vagas);
        sistema.adicionarEstacao(estacao);
        return estacao.getId();
    }

    // =====================================================================
    // US06 - gestao de bicicletas
    // =====================================================================

    /**
     * Cadastra uma bicicleta em uma estacao.
     *
     * <p>Os dados da bicicleta sao conferidos antes da vaga: uma estacao lotada so
     * reclama da falta de vaga depois que o tipo e a bateria informados se mostram
     * validos.</p>
     *
     * @param nivelBateria nivel informado, ou {@code null} quando nao foi informado.
     */
    public void adicionarBicicleta(String idEstacao, String idBicicleta, String tipo, String nivelBateria)
            throws BikeshareException {
        Estacao estacao = exigirEstacao(idEstacao);
        ValidadorEntrada.exigirPreenchido(idBicicleta, Mensagens.BICICLETA_ID_NULO);

        String codigo = TextoUtil.limpar(idBicicleta);
        if (sistema.buscarBicicleta(codigo) != null) {
            throw new RegraDeNegocioException(Mensagens.BICICLETA_JA_EXISTE);
        }

        Bicicleta bicicleta = montarBicicleta(codigo, estacao.getId(), tipo, nivelBateria);

        if (!estacao.temVagaLivre()) {
            throw new RegraDeNegocioException(Mensagens.ESTACAO_SEM_VAGAS);
        }
        sistema.adicionarBicicleta(bicicleta);
        estacao.acolher(bicicleta.getId());
    }

    /** Constroi a bicicleta do tipo pedido, validando a bateria conforme o tipo. */
    private Bicicleta montarBicicleta(String id, String idEstacao, String tipo, String nivelBateria)
            throws BikeshareException {
        if (TIPO_CONVENCIONAL.equals(tipo)) {
            if (nivelBateria != null) {
                throw new RegraDeNegocioException(Mensagens.BICICLETA_SEM_BATERIA);
            }
            return new BicicletaConvencional(id, idEstacao);
        }
        if (TIPO_ELETRICA.equals(tipo)) {
            int nivel = ValidadorEntrada.exigirInteiro(nivelBateria,
                    Mensagens.BATERIA_NULA, Mensagens.BATERIA_NAO_NUMERICA);
            if (nivel < BATERIA_MINIMA || nivel > BATERIA_MAXIMA) {
                throw new ValidacaoException(Mensagens.BATERIA_FORA_DA_FAIXA);
            }
            return new BicicletaEletrica(id, idEstacao, nivel);
        }
        throw new ValidacaoException(Mensagens.TIPO_INVALIDO);
    }

    /** Retira a bicicleta do parque; so bicicletas disponiveis podem ser removidas. */
    public void removerBicicleta(String idEstacao, String idBicicleta) throws BikeshareException {
        Estacao estacao = exigirEstacao(idEstacao);
        Bicicleta bicicleta = exigirBicicleta(idBicicleta);

        if (!bicicleta.estaEm(estacao.getId())) {
            throw new RegraDeNegocioException(Mensagens.BICICLETA_FORA_DA_ESTACAO);
        }
        if (bicicleta.getEstado() != EstadoBicicleta.DISPONIVEL) {
            throw new RegraDeNegocioException(Mensagens.BICICLETA_INDISPONIVEL);
        }
        estacao.liberar(bicicleta.getId());
        sistema.removerBicicleta(bicicleta.getId());
    }

    public String getAtributoBicicleta(String idBicicleta, String atributo) throws BikeshareException {
        return exigirBicicleta(idBicicleta).getAtributo(TextoUtil.limpar(atributo));
    }

    public String getEstadoBicicleta(String idBicicleta) throws BikeshareException {
        return exigirBicicleta(idBicicleta).getEstado().getDescricao();
    }

    // =====================================================================
    // US07 - consulta de disponibilidade
    // =====================================================================

    /** Quantas bicicletas da estacao estao efetivamente prontas para retirada. */
    public String getBicicletasDisponiveis(String idEstacao) throws BikeshareException {
        Estacao estacao = exigirEstacao(idEstacao);
        long disponiveis = estacao.getBicicletas().stream()
                .map(sistema::buscarBicicleta)
                .filter(bicicleta -> bicicleta != null && bicicleta.estaDisponivel())
                .count();
        return String.valueOf(disponiveis);
    }

    /** Quantas vagas da estacao estao livres para receber uma devolucao. */
    public String getVagasDisponiveis(String idEstacao) throws BikeshareException {
        return String.valueOf(exigirEstacao(idEstacao).getVagasLivres());
    }

    // =====================================================================
    // acesso compartilhado
    // =====================================================================

    public Estacao exigirEstacao(String idEstacao) throws BikeshareException {
        ValidadorEntrada.exigirPreenchido(idEstacao, Mensagens.ESTACAO_ID_NULO);
        Estacao estacao = sistema.buscarEstacao(idEstacao);
        if (estacao == null) {
            throw new EntidadeNaoEncontradaException(Mensagens.ESTACAO_NAO_EXISTE);
        }
        return estacao;
    }

    public Bicicleta exigirBicicleta(String idBicicleta) throws BikeshareException {
        ValidadorEntrada.exigirPreenchido(idBicicleta, Mensagens.BICICLETA_ID_NULO);
        Bicicleta bicicleta = sistema.buscarBicicleta(idBicicleta);
        if (bicicleta == null) {
            throw new EntidadeNaoEncontradaException(Mensagens.BICICLETA_NAO_EXISTE);
        }
        return bicicleta;
    }
}
