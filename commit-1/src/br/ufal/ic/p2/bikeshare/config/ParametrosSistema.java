package br.ufal.ic.p2.bikeshare.config;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Parametros de tarifacao e limites de negocio do BIKESHARE.
 *
 * <p>Atende ao requisito nao funcional BS04: nenhum valor de regra de negocio
 * (tarifa por minuto, franquia, carencia, tempo maximo de aluguel, nivel minimo de
 * bateria, valor da penalidade de atraso) fica embutido no meio do codigo. Todos sao
 * lidos de {@code config/bikeshare.properties} na inicializacao; se o arquivo nao
 * existir ou uma chave estiver ausente, vale o padrao declarado aqui.</p>
 *
 * <p>A classe e um ponto unico de configuracao e nao guarda estado de execucao, por
 * isso nao participa da serializacao do sistema.</p>
 */
public final class ParametrosSistema {

    private static final String ARQUIVO_CONFIG = "config/bikeshare.properties";

    private static final Properties PROPRIEDADES = carregar();

    private ParametrosSistema() {
    }

    // ---- planos padrao -------------------------------------------------

    /** Nome do plano padrao, atribuido a quem nao escolhe um plano no cadastro. */
    public static String planoPadrao() {
        return texto("plano.padrao", "avulso");
    }

    /** Franquia de minutos gratuitos do plano avulso (nenhuma). */
    public static int franquiaAvulso() {
        return inteiro("plano.avulso.franquiaMinutos", 0);
    }

    /** Valor cobrado por minuto no plano avulso. */
    public static BigDecimal valorMinutoAvulso() {
        return decimal("plano.avulso.valorMinuto", "0.50");
    }

    /** Franquia de minutos gratuitos por corrida no plano assinante. */
    public static int franquiaAssinante() {
        return inteiro("plano.assinante.franquiaMinutos", 60);
    }

    /** Valor cobrado por minuto excedente a franquia no plano assinante. */
    public static BigDecimal valorMinutoExcedenteAssinante() {
        return decimal("plano.assinante.valorMinutoExcedente", "0.30");
    }

    /** Mensalidade padrao do plano assinante. */
    public static BigDecimal mensalidadeAssinante() {
        return decimal("plano.assinante.mensalidade", "30.00");
    }

    // ---- bicicletas ----------------------------------------------------

    /** Adicional por minuto cobrado em corridas com bicicleta eletrica. */
    public static BigDecimal adicionalMinutoEletrica() {
        return decimal("bicicleta.eletrica.adicionalPorMinuto", "0.20");
    }

    /** Nivel minimo de bateria (%) para que uma eletrica possa ser retirada. */
    public static int nivelMinimoBateria() {
        return inteiro("bicicleta.eletrica.nivelMinimoBateria", 15);
    }

    // ---- alugueis ------------------------------------------------------

    /** Prazo de carencia, em minutos, para cancelar sem cobranca na mesma estacao. */
    public static long carenciaMinutos() {
        return inteiro("aluguel.carenciaMinutos", 2);
    }

    /** Tempo maximo de uso, em minutos, antes de o aluguel expirar. */
    public static long tempoMaximoMinutos() {
        return inteiro("aluguel.tempoMaximoMinutos", 1440);
    }

    /** Valor fixo da penalidade automatica aplicada a um aluguel expirado. */
    public static BigDecimal valorPenalidadeAtraso() {
        return decimal("penalidade.atraso.valor", "10.00");
    }

    // ---- leitura -------------------------------------------------------

    private static Properties carregar() {
        Properties propriedades = new Properties();
        Path arquivo = Path.of(ARQUIVO_CONFIG);
        if (Files.isReadable(arquivo)) {
            try (InputStream entrada = Files.newInputStream(arquivo)) {
                propriedades.load(entrada);
            } catch (IOException ignorada) {
                // Configuracao ilegivel: o sistema segue com os valores padrao.
            }
        }
        return propriedades;
    }

    private static String texto(String chave, String padrao) {
        String valor = PROPRIEDADES.getProperty(chave);
        return (valor == null || valor.isBlank()) ? padrao : valor.trim();
    }

    private static int inteiro(String chave, int padrao) {
        try {
            return Integer.parseInt(texto(chave, String.valueOf(padrao)));
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    private static BigDecimal decimal(String chave, String padrao) {
        try {
            return new BigDecimal(texto(chave, padrao).replace(",", "."));
        } catch (NumberFormatException e) {
            return new BigDecimal(padrao);
        }
    }
}
