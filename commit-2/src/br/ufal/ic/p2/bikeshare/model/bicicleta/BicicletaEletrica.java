package br.ufal.ic.p2.bikeshare.model.bicicleta;

import br.ufal.ic.p2.bikeshare.config.ParametrosSistema;
import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.util.NumeroUtil;

import java.math.BigDecimal;

/**
 * Bicicleta eletrica: cobra um adicional por minuto de corrida e so pode ser retirada
 * com bateria acima do nivel minimo configurado.
 *
 * <p>As duas regras sao independentes do estado operacional: uma eletrica com bateria
 * baixa continua ocupando a vaga da estacao, mas nao e contada como disponivel.</p>
 */
public class BicicletaEletrica extends Bicicleta {

    private static final long serialVersionUID = 1L;

    private final int nivelBateria;

    public BicicletaEletrica(String id, String estacaoAtual, int nivelBateria) {
        super(id, estacaoAtual);
        this.nivelBateria = nivelBateria;
    }

    @Override
    public String getTipo() {
        return "eletrica";
    }

    @Override
    public BigDecimal calcularAdicionalPorTempo(long minutos) {
        return NumeroUtil.arredondar(
                ParametrosSistema.adicionalMinutoEletrica().multiply(BigDecimal.valueOf(minutos)));
    }

    @Override
    protected boolean prontaParaUso() {
        return nivelBateria >= ParametrosSistema.nivelMinimoBateria();
    }

    @Override
    public void validarRetirada() throws BikeshareException {
        if (!prontaParaUso()) {
            throw new RegraDeNegocioException(Mensagens.BATERIA_INSUFICIENTE);
        }
    }

    @Override
    public String getAtributo(String atributo) throws BikeshareException {
        if ("nivelBateria".equals(atributo)) {
            return String.valueOf(nivelBateria);
        }
        return super.getAtributo(atributo);
    }
}
