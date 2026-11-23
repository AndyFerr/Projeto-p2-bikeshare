import easyaccept.EasyAccept;

/**
 * Executor dos testes de aceitacao do BIKESHARE.
 *
 * <p>Cada arquivo de script corresponde a uma User Story; os arquivos terminados em
 * {@code _1} reexecutam consultas depois de um reinicio e verificam a persistencia do
 * estado.</p>
 */
public class Main {

    private static final String FACADE = "br.ufal.ic.p2.bikeshare.Facade";

    private static final String[] SCRIPTS = {
            "tests/us1.txt", "tests/us1_1.txt",
            "tests/us2.txt",
            "tests/us3.txt", "tests/us3_1.txt",
            "tests/us4.txt", "tests/us4_1.txt",
            "tests/us5.txt", "tests/us5_1.txt",
            "tests/us6.txt", "tests/us6_1.txt",
            "tests/us7.txt",
            "tests/us8.txt", "tests/us8_1.txt",
            "tests/us9.txt", "tests/us9_1.txt",
            "tests/us10.txt", "tests/us10_1.txt",
            "tests/us11.txt", "tests/us11_1.txt",
            "tests/us12.txt", "tests/us12_1.txt",
            "tests/us13.txt", "tests/us13_1.txt",
            "tests/us14.txt", "tests/us14_1.txt",
            "tests/us15.txt", "tests/us15_1.txt",
            "tests/us16.txt", "tests/us16_1.txt",
    };

    public static void main(String[] args) {
        for (String script : SCRIPTS) {
            EasyAccept.main(new String[]{FACADE, script});
        }
    }
}
