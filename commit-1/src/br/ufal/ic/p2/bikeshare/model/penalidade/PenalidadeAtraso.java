package br.ufal.ic.p2.bikeshare.model.penalidade;

import br.ufal.ic.p2.bikeshare.config.ParametrosSistema;

/**
 * Penalidade automatica aplicada quando um aluguel ultrapassa o tempo maximo de uso
 * e expira. Diferente das demais, o operador nao escolhe o valor: ele vem fixo da
 * configuracao do sistema.
 */
public class PenalidadeAtraso extends Penalidade {

    private static final long serialVersionUID = 1L;

    public PenalidadeAtraso(String motivo) {
        super(ParametrosSistema.valorPenalidadeAtraso(), motivo);
    }

    @Override
    public String getTipo() {
        return "atraso";
    }
}
