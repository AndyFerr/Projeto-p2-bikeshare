package br.ufal.ic.p2.bikeshare.persistence;

import br.ufal.ic.p2.bikeshare.service.SistemaBikeshare;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persistencia do estado do sistema entre execucoes (requisito BS03).
 *
 * <p>O estado completo e gravado em um unico arquivo no encerramento e relido na
 * inicializacao seguinte. Nao ha banco de dados: toda a serializacao se apoia no fato
 * de {@link SistemaBikeshare} e o grafo de objetos sob ele implementarem
 * {@link java.io.Serializable}.</p>
 */
public final class RepositorioSistema {

    private static final Path ARQUIVO = Path.of("bikeshare.ser");

    private RepositorioSistema() {
    }

    /** Le o estado gravado; devolve um sistema vazio se nao houver nada salvo. */
    public static SistemaBikeshare carregar() {
        if (!Files.isReadable(ARQUIVO)) {
            return new SistemaBikeshare();
        }
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(ARQUIVO))) {
            return (SistemaBikeshare) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new SistemaBikeshare();
        }
    }

    /** Grava o estado atual, substituindo o conteudo anterior. */
    public static void salvar(SistemaBikeshare sistema) {
        try (ObjectOutputStream saida = new ObjectOutputStream(Files.newOutputStream(ARQUIVO))) {
            saida.writeObject(sistema);
        } catch (IOException e) {
            throw new UncheckedIOException("Nao foi possivel salvar o estado do sistema.", e);
        }
    }

    /** Descarta o estado gravado, usado pelo comando de zerar o sistema. */
    public static void apagar() {
        try {
            Files.deleteIfExists(ARQUIVO);
        } catch (IOException e) {
            throw new UncheckedIOException("Nao foi possivel apagar o estado do sistema.", e);
        }
    }
}
