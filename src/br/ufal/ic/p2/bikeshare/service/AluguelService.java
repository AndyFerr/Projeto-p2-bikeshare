package br.ufal.ic.p2.bikeshare.service;

import br.ufal.ic.p2.bikeshare.config.ParametrosSistema;
import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.EntidadeNaoEncontradaException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;
import br.ufal.ic.p2.bikeshare.model.aluguel.Aluguel;
import br.ufal.ic.p2.bikeshare.model.aluguel.EstadoAluguel;
import br.ufal.ic.p2.bikeshare.model.bicicleta.Bicicleta;
import br.ufal.ic.p2.bikeshare.model.bicicleta.EstadoBicicleta;
import br.ufal.ic.p2.bikeshare.model.estacao.Estacao;
import br.ufal.ic.p2.bikeshare.model.penalidade.PenalidadeAtraso;
import br.ufal.ic.p2.bikeshare.model.usuario.Usuario;
import br.ufal.ic.p2.bikeshare.util.DataUtil;
import br.ufal.ic.p2.bikeshare.util.NumeroUtil;
import br.ufal.ic.p2.bikeshare.util.TextoUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Regras de retirada, devolucao e historico de corridas (US08 a US10).
 *
 * <p>E aqui que as hierarquias do dominio se encontram: o plano do usuario diz quanto
 * custa o tempo, a bicicleta diz quanto custa o seu tipo e o metodo de pagamento diz
 * se a cobranca pode ser feita &mdash; nenhum desses tres calculos aparece neste
 * servico, que apenas os orquestra.</p>
 */
public class AluguelService {

    private static final String MOTIVO_ATRASO = "Devolucao apos o tempo maximo de uso.";

    private final SistemaBikeshare sistema;
    private final UsuarioService usuarios;
    private final EstacaoService estacoes;

    public AluguelService(SistemaBikeshare sistema, UsuarioService usuarios, EstacaoService estacoes) {
        this.sistema = sistema;
        this.usuarios = usuarios;
        this.estacoes = estacoes;
    }

    // =====================================================================
    // US08 - retirada
    // =====================================================================

    /**
     * Inicia uma corrida.
     *
     * <p>As pre-condicoes sao conferidas em ordem fixa: primeiro a existencia das
     * entidades e a data, depois o bloqueio por inadimplencia &mdash; que tem
     * precedencia sobre qualquer outra recusa &mdash;, em seguida a situacao da
     * bicicleta e, por fim, a capacidade de pagamento.</p>
     */
    public String retirar(String idUsuario, String idEstacao, String idBicicleta, String data)
            throws BikeshareException {
        Usuario usuario = usuarios.exigirUsuario(idUsuario);
        Estacao estacao = estacoes.exigirEstacao(idEstacao);
        Bicicleta bicicleta = estacoes.exigirBicicleta(idBicicleta);
        LocalDateTime inicio = exigirDataHora(data);

        if (usuario.estaInadimplente()) {
            throw new RegraDeNegocioException(Mensagens.USUARIO_INADIMPLENTE);
        }
        if (usuario.possuiAluguelEmAndamento()) {
            throw new RegraDeNegocioException(Mensagens.USUARIO_JA_POSSUI_ALUGUEL);
        }
        if (!bicicleta.estaEm(estacao.getId())) {
            throw new RegraDeNegocioException(Mensagens.BICICLETA_FORA_DA_ESTACAO);
        }
        if (bicicleta.getEstado() != EstadoBicicleta.DISPONIVEL) {
            throw new RegraDeNegocioException(Mensagens.BICICLETA_INDISPONIVEL);
        }
        bicicleta.validarRetirada();
        usuario.getMetodoPagamento().validarRetirada();

        Aluguel aluguel = new Aluguel(sistema.gerarIdAluguel(), usuario.getId(),
                bicicleta.getId(), estacao.getId(), inicio);
        sistema.adicionarAluguel(aluguel);

        estacao.liberar(bicicleta.getId());
        bicicleta.retirar();
        usuario.iniciarAluguel(aluguel.getId());
        return aluguel.getId();
    }

    // =====================================================================
    // US09 - devolucao
    // =====================================================================

