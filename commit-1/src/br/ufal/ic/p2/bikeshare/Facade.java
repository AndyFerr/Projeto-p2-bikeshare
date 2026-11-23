package br.ufal.ic.p2.bikeshare;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.persistence.RepositorioSistema;
import br.ufal.ic.p2.bikeshare.service.SistemaBikeshare;
import br.ufal.ic.p2.bikeshare.service.UsuarioService;

/**
 * Ponto unico de entrada do sistema BIKESHARE (requisito nao funcional BS01).
 *
 * <p>Nao ha interface grafica: os testes de aceitacao conversam com o sistema
 * exclusivamente por esta classe. A Facade nao contem regra de negocio &mdash; cada
 * chamada apenas delega ao servico responsavel &mdash; e e tambem quem cuida do ciclo
 * de vida do estado (carga na criacao, gravacao no encerramento).</p>
 */
public class Facade {

    private static final String NAO_IMPLEMENTADO = "Ainda nao implementado.";

    private SistemaBikeshare sistema;
    private UsuarioService usuarios;

    public Facade() {
        trocarSistema(RepositorioSistema.carregar());
    }

    /** Aponta a Facade e os servicos para um novo estado. */
    private void trocarSistema(SistemaBikeshare novoSistema) {
        this.sistema = novoSistema;
        this.usuarios = new UsuarioService(novoSistema);
    }

    // =====================================================================
    // US01 - Cadastro de usuario
    // =====================================================================

    public String criarUsuario(String nome, String email, String senha, String tipo, String cpf)
            throws BikeshareException {
        return usuarios.criarPessoaFisica(nome, email, senha, tipo, cpf, null, null);
    }

    public String criarUsuario(String nome, String email, String senha, String tipo,
                               String cnpj, String razaoSocial) throws BikeshareException {
        return usuarios.criarPessoaJuridica(nome, email, senha, tipo, cnpj, razaoSocial, null, null);
    }

    public String criarUsuario(String nome, String email, String senha, String tipo, String cpf,
                               String plano, String mensalidade) throws BikeshareException {
        return usuarios.criarPessoaFisica(nome, email, senha, tipo, cpf, plano, mensalidade);
    }

    public String criarUsuario(String nome, String email, String senha, String tipo, String cnpj,
                               String razaoSocial, String plano, String mensalidade)
            throws BikeshareException {
        return usuarios.criarPessoaJuridica(nome, email, senha, tipo, cnpj, razaoSocial, plano, mensalidade);
    }

    // =====================================================================
    // US02 - Consulta de usuario
    // =====================================================================

    public String getAtributoUsuario(String usuario, String atributo) throws BikeshareException {
        return usuarios.getAtributo(usuario, atributo);
    }

    public String getUsuarioPorEmail(String email) throws BikeshareException {
        return usuarios.getUsuarioPorEmail(email);
    }

    // =====================================================================
    // US03 - Alteracao de usuario (inclui metodo de pagamento)
    // =====================================================================

    public void alterarUsuario(String usuario, String atributo, String valor) throws BikeshareException {
        usuarios.alterar(usuario, atributo, valor);
    }

    public void alterarUsuario(String usuario, String atributo, String valor1, String chave)
            throws BikeshareException {
        usuarios.alterarMetodoPix(usuario, exigirMetodoPagamento(atributo, valor1), chave);
    }

    public void alterarUsuario(String usuario, String atributo, String valor1, String numero, String validade)
            throws BikeshareException {
        usuarios.alterarMetodoCartao(usuario, exigirMetodoPagamento(atributo, valor1), numero, validade);
    }

    /**
     * As assinaturas de quatro e cinco parametros existem apenas para o cadastro do
     * metodo de pagamento; qualquer outro atributo nesse formato nao e alteravel.
     */
    private String exigirMetodoPagamento(String atributo, String valor1) throws BikeshareException {
        if (!"metodoPagamento".equals(atributo)) {
            throw new BikeshareException("Atributo nao pode ser alterado.");
        }
        return valor1;
    }

    // =====================================================================
    // US04 - Remocao de usuario
    // =====================================================================

    public void removerUsuario(String usuario) throws BikeshareException {
        usuarios.remover(usuario);
    }

    // =====================================================================
    // US05 - Cadastro de estacao
    // =====================================================================

    public String criarEstacao(String nome, String endereco, String capacidade) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    // =====================================================================
    // US06 - Gestao de bicicletas na estacao
    // =====================================================================

    public void adicionarBicicleta(String estacao, String bicicleta, String tipo) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    public void adicionarBicicleta(String estacao, String bicicleta, String tipo, String nivelBateria) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    public void removerBicicleta(String estacao, String bicicleta) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    public String getAtributoBicicleta(String bicicleta, String atributo) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    // =====================================================================
    // US07 - Consulta de disponibilidade
    // =====================================================================

    public String getBicicletasDisponiveis(String estacao) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    public String getVagasDisponiveis(String estacao) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    public String getEstadoBicicleta(String bicicleta) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    // =====================================================================
    // US08 - Retirada de bicicleta
    // =====================================================================

    public String retirarBicicleta(String usuario, String estacao, String bicicleta, String data) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    // =====================================================================
    // US09 - Devolucao de bicicleta
    // =====================================================================

    public void devolverBicicleta(String aluguel, String estacao, String data) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    // =====================================================================
    // US10 - Consulta de historico de alugueis
    // =====================================================================

    public String getHistoricoAlugueis(String usuario) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    public String getHistoricoAlugueis(String usuario, String atributo) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    public String getAtributoAluguel(String aluguel, String atributo) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    // =====================================================================
    // US11/US12 - Planos e tarifacao
    // =====================================================================

    public String criarPlano(String nome, String franquiaMinutos, String valorMinutoExcedente,
                             String mensalidade) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    // =====================================================================
    // US13 - Regras de bloqueio / penalidades
    // =====================================================================

    public void lancaPenalidade(String usuario, String tipo, String valor, String motivo) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    // =====================================================================
    // US14 - Manutencao
    // =====================================================================

    public void lancaDefeito(String bicicleta, String descricao) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    public void concluiManutencao(String bicicleta) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    // =====================================================================
    // US15 - Relatorios
    // =====================================================================

    public String getTotalFaturado(String dataInicial, String dataFinal) {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    // =====================================================================
    // US16 - Undo / redo
    // =====================================================================

    public void undo() {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    public void redo() {
        throw new UnsupportedOperationException(NAO_IMPLEMENTADO);
    }

    // =====================================================================
    // Ciclo de vida do sistema
    // =====================================================================

    /** Descarta todo o estado, em memoria e em disco. */
    public void zerarSistema() {
        trocarSistema(new SistemaBikeshare());
        RepositorioSistema.apagar();
    }

    /** Grava o estado atual para que a proxima execucao continue de onde parou. */
    public void encerrarSistema() {
        RepositorioSistema.salvar(sistema);
    }
}
