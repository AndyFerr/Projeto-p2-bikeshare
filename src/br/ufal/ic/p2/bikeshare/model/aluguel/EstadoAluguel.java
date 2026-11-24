package br.ufal.ic.p2.bikeshare.model.aluguel;

/**
 * Condicao de uma corrida.
 *
 * <p>Toda corrida nasce {@link #EM_ANDAMENTO} e termina de uma destas tres formas:
 * {@link #FINALIZADO} na devolucao comum, {@link #CANCELADO} quando a bicicleta volta
 * a estacao de origem dentro do prazo de carencia e {@link #EXPIRADO} quando o tempo
 * maximo de uso e ultrapassado.</p>
 */
public enum EstadoAluguel {

    EM_ANDAMENTO("emAndamento"),
    FINALIZADO("finalizado"),
    CANCELADO("cancelado"),
    EXPIRADO("expirado");

    private final String descricao;

    EstadoAluguel(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
