package br.ufal.ic.p2.bikeshare.model.bicicleta;

/** Bicicleta sem motor: nao tem bateria e nao cobra adicional por minuto. */
public class BicicletaConvencional extends Bicicleta {

    private static final long serialVersionUID = 1L;

    public BicicletaConvencional(String id, String estacaoAtual) {
        super(id, estacaoAtual);
    }

    @Override
    public String getTipo() {
        return "convencional";
    }
}
