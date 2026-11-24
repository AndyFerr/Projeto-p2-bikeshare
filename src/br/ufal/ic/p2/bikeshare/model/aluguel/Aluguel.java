package br.ufal.ic.p2.bikeshare.model.aluguel;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;
import br.ufal.ic.p2.bikeshare.util.DataUtil;
import br.ufal.ic.p2.bikeshare.util.NumeroUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Periodo de uso de uma bicicleta por um usuario, da retirada ate a devolucao.
 *
 * <p>Enquanto a corrida esta em andamento nao existe estacao de destino, horario de
 * fim nem valor cobrado: perguntar por eles e um erro de negocio, e nao um atributo
 * inexistente. Esses tres dados so passam a existir no desfecho da corrida.</p>
 */
public class Aluguel implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String idUsuario;
    private final String idBicicleta;
    private final String idEstacaoOrigem;
    private final LocalDateTime inicio;

    private EstadoAluguel estado = EstadoAluguel.EM_ANDAMENTO;
    private String idEstacaoDestino;
    private LocalDateTime fim;
    private BigDecimal valorCobrado;

    public Aluguel(String id, String idUsuario, String idBicicleta, String idEstacaoOrigem,
                   LocalDateTime inicio) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.idBicicleta = idBicicleta;
        this.idEstacaoOrigem = idEstacaoOrigem;
        this.inicio = inicio;
    }

    public String getId() {
        return id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getIdBicicleta() {
        return idBicicleta;
    }

    public String getIdEstacaoOrigem() {
        return idEstacaoOrigem;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public EstadoAluguel getEstado() {
        return estado;
    }

    public boolean estaEmAndamento() {
        return estado == EstadoAluguel.EM_ANDAMENTO;
    }

    /** Valor efetivamente cobrado; zero enquanto a corrida nao foi encerrada. */
    public BigDecimal getValorCobrado() {
        return valorCobrado == null ? NumeroUtil.zero() : valorCobrado;
    }

    /** Duracao da corrida em minutos cheios, dado um horario de devolucao. */
    public long duracaoEmMinutos(LocalDateTime devolucao) {
        return Duration.between(inicio, devolucao).toMinutes();
    }

    /** Registra o desfecho da corrida: estado, destino, horario e valor apurado. */
    public void encerrar(EstadoAluguel desfecho, String idEstacaoDestino,
                         LocalDateTime fim, BigDecimal valorCobrado) {
        this.estado = desfecho;
        this.idEstacaoDestino = idEstacaoDestino;
        this.fim = fim;
        this.valorCobrado = NumeroUtil.arredondar(valorCobrado);
    }

    public String getAtributo(String atributo) throws BikeshareException {
        return switch (atributo) {
            case "estado" -> estado.getDescricao();
            case "usuario" -> idUsuario;
            case "bicicleta" -> idBicicleta;
            case "estacaoOrigem" -> idEstacaoOrigem;
            case "inicio" -> DataUtil.formatar(inicio);
            case "estacaoDestino" -> exigirEncerrado(idEstacaoDestino);
            case "fim" -> exigirEncerrado(fim == null ? null : DataUtil.formatar(fim));
            case "valorCobrado" -> exigirEncerrado(NumeroUtil.formatarMoeda(getValorCobrado()));
            default -> throw new ValidacaoException(Mensagens.ATRIBUTO_NAO_EXISTE);
        };
    }

    /** Atributos do desfecho so podem ser consultados depois da devolucao. */
    private String exigirEncerrado(String valor) throws BikeshareException {
        if (estaEmAndamento()) {
            throw new RegraDeNegocioException(Mensagens.ALUGUEL_EM_ANDAMENTO);
        }
        return valor;
    }
}
