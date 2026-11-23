package br.ufal.ic.p2.bikeshare.exceptions;

/**
 * Erro gerado quando a operacao e sintaticamente valida mas viola uma regra de
 * negocio (usuario inadimplente, estacao cheia, bicicleta indisponivel, ...).
 */
public class RegraDeNegocioException extends BikeshareException {

    private static final long serialVersionUID = 1L;

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
