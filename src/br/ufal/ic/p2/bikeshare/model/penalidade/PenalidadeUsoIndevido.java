package br.ufal.ic.p2.bikeshare.model.penalidade;

import java.math.BigDecimal;

/**
 * Penalidade lancada pelo operador por uso fora das regras do servico, como
 * devolucao em local nao autorizado.
 */
public class PenalidadeUsoIndevido extends Penalidade {

    private static final long serialVersionUID = 1L;

    public PenalidadeUsoIndevido(BigDecimal valor, String motivo) {
        super(valor, motivo);
    }

    @Override
    public String getTipo() {
        return "usoIndevido";
    }
}
