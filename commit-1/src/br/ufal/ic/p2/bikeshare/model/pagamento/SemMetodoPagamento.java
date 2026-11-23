package br.ufal.ic.p2.bikeshare.model.pagamento;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;

/**
 * Metodo de pagamento de um usuario que ainda nao cadastrou nenhum.
 *
 * <p>Objeto nulo da hierarquia: evita testes de {@code null} espalhados pelo sistema
 * e concentra num unico lugar a recusa da retirada de quem nao tem como pagar.</p>
 */
public class SemMetodoPagamento extends MetodoPagamento {

    private static final long serialVersionUID = 1L;

    @Override
    public String getNome() {
        return "nenhum";
    }

    @Override
    public void validarRetirada() throws BikeshareException {
        throw new RegraDeNegocioException(Mensagens.METODO_PAGAMENTO_NAO_CADASTRADO);
    }
}
