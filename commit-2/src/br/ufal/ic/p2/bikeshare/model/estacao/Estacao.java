package br.ufal.ic.p2.bikeshare.model.estacao;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Ponto fisico de retirada e devolucao, com um numero fixo de vagas.
 *
 * <p>A estacao controla apenas a ocupacao fisica: toda bicicleta presente ocupa uma
 * vaga, esteja ela disponivel ou em manutencao. Quantas dessas bicicletas podem de
 * fato ser retiradas e uma pergunta sobre o estado de cada bicicleta, respondida
 * pelo servico.</p>
 */
public class Estacao implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String nome;
    private final String endereco;
    private final int capacidade;

    private final Set<String> bicicletas = new LinkedHashSet<>();

    public Estacao(String id, String nome, String endereco, int capacidade) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.capacidade = capacidade;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public Set<String> getBicicletas() {
        return Collections.unmodifiableSet(bicicletas);
    }

    /** Vagas livres: a capacidade menos as bicicletas fisicamente presentes. */
    public int getVagasLivres() {
        return capacidade - bicicletas.size();
    }

    public boolean temVagaLivre() {
        return getVagasLivres() > 0;
    }

    public void acolher(String idBicicleta) {
        bicicletas.add(idBicicleta);
    }

    public void liberar(String idBicicleta) {
        bicicletas.remove(idBicicleta);
    }
}
