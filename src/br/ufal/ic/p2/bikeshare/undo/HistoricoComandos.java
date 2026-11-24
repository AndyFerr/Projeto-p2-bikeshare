package br.ufal.ic.p2.bikeshare.undo;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.service.SistemaBikeshare;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Historico de desfazer e refazer (US16).
 *
 * <p>Guarda retratos do estado anterior a cada comando bem-sucedido. Comandos
 * recusados nao entram no historico, porque nada chegou a mudar, e consultas tambem
 * nao, porque nao alteram estado. Um comando novo descarta a pilha de refazer.</p>
 *
 * <p>O historico vive apenas em memoria: ele nao e persistido, de modo que uma nova
 * execucao comeca sem nada a desfazer, com o estado gravado no encerramento.</p>
 */
public class HistoricoComandos {

    private final Deque<Memento> desfazer = new ArrayDeque<>();
    private final Deque<Memento> refazer = new ArrayDeque<>();

    /** Registra o retrato anterior a um comando que acabou de ser aplicado. */
    public void registrar(Memento estadoAnterior) {
        desfazer.push(estadoAnterior);
        refazer.clear();
    }

    /** Volta ao estado anterior ao ultimo comando aplicado. */
    public SistemaBikeshare desfazer(SistemaBikeshare estadoAtual) throws BikeshareException {
        if (desfazer.isEmpty()) {
            throw new RegraDeNegocioException(Mensagens.NADA_A_DESFAZER);
        }
        refazer.push(Memento.capturar(estadoAtual));
        return desfazer.pop().restaurar();
    }

    /** Reaplica o estado desfeito mais recentemente. */
    public SistemaBikeshare refazer(SistemaBikeshare estadoAtual) throws BikeshareException {
        if (refazer.isEmpty()) {
            throw new RegraDeNegocioException(Mensagens.NADA_A_REFAZER);
        }
        desfazer.push(Memento.capturar(estadoAtual));
        return refazer.pop().restaurar();
    }
}
