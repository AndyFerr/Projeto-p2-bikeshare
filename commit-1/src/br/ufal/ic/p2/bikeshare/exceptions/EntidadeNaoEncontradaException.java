package br.ufal.ic.p2.bikeshare.exceptions;

/**
 * Erro gerado quando um identificador informado nao corresponde a nenhuma
 * entidade cadastrada (usuario, estacao, bicicleta, aluguel ou plano).
 */
public class EntidadeNaoEncontradaException extends BikeshareException {

    private static final long serialVersionUID = 1L;

    public EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
