package br.ufal.ic.p2.bikeshare.undo;

import br.ufal.ic.p2.bikeshare.service.SistemaBikeshare;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;

/**
 * Retrato imutavel do estado do sistema.
 *
 * <p>Guardar o estado inteiro, em vez de uma operacao inversa por comando, e o que
 * torna possivel desfazer ate mesmo {@code zerarSistema} e manter contadores de
 * identificadores coerentes depois de um desfazer.</p>
 */
public final class Memento {

    private final byte[] estado;

    private Memento(byte[] estado) {
        this.estado = estado;
    }

    /** Copia profunda do sistema, aproveitando a mesma serializacao da persistencia. */
    public static Memento capturar(SistemaBikeshare sistema) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream saida = new ObjectOutputStream(bytes)) {
            saida.writeObject(sistema);
            saida.flush();
            return new Memento(bytes.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Nao foi possivel capturar o estado do sistema.", e);
        }
    }

    /** Reconstroi o sistema a partir do retrato. */
    public SistemaBikeshare restaurar() {
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(estado))) {
            return (SistemaBikeshare) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Nao foi possivel restaurar o estado do sistema.", e);
        }
    }
}
