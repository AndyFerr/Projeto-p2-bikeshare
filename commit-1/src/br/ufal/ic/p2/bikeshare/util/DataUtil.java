package br.ufal.ic.p2.bikeshare.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conversao das datas trocadas com a camada de testes.
 *
 * <p>Retiradas e devolucoes usam {@code d/M/yyyy H:mm}; os relatorios de faturamento
 * usam {@code d/M/yyyy}, sem horario. A validacao e estrita: dias inexistentes no mes
 * (31/2), anos nao bissextos (29/2/2025), horas fora de 0-23 e minutos fora de 0-59
 * sao recusados, assim como qualquer formato diferente do esperado.</p>
 */
public final class DataUtil {

    private static final Pattern DATA_HORA =
            Pattern.compile("^(\\d{1,2})/(\\d{1,2})/(\\d{4}) (\\d{1,2}):(\\d{1,2})$");

    private static final Pattern DATA =
            Pattern.compile("^(\\d{1,2})/(\\d{1,2})/(\\d{4})$");

    private DataUtil() {
    }

    /**
     * Converte {@code d/M/yyyy H:mm} em data e hora.
     *
     * @return a data convertida, ou {@code null} se o texto nao for uma data valida.
     */
    public static LocalDateTime paraDataHora(String texto) {
        Matcher m = DATA_HORA.matcher(TextoUtil.limpar(texto));
        if (!m.matches()) {
            return null;
        }
        try {
            return LocalDateTime.of(
                    Integer.parseInt(m.group(3)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(4)),
                    Integer.parseInt(m.group(5)));
        } catch (DateTimeException e) {
            return null;
        }
    }

    /**
     * Converte {@code d/M/yyyy} em data, sem horario.
     *
     * @return a data convertida, ou {@code null} se o texto nao for uma data valida.
     */
    public static LocalDate paraData(String texto) {
        Matcher m = DATA.matcher(TextoUtil.limpar(texto));
        if (!m.matches()) {
            return null;
        }
        try {
            return LocalDate.of(
                    Integer.parseInt(m.group(3)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(1)));
        } catch (DateTimeException e) {
            return null;
        }
    }

    /** Formata uma data e hora como {@code d/M/yyyy H:mm}. */
    public static String formatar(LocalDateTime dataHora) {
        return dataHora.getDayOfMonth() + "/" + dataHora.getMonthValue() + "/" + dataHora.getYear()
                + " " + dataHora.getHour() + ":" + String.format("%02d", dataHora.getMinute());
    }
}
