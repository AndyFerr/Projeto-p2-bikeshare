package br.ufal.ic.p2.bikeshare.model.pagamento;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Forma pela qual o usuario paga suas cobrancas.
 *
 * <p>Raiz da hierarquia de pagamento. Cada subclasse decide sozinha se autoriza o
 * inicio de uma corrida ({@link #validarRetirada()}), como registra a cobranca da
 * devolucao ({@link #cobrar(BigDecimal)}) e quais atributos sabe responder
 * ({@link #getAtributo(String)}) &mdash; perguntar o numero do cartao a quem paga por
 * Pix e um erro de negocio, nao um atributo inexistente.</p>
 */
public abstract class MetodoPagamento implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Nome do metodo tal como exibido nas consultas. */
    public abstract String getNome();

    /**
     * Verifica se o metodo autoriza iniciar uma nova corrida, cujo valor ainda e
     * desconhecido. Por padrao autoriza; subclasses restringem.
     */
    public void validarRetirada() throws BikeshareException {
        // Cartao e Pix nao impoem restricao previa.
    }

    /**
     * Registra a cobranca do valor apurado na devolucao.
     *
     * @throws BikeshareException se o metodo nao puder arcar com o valor.
     */
    public void cobrar(BigDecimal valor) throws BikeshareException {
        // Cartao e Pix nao mantem saldo: nada a debitar localmente.
    }

    /**
     * Responde um atributo do metodo de pagamento.
     *
     * <p>A implementacao padrao recusa todos os atributos especificos de pagamento
     * com a mensagem do metodo que o usuario <em>nao</em> usa; cada subclasse trata
     * apenas os seus.</p>
     */
    public String getAtributo(String atributo) throws BikeshareException {
        switch (atributo) {
            case "numeroCartao", "validadeCartao" ->
                    throw new RegraDeNegocioException(Mensagens.NAO_USA_CARTAO);
            case "chavePix" -> throw new RegraDeNegocioException(Mensagens.NAO_USA_PIX);
            case "saldo" -> throw new RegraDeNegocioException(Mensagens.NAO_USA_SALDO);
            default -> throw new ValidacaoException(Mensagens.ATRIBUTO_NAO_EXISTE);
        }
    }
}
