package br.ufal.ic.p2.bikeshare.model.usuario;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.model.plano.Plano;

import java.math.BigDecimal;

/** Usuario pessoa juridica, identificado por CNPJ e razao social. */
public class PessoaJuridica extends Usuario {

    private static final long serialVersionUID = 1L;

    private final String cnpj;
    private final String razaoSocial;

    public PessoaJuridica(String id, String nome, String email, String senha,
                          Plano plano, BigDecimal mensalidadeContratada,
                          String cnpj, String razaoSocial) {
        super(id, nome, email, senha, plano, mensalidadeContratada);
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
    }

    @Override
    public String getTipo() {
        return "pessoaJuridica";
    }

    @Override
    public String getAtributo(String atributo) throws BikeshareException {
        return switch (atributo) {
            case "cnpj" -> cnpj;
            case "razaoSocial" -> razaoSocial;
            default -> super.getAtributo(atributo);
        };
    }
}
