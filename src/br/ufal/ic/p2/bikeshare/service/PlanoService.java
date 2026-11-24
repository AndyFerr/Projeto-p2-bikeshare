package br.ufal.ic.p2.bikeshare.service;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;
import br.ufal.ic.p2.bikeshare.model.plano.Plano;
import br.ufal.ic.p2.bikeshare.util.TextoUtil;

import java.math.BigDecimal;

/**
 * Criacao de modalidades de tarifacao em tempo de execucao (US12).
 *
 * <p>Um plano criado aqui e indistinguivel dos planos pre-registrados: como toda a
 * tarifacao se apoia em franquia e valor do minuto excedente, nenhuma regra de
 * cobranca precisa saber que ele foi criado depois.</p>
 */
public class PlanoService {

    private final SistemaBikeshare sistema;

    public PlanoService(SistemaBikeshare sistema) {
        this.sistema = sistema;
    }

    public String criarPlano(String nome, String franquiaMinutos, String valorMinutoExcedente,
                             String mensalidade) throws BikeshareException {
        ValidadorEntrada.exigirPreenchido(nome, Mensagens.PLANO_NOME_NULO);
        if (sistema.buscarPlano(nome) != null) {
            throw new RegraDeNegocioException(Mensagens.PLANO_JA_EXISTE);
        }

        int franquia = ValidadorEntrada.exigirInteiro(franquiaMinutos,
                Mensagens.FRANQUIA_NULA, Mensagens.FRANQUIA_NAO_NUMERICA);
        if (franquia < 0) {
            throw new ValidacaoException(Mensagens.FRANQUIA_NEGATIVA);
        }

        BigDecimal excedente = ValidadorEntrada.exigirDecimal(valorMinutoExcedente,
                Mensagens.EXCEDENTE_NULO, Mensagens.EXCEDENTE_NAO_NUMERICO);
        ValidadorEntrada.exigirNaoNegativo(excedente, Mensagens.EXCEDENTE_NEGATIVO);

        BigDecimal valorMensalidade = ValidadorEntrada.exigirDecimal(mensalidade,
                Mensagens.MENSALIDADE_NULA, Mensagens.MENSALIDADE_NAO_NUMERICA);
        ValidadorEntrada.exigirNaoNegativo(valorMensalidade, Mensagens.MENSALIDADE_NEGATIVA);

        Plano plano = new Plano(TextoUtil.limpar(nome), franquia, excedente, valorMensalidade);
        sistema.registrarPlano(plano);
        return plano.getNome();
    }
}
