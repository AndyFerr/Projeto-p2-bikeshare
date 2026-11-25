package br.ufal.ic.p2.bikeshare.persistence;

import br.ufal.ic.p2.bikeshare.service.SistemaBikeshare;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Persistencia do estado do sistema entre execucoes (requisito BS03).
 *
 * <p>O estado completo e gravado em um unico arquivo no encerramento e relido na
 * inicializacao seguinte. Nao ha banco de dados: toda a serializacao se apoia no fato
 * de {@link SistemaBikeshare} e o grafo de objetos sob ele implementarem
 * {@link java.io.Serializable}.</p>
 *
 * <p>A gravacao e feita em duas etapas &mdash; um arquivo temporario que depois
 * substitui o definitivo &mdash;, de modo que uma interrupcao no meio da escrita nao
 * deixe para tras um arquivo pela metade: ou o estado anterior continua intacto, ou o
 * novo ja esta completo.</p>
 *
 * <p>Na leitura, um arquivo ilegivel nunca derruba o sistema. Ele e posto de lado com
 * um nome proprio, o problema e avisado na saida de erro e a execucao segue com um
 * sistema vazio. Essa escolha e deliberada: como o estado e lido na construcao da
 * Facade, lancar excecao ali abortaria a execucao inteira antes mesmo do primeiro
 * comando &mdash; e o caso mais comum de arquivo ilegivel nao e corrupcao de disco, e
 * sim um estado gravado por uma versao anterior das classes, situacao rotineira
 * durante o desenvolvimento. Pondo o arquivo de lado, nada se perde em silencio e
 * nada quebra.</p>
 */
public final class RepositorioSistema {

    private static final Path ARQUIVO = Path.of("bikeshare.ser");
    private static final Path TEMPORARIO = Path.of("bikeshare.ser.tmp");

    private static final DateTimeFormatter CARIMBO =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private RepositorioSistema() {
    }

    /**
     * Le o estado gravado.
     *
     * @return o estado recuperado; um sistema vazio se nao houver nada salvo ou se o
     *         arquivo existente nao puder ser lido.
     */
    public static SistemaBikeshare carregar() {
        if (!Files.exists(ARQUIVO)) {
            return new SistemaBikeshare();
        }
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(ARQUIVO))) {
            Object objeto = entrada.readObject();
            if (objeto instanceof SistemaBikeshare sistema) {
                return sistema;
            }
            return descartar("o arquivo contem um tipo de dado inesperado");
        } catch (IOException | ClassNotFoundException e) {
            return descartar(e.getMessage());
        }
    }

    /**
     * Poe de lado um estado ilegivel e recomeca do zero.
     *
     * <p>O arquivo e renomeado com a data e a hora em que foi descartado, para que
     * possa ser inspecionado depois, e o motivo vai para a saida de erro.</p>
     */
    private static SistemaBikeshare descartar(String motivo) {
        Path destino = Path.of(ARQUIVO + ".ilegivel-" + LocalDateTime.now().format(CARIMBO));
        String aviso;
        try {
            Files.move(ARQUIVO, destino, StandardCopyOption.REPLACE_EXISTING);
            aviso = "preservado em " + destino;
        } catch (IOException e) {
            aviso = "nao foi possivel preserva-lo (" + e.getMessage() + ")";
        }
        System.err.println("[BIKESHARE] Estado salvo ilegivel (" + motivo + "); "
                + aviso + ". Iniciando com um sistema vazio.");
        return new SistemaBikeshare();
    }

    /**
     * Grava o estado atual.
     *
     * <p>Escreve primeiro em um arquivo temporario e so entao o move sobre o
     * definitivo, de preferencia de forma atomica, para que o estado anterior nunca
     * seja substituido por uma gravacao incompleta.</p>
     */
    public static void salvar(SistemaBikeshare sistema) {
        try {
            try (ObjectOutputStream saida = new ObjectOutputStream(Files.newOutputStream(TEMPORARIO))) {
                saida.writeObject(sistema);
                saida.flush();
            }
            substituirArquivoDefinitivo();
        } catch (IOException e) {
            limparTemporario(e);
            throw new UncheckedIOException("Nao foi possivel salvar o estado do sistema.", e);
        }
    }

    /** Move o temporario sobre o definitivo, caindo para o modo nao atomico se preciso. */
    private static void substituirArquivoDefinitivo() throws IOException {
        try {
            Files.move(TEMPORARIO, ARQUIVO,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(TEMPORARIO, ARQUIVO, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Remove o temporario de uma gravacao que falhou, sem mascarar o erro original. */
    private static void limparTemporario(IOException erroOriginal) {
        try {
            Files.deleteIfExists(TEMPORARIO);
        } catch (IOException erroLimpeza) {
            erroOriginal.addSuppressed(erroLimpeza);
        }
    }

    /** Descarta o estado gravado, usado pelo comando de zerar o sistema. */
    public static void apagar() {
        try {
            Files.deleteIfExists(ARQUIVO);
            Files.deleteIfExists(TEMPORARIO);
        } catch (IOException e) {
            throw new UncheckedIOException("Nao foi possivel apagar o estado do sistema.", e);
        }
    }
}
