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
 * <p>Chave ausente e chave mal preenchida sao situacoes distintas. A primeira e
 * normal: o padrao assume o lugar. A segunda e um erro de configuracao &mdash; uma
 * tarifa escrita como {@code abc}, uma franquia negativa, um nivel de bateria acima de
 * 100 &mdash; e e recusada na inicializacao, em vez de silenciosamente trocada pelo
 * padrao. Aceitar o valor padrao no lugar de um valor invalido faria o sistema rodar
 * com uma regra diferente da que esta escrita no arquivo, e cobrar pela regra errada e
 * pior do que nao subir.</p>
 *
 * <p>A classe e um ponto unico de configuracao e nao guarda estado de execucao, por
 * isso nao participa da serializacao do sistema.</p>
 */
public final class ParametrosSistema {

    private static final String ARQUIVO_CONFIG = "config/bikeshare.properties";

    private static final int BATERIA_MINIMA = 0;
    private static final int BATERIA_MAXIMA = 100;

    private static final Properties PROPRIEDADES = carregar();

    static {
        validarConfiguracao();
    }

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
        if (!Files.exists(arquivo)) {
            return propriedades;
        }
        try (InputStream entrada = Files.newInputStream(arquivo)) {
            propriedades.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Nao foi possivel ler o arquivo de configuracao: " + arquivo.toAbsolutePath(), e);
        }
        return propriedades;
    }

    private static String texto(String chave, String padrao) {
        String valor = PROPRIEDADES.getProperty(chave);
        return (valor == null || valor.isBlank()) ? padrao : valor.trim();
    }

    private static int inteiro(String chave, int padrao) {
        String valor = texto(chave, String.valueOf(padrao));
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Configuracao invalida: " + chave + " deve ser um numero inteiro, mas vale \""
                            + valor + "\".", e);
        }
    }

    private static BigDecimal decimal(String chave, String padrao) {
        String valor = texto(chave, padrao);
        try {
            return new BigDecimal(valor.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Configuracao invalida: " + chave + " deve ser um numero, mas vale \""
                            + valor + "\".", e);
        }
    }

    // ---- validacao -----------------------------------------------------

    /**
     * Confere, na inicializacao, que os valores configurados fazem sentido como regra
     * de negocio. Uma chave ausente ja foi substituida pelo padrao neste ponto, de modo
     * que so chega aqui o que esta de fato escrito no arquivo.
     */
    private static void validarConfiguracao() {
        exigirNaoNegativo("plano.avulso.franquiaMinutos", BigDecimal.valueOf(franquiaAvulso()));
        exigirNaoNegativo("plano.avulso.valorMinuto", valorMinutoAvulso());
        exigirNaoNegativo("plano.assinante.franquiaMinutos", BigDecimal.valueOf(franquiaAssinante()));
        exigirNaoNegativo("plano.assinante.valorMinutoExcedente", valorMinutoExcedenteAssinante());
        exigirNaoNegativo("plano.assinante.mensalidade", mensalidadeAssinante());
        exigirNaoNegativo("bicicleta.eletrica.adicionalPorMinuto", adicionalMinutoEletrica());
        exigirNaoNegativo("aluguel.carenciaMinutos", BigDecimal.valueOf(carenciaMinutos()));
        exigirNaoNegativo("penalidade.atraso.valor", valorPenalidadeAtraso());

        int bateria = nivelMinimoBateria();
        if (bateria < BATERIA_MINIMA || bateria > BATERIA_MAXIMA) {
            throw new IllegalStateException("Configuracao invalida: "
                    + "bicicleta.eletrica.nivelMinimoBateria deve estar entre "
                    + BATERIA_MINIMA + " e " + BATERIA_MAXIMA + ", mas vale " + bateria + ".");
        }

        if (tempoMaximoMinutos() <= 0) {
            throw new IllegalStateException("Configuracao invalida: "
                    + "aluguel.tempoMaximoMinutos deve ser maior que zero, mas vale "
                    + tempoMaximoMinutos() + ".");
        }

        // So os planos pre-registrados existem quando o primeiro usuario e cadastrado,
        // entao apontar o plano padrao para qualquer outro nome deixaria o cadastro sem
        // plano algum.
        String plano = planoPadrao();
        if (!"avulso".equals(plano) && !"assinante".equals(plano)) {
            throw new IllegalStateException("Configuracao invalida: plano.padrao deve ser "
                    + "\"avulso\" ou \"assinante\", mas vale \"" + plano + "\".");
        }
    }

    private static void exigirNaoNegativo(String chave, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Configuracao invalida: " + chave + " nao pode ser negativo, mas vale "
                            + valor.toPlainString() + ".");
        }
    }
}
