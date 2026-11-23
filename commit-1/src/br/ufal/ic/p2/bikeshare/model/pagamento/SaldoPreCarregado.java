package br.ufal.ic.p2.bikeshare.model.pagamento;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.util.NumeroUtil;

import java.math.BigDecimal;

/**
 * Carteira pre-paga do usuario.
 *
 * <p>Unico metodo de pagamento que impoe restricoes: so autoriza iniciar uma corrida
 * com saldo positivo e so aceita a cobranca da devolucao se houver saldo suficiente,
 * caso em que o valor e debitado de fato.</p>
 */
public class SaldoPreCarregado extends MetodoPagamento {

    private static final long serialVersionUID = 1L;

    private BigDecimal saldo = NumeroUtil.zero();

    @Override
    public String getNome() {
        return "saldoPreCarregado";
    }

    /** Recarga do saldo; o valor informado ja foi validado como positivo. */
    public void recarregar(BigDecimal valor) {
        this.saldo = NumeroUtil.arredondar(this.saldo.add(valor));
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    @Override
    public void validarRetirada() throws BikeshareException {
        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException(Mensagens.SALDO_INSUFICIENTE);
        }
    }

    @Override
    public void cobrar(BigDecimal valor) throws BikeshareException {
        if (saldo.compareTo(valor) < 0) {
            throw new RegraDeNegocioException(Mensagens.SALDO_INSUFICIENTE);
        }
        this.saldo = NumeroUtil.arredondar(this.saldo.subtract(valor));
    }

    @Override
    public String getAtributo(String atributo) throws BikeshareException {
        if ("saldo".equals(atributo)) {
            return NumeroUtil.formatarMoeda(saldo);
        }
        return super.getAtributo(atributo);
    }
}
