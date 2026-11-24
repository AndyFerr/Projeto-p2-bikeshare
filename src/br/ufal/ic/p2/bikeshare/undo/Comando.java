package br.ufal.ic.p2.bikeshare.undo;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;

/**
 * Operacao que altera o estado do sistema e devolve um resultado.
 *
 * <p>Existe para que a Facade possa envolver qualquer comando no mesmo protocolo de
 * registro no historico, sem repetir o tratamento em cada metodo.</p>
 */
@FunctionalInterface
public interface Comando<T> {

    T executar() throws BikeshareException;
}
