package br.ufal.ic.p2.bikeshare.util;

/**
 * Utilitarios de texto usados pelas validacoes de entrada.
 *
 * <p>Em todo o sistema um campo preenchido apenas com espacos equivale a um campo
 * vazio, de modo que {@code nome=""} e {@code nome="   "} produzem a mesma mensagem
 * de erro (requisito nao funcional BS05).</p>
 */
public final class TextoUtil {

    private TextoUtil() {
    }

    /** Indica se o texto e nulo, vazio ou formado apenas por espacos. */
    public static boolean vazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    /** Remove os espacos das pontas, tratando nulo como vazio. */
    public static String limpar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    /** Compara dois textos ignorando espacos nas pontas e diferenca de caixa. */
    public static boolean equivalentes(String um, String outro) {
        return limpar(um).equalsIgnoreCase(limpar(outro));
    }

    /** Normaliza um email para servir de chave de unicidade (sem caixa). */
    public static String chaveEmail(String email) {
        return limpar(email).toLowerCase();
    }
}
