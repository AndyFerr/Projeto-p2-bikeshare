package br.ufal.ic.p2.bikeshare.exceptions;

/**
 * Excecao de negocio do BIKESHARE.
 *
 * <p>Todo erro previsto pelas regras de negocio e sinalizado por esta excecao (ou por
 * uma de suas especializacoes). A mensagem carregada por ela e exatamente a mensagem
 * verificada pelos testes de aceitacao, o que atende ao requisito nao funcional BS05:
 * mensagens de erro especificas e consistentes entre todos os comandos.</p>
 */
public class BikeshareException extends Exception {

    private static final long serialVersionUID = 1L;

    public BikeshareException(String mensagem) {
        super(mensagem);
    }
}
