package br.ufal.ic.p2.bikeshare.model.pagamento;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;

/** Pagamento por Pix, identificado pela chave cadastrada pelo usuario. */
public class Pix extends MetodoPagamento {

    private static final long serialVersionUID = 1L;

    private final String chave;

    public Pix(String chave) {
        this.chave = chave;
    }

    @Override
    public String getNome() {
        return "pix";
    }

    @Override
    public String getAtributo(String atributo) throws BikeshareException {
        if ("chavePix".equals(atributo)) {
            return chave;
        }
        return super.getAtributo(atributo);
    }
}