    /**
     * Encerra uma corrida na estacao informada.
     *
     * <p>Nada e alterado antes de a cobranca ser aceita: se o metodo de pagamento
     * recusar o valor apurado, o aluguel continua em andamento exatamente como
     * estava.</p>
     */
    public void devolver(String idAluguel, String idEstacao, String data) throws BikeshareException {
        Aluguel aluguel = exigirAluguel(idAluguel);
        Estacao destino = estacoes.exigirEstacao(idEstacao);
        LocalDateTime fim = exigirDataHora(data);

        if (!aluguel.estaEmAndamento()) {
            throw new RegraDeNegocioException(Mensagens.ALUGUEL_NAO_ESTA_EM_ANDAMENTO);
        }
        if (fim.isBefore(aluguel.getInicio())) {
            throw new ValidacaoException(Mensagens.DEVOLUCAO_ANTES_DA_RETIRADA);
        }
        if (!destino.temVagaLivre()) {
            throw new RegraDeNegocioException(Mensagens.ESTACAO_CHEIA);
        }

        Usuario usuario = sistema.buscarUsuario(aluguel.getIdUsuario());
        Bicicleta bicicleta = sistema.buscarBicicleta(aluguel.getIdBicicleta());
        long minutos = aluguel.duracaoEmMinutos(fim);

        EstadoAluguel desfecho = apurarDesfecho(aluguel, destino, minutos);
        BigDecimal valor = desfecho == EstadoAluguel.FINALIZADO
                ? calcularValor(usuario, bicicleta, minutos)
                : NumeroUtil.zero();

        if (desfecho == EstadoAluguel.FINALIZADO && usuario != null) {
            usuario.getMetodoPagamento().cobrar(valor);
        }

        aluguel.encerrar(desfecho, destino.getId(), fim, valor);
        destino.acolher(bicicleta.getId());

        if (desfecho == EstadoAluguel.EXPIRADO) {
            bicicleta.recolherParaManutencaoEm(destino.getId(), MOTIVO_ATRASO);
            if (usuario != null) {
                usuario.lancarPenalidade(new PenalidadeAtraso(MOTIVO_ATRASO));
            }
        } else {
            bicicleta.devolverEm(destino.getId());
        }

        if (usuario != null) {
            usuario.encerrarAluguel();
        }
    }

    /**
     * Decide o desfecho da corrida: ultrapassar o tempo maximo expira o aluguel;
     * devolver na estacao de origem dentro da carencia o cancela; o resto e corrida
     * normal.
     */
    private EstadoAluguel apurarDesfecho(Aluguel aluguel, Estacao destino, long minutos) {
        if (minutos > ParametrosSistema.tempoMaximoMinutos()) {
            return EstadoAluguel.EXPIRADO;
        }
        boolean mesmaEstacao = destino.getId().equals(aluguel.getIdEstacaoOrigem());
        if (mesmaEstacao && minutos <= ParametrosSistema.carenciaMinutos()) {
            return EstadoAluguel.CANCELADO;
        }
        return EstadoAluguel.FINALIZADO;
    }

    /**
     * Valor da corrida: o que o plano cobra pelo tempo mais o adicional do tipo de
     * bicicleta. Os dois parcelas sao calculadas pelos proprios objetos.
     */
    private BigDecimal calcularValor(Usuario usuario, Bicicleta bicicleta, long minutos) {
        BigDecimal porTempo = usuario == null
                ? NumeroUtil.zero()
                : usuario.getPlano().calcularValorPorTempo(minutos);
        return NumeroUtil.arredondar(porTempo.add(bicicleta.calcularAdicionalPorTempo(minutos)));
    }

    // =====================================================================
    // US10 - historico e consulta de aluguel
    // =====================================================================

    public String getAtributoAluguel(String idAluguel, String atributo) throws BikeshareException {
        return exigirAluguel(idAluguel).getAtributo(TextoUtil.limpar(atributo));
    }

    /**
     * Resumo do historico do usuario.
     *
     * @param atributo {@code quantidade} ou {@code totalGasto}; {@code null} equivale
     *                 a quantidade.
     */
    public String getHistorico(String idUsuario, String atributo) throws BikeshareException {
        Usuario usuario = usuarios.exigirUsuario(idUsuario);
        String escolhido = atributo == null ? "quantidade" : TextoUtil.limpar(atributo);
        return switch (escolhido) {
            case "quantidade" -> String.valueOf(usuario.getHistoricoAlugueis().size());
            case "totalGasto" -> NumeroUtil.formatarMoeda(somarGastos(usuario));
            default -> throw new ValidacaoException(Mensagens.ATRIBUTO_NAO_EXISTE);
        };
    }

    /** Soma o que o usuario pagou; corridas canceladas e expiradas valem zero. */
    private BigDecimal somarGastos(Usuario usuario) {
        BigDecimal total = NumeroUtil.zero();
        for (String idAluguel : usuario.getHistoricoAlugueis()) {
            Aluguel aluguel = sistema.buscarAluguel(idAluguel);
            if (aluguel != null) {
                total = total.add(aluguel.getValorCobrado());
            }
        }
        return NumeroUtil.arredondar(total);
    }

    // =====================================================================
    // acesso compartilhado
    // =====================================================================

    public Aluguel exigirAluguel(String idAluguel) throws BikeshareException {
        ValidadorEntrada.exigirPreenchido(idAluguel, Mensagens.ALUGUEL_ID_NULO);
        Aluguel aluguel = sistema.buscarAluguel(idAluguel);
        if (aluguel == null) {
            throw new EntidadeNaoEncontradaException(Mensagens.ALUGUEL_NAO_EXISTE);
        }
        return aluguel;
    }

    /** Converte uma data e hora obrigatoria, com as mensagens padrao do sistema. */
    public static LocalDateTime exigirDataHora(String data) throws BikeshareException {
        ValidadorEntrada.exigirPreenchido(data, Mensagens.DATA_NULA);
        LocalDateTime dataHora = DataUtil.paraDataHora(data);
        if (dataHora == null) {
            throw new ValidacaoException(Mensagens.DATA_INVALIDA);
        }
        return dataHora;
    }
}
