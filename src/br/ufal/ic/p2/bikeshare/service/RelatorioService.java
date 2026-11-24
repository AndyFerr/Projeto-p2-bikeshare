package br.ufal.ic.p2.bikeshare.service;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;
import br.ufal.ic.p2.bikeshare.model.aluguel.Aluguel;
import br.ufal.ic.p2.bikeshare.util.DataUtil;
import br.ufal.ic.p2.bikeshare.util.NumeroUtil;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Relatorios gerenciais do sistema (US15).
 *
 * <p>O faturamento e contado pelo dia da <em>devolucao</em>, que e o momento em que a
 * cobranca de fato acontece: uma corrida iniciada em um dia e devolvida no seguinte
 * entra no periodo do segundo, e uma corrida ainda em andamento nao entra em nenhum.
 * O periodo e fechado nas duas pontas e cobre os dias inteiros informados.</p>
 */
public class RelatorioService {

    private final SistemaBikeshare sistema;

    public RelatorioService(SistemaBikeshare sistema) {
        this.sistema = sistema;
    }

    public String getTotalFaturado(String dataInicial, String dataFinal) throws BikeshareException {
        LocalDate inicio = exigirData(dataInicial);
        LocalDate fim = exigirData(dataFinal);
        if (fim.isBefore(inicio)) {
            throw new ValidacaoException(Mensagens.DATA_FINAL_ANTES_DA_INICIAL);
        }

        BigDecimal total = NumeroUtil.zero();
        for (Aluguel aluguel : sistema.getAlugueis().values()) {
            if (aluguel.getFim() == null) {
                continue;
            }
            LocalDate diaDaCobranca = aluguel.getFim().toLocalDate();
            if (!diaDaCobranca.isBefore(inicio) && !diaDaCobranca.isAfter(fim)) {
                total = total.add(aluguel.getValorCobrado());
            }
        }
        return NumeroUtil.formatarMoeda(total);
    }

    /** Converte uma data obrigatoria sem horario, com as mensagens padrao do sistema. */
    private LocalDate exigirData(String data) throws BikeshareException {
        ValidadorEntrada.exigirPreenchido(data, Mensagens.DATA_NULA);
        LocalDate convertida = DataUtil.paraData(data);
        if (convertida == null) {
            throw new ValidacaoException(Mensagens.DATA_INVALIDA);
        }
        return convertida;
    }
}
