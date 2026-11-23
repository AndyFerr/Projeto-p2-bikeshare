package br.ufal.ic.p2.bikeshare.model.penalidade;

import java.math.BigDecimal;

/** Penalidade lancada pelo operador por dano causado a bicicleta. */
public class PenalidadeDano extends Penalidade {

    private static final long serialVersionUID = 1L;

    public PenalidadeDano(BigDecimal valor, String motivo) {
        super(valor, motivo);
    }

    @Override
    public String getTipo() {
        return "dano";
    }
}
