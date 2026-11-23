package br.ufal.ic.p2.bikeshare.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Conversao e formatacao dos numeros trocados com a camada de testes.
 *
 * <p>Valores monetarios circulam internamente como {@link BigDecimal} com duas casas
 * decimais, para que somas de tarifas e penalidades nao acumulem erro de ponto
 * flutuante, e sao exibidos no formato brasileiro ({@code 1234,56}).</p>
 */
public final class NumeroUtil {

    private static final String INTEIRO = "-?\\d+";
    private static final String DECIMAL = "-?\\d+([.,]\\d+)?";

    private NumeroUtil() {
    }

    /** Indica se o texto representa um inteiro (sem casas decimais). */
    public static boolean ehInteiro(String texto) {
        return TextoUtil.limpar(texto).matches(INTEIRO);
    }

    /** Indica se o texto representa um numero decimal (virgula ou ponto). */
    public static boolean ehDecimal(String texto) {
        return TextoUtil.limpar(texto).matches(DECIMAL);
    }

    /** Converte um texto ja validado por {@link #ehInteiro(String)}. */
    public static int paraInteiro(String texto) {
        return Integer.parseInt(TextoUtil.limpar(texto));
    }

    /** Converte um texto ja validado por {@link #ehDecimal(String)}. */
    public static BigDecimal paraDecimal(String texto) {
        return arredondar(new BigDecimal(TextoUtil.limpar(texto).replace(',', '.')));
    }

    /** Arredonda um valor monetario para duas casas decimais. */
    public static BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    /** Formata um valor monetario no padrao brasileiro, como {@code 120,50}. */
    public static String formatarMoeda(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator(',');
        return new DecimalFormat("0.00", simbolos).format(arredondar(valor));
    }

    /** Valor monetario zero, ja na escala usada pelo sistema. */
    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
