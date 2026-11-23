package br.ufal.ic.p2.bikeshare.model.pagamento;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;

/**
 * Pagamento por cartao: a cobranca e sempre autorizada, cabendo a operadora,
 * fora do escopo do sistema, liquida-la.
 */
public class Cartao extends MetodoPagamento {

    private static final long serialVersionUID = 1L;

    private final String numero;
    private final String validade;

    public Cartao(String numero, String validade) {
        this.numero = numero;
        this.validade = validade;
    }

    @Override
    public String getNome() {
        return "cartao";
    }

    @Override
    public String getAtributo(String atributo) throws BikeshareException {
        return switch (atributo) {
            case "numeroCartao" -> numero;
            case "validadeCartao" -> validade;
            default -> super.getAtributo(atributo);
        };
    }
}
