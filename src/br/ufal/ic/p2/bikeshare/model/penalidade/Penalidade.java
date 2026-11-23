package br.ufal.ic.p2.bikeshare.model.penalidade;

import br.ufal.ic.p2.bikeshare.util.NumeroUtil;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Cobranca adicional lancada sobre um usuario.
 *
 * <p>Enquanto houver penalidade em aberto o usuario fica inadimplente e nao pode
 * iniciar novas corridas. As subclasses se distinguem pela origem do valor: a de
 * atraso e automatica e tem valor fixo de configuracao, as demais sao lancadas pelo
 * operador com o valor que ele apurar.</p>
 */
public abstract class Penalidade implements Serializable {

    private static final long serialVersionUID = 1L;

    private final BigDecimal valor;
    private final String motivo;

    protected Penalidade(BigDecimal valor, String motivo) {
        this.valor = NumeroUtil.arredondar(valor);
        this.motivo = motivo;
    }

    /** Nome do tipo da penalidade, tal como aceito pelo comando de lancamento. */
    public abstract String getTipo();

    public BigDecimal getValor() {
        return valor;
    }

    public String getMotivo() {
        return motivo;
    }
}
