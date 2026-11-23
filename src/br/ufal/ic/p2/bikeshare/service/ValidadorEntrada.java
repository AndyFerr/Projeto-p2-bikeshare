package br.ufal.ic.p2.bikeshare.service;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;
import br.ufal.ic.p2.bikeshare.util.NumeroUtil;
import br.ufal.ic.p2.bikeshare.util.TextoUtil;

import java.math.BigDecimal;

/**
 * Validacoes de entrada reaproveitadas pelos servicos.
 *
 * <p>Cada metodo recebe a mensagem a ser lancada, de modo que a mesma regra
 * (&ldquo;campo obrigatorio&rdquo;, &ldquo;deve ser numerico&rdquo;) possa reportar o
 * texto exato que cada comando exige, sem duplicar a logica de validacao.</p>
 */
public final class ValidadorEntrada {

    private ValidadorEntrada() {
    }

    /** Recusa um campo obrigatorio vazio ou formado apenas por espacos. */
    public static void exigirPreenchido(String valor, String mensagem) throws BikeshareException {
        if (TextoUtil.vazio(valor)) {
            throw new ValidacaoException(mensagem);
        }
    }

    /** Converte um inteiro obrigatorio, recusando vazio e formato nao inteiro. */
    public static int exigirInteiro(String valor, String mensagemNulo, String mensagemFormato)
            throws BikeshareException {
        exigirPreenchido(valor, mensagemNulo);
        if (!NumeroUtil.ehInteiro(valor)) {
            throw new ValidacaoException(mensagemFormato);
        }
        return NumeroUtil.paraInteiro(valor);
    }

    /** Converte um decimal obrigatorio, recusando vazio e formato nao numerico. */
    public static BigDecimal exigirDecimal(String valor, String mensagemNulo, String mensagemFormato)
            throws BikeshareException {
        exigirPreenchido(valor, mensagemNulo);
        if (!NumeroUtil.ehDecimal(valor)) {
            throw new ValidacaoException(mensagemFormato);
        }
        return NumeroUtil.paraDecimal(valor);
    }

    /** Recusa valores negativos (zero e aceito). */
    public static void exigirNaoNegativo(BigDecimal valor, String mensagem) throws BikeshareException {
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacaoException(mensagem);
        }
    }

    /** Recusa valores menores ou iguais a zero. */
    public static void exigirPositivo(BigDecimal valor, String mensagem) throws BikeshareException {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException(mensagem);
        }
    }

    /** Recusa inteiros menores ou iguais a zero. */
    public static void exigirPositivo(int valor, String mensagem) throws BikeshareException {
        if (valor <= 0) {
            throw new ValidacaoException(mensagem);
        }
    }
}
