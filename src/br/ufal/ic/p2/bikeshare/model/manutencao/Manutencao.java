package br.ufal.ic.p2.bikeshare.model.manutencao;

import java.io.Serializable;

/**
 * Ordem de manutencao aberta por um defeito relatado em uma bicicleta.
 *
 * <p>Guarda a descricao do problema e se ele ja foi resolvido, de modo que o
 * historico de manutencoes de uma bicicleta sobreviva a conclusao do reparo.</p>
 */
public class Manutencao implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String descricao;
    private boolean concluida;

    public Manutencao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean estaConcluida() {
        return concluida;
    }

    public void concluir() {
        this.concluida = true;
    }
}
