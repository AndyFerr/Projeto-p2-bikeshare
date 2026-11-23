package br.ufal.ic.p2.bikeshare.model.bicicleta;

/**
 * Condicao operacional de uma bicicleta.
 *
 * <p>Uma bicicleta nasce {@link #DISPONIVEL}, vai para {@link #EM_USO} ao ser retirada
 * e volta a ficar disponivel na devolucao. Um defeito relatado &mdash; ou a expiracao
 * de um aluguel &mdash; a leva para {@link #EM_MANUTENCAO}, de onde so sai pela
 * conclusao da manutencao.</p>
 */
public enum EstadoBicicleta {

    DISPONIVEL("disponivel"),
    EM_USO("emUso"),
    EM_MANUTENCAO("emManutencao");

    private final String descricao;

    EstadoBicicleta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
