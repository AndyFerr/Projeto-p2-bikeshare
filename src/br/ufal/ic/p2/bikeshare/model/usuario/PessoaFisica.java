package br.ufal.ic.p2.bikeshare.model.usuario;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.model.plano.Plano;

import java.math.BigDecimal;

/** Usuario pessoa fisica, identificado pelo CPF. */
public class PessoaFisica extends Usuario {

    private static final long serialVersionUID = 1L;

    private final String cpf;

    public PessoaFisica(String id, String nome, String email, String senha,
                        Plano plano, BigDecimal mensalidadeContratada, String cpf) {
        super(id, nome, email, senha, plano, mensalidadeContratada);
        this.cpf = cpf;
    }

    @Override
    public String getTipo() {
        return "pessoaFisica";
    }

    @Override
    public String getAtributo(String atributo) throws BikeshareException {
        if ("cpf".equals(atributo)) {
            return cpf;
        }
        return super.getAtributo(atributo);
    }
}
