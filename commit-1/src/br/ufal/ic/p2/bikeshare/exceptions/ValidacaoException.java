package br.ufal.ic.p2.bikeshare.exceptions;

/**
 * Erro de validacao de entrada: campo obrigatorio ausente, formato invalido ou
 * valor fora da faixa permitida.
 */
public class ValidacaoException extends BikeshareException {

    private static final long serialVersionUID = 1L;

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
