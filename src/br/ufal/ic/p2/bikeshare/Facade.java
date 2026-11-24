package br.ufal.ic.p2.bikeshare;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;
import br.ufal.ic.p2.bikeshare.persistence.RepositorioSistema;
import br.ufal.ic.p2.bikeshare.service.AluguelService;
import br.ufal.ic.p2.bikeshare.service.EstacaoService;
import br.ufal.ic.p2.bikeshare.service.ManutencaoService;
import br.ufal.ic.p2.bikeshare.service.PenalidadeService;
import br.ufal.ic.p2.bikeshare.service.PlanoService;
import br.ufal.ic.p2.bikeshare.service.RelatorioService;
import br.ufal.ic.p2.bikeshare.service.SistemaBikeshare;
import br.ufal.ic.p2.bikeshare.service.UsuarioService;
import br.ufal.ic.p2.bikeshare.undo.Comando;
import br.ufal.ic.p2.bikeshare.undo.HistoricoComandos;
import br.ufal.ic.p2.bikeshare.undo.Memento;

/**
 * Ponto unico de entrada do sistema BIKESHARE (requisito nao funcional BS01).
 *
 * <p>Nao ha interface grafica: os testes de aceitacao conversam com o sistema
 * exclusivamente por esta classe. A Facade nao contem regra de negocio &mdash; cada
 * chamada apenas delega ao servico responsavel. Cabe a ela, porem, tres
 * responsabilidades de infraestrutura: carregar e gravar o estado, apontar os
 * servicos para o estado vigente e registrar no historico de desfazer todo comando
 * que termina com sucesso.</p>
 */
public class Facade {

    private SistemaBikeshare sistema;
    private final HistoricoComandos historico = new HistoricoComandos();

    private UsuarioService usuarios;
    private EstacaoService estacoes;
    private AluguelService alugueis;
    private PlanoService planos;
    private PenalidadeService penalidades;
    private ManutencaoService manutencoes;
    private RelatorioService relatorios;

    public Facade() {
        trocarSistema(RepositorioSistema.carregar());
    }

    /** Aponta a Facade e todos os servicos para um novo estado. */
    private void trocarSistema(SistemaBikeshare novoSistema) {
        this.sistema = novoSistema;
        this.usuarios = new UsuarioService(novoSistema);
        this.estacoes = new EstacaoService(novoSistema);
        this.alugueis = new AluguelService(novoSistema, this.usuarios, this.estacoes);
        this.planos = new PlanoService(novoSistema);
        this.penalidades = new PenalidadeService(this.usuarios);
        this.manutencoes = new ManutencaoService(this.estacoes);
        this.relatorios = new RelatorioService(novoSistema);
    }

    /**
     * Executa um comando que altera o estado, guardando antes um retrato do sistema.
     *
     * <p>O retrato so entra no historico se o comando terminar sem erro: operacoes
     * recusadas nao mudaram nada e, por isso, tambem nao podem ser desfeitas.</p>
     */
    private <T> T executar(Comando<T> comando) throws BikeshareException {
        Memento estadoAnterior = Memento.capturar(sistema);
        T resultado = comando.executar();
        historico.registrar(estadoAnterior);
        return resultado;
    }

    /** Versao de {@link #executar(Comando)} para comandos sem retorno. */
    private void executar(ComandoSemRetorno comando) throws BikeshareException {
        executar(() -> {
            comando.executar();
            return null;
        });
    }

    @FunctionalInterface
    private interface ComandoSemRetorno {
        void executar() throws BikeshareException;
    }

    // =====================================================================
    // US01 - Cadastro de usuario
    // =====================================================================

    public String criarUsuario(String nome, String email, String senha, String tipo, String cpf)
            throws BikeshareException {
        return executar(() -> usuarios.criarPessoaFisica(nome, email, senha, tipo, cpf, null, null));
    }

    public String criarUsuario(String nome, String email, String senha, String tipo,
                               String cnpj, String razaoSocial) throws BikeshareException {
        return executar(() ->
                usuarios.criarPessoaJuridica(nome, email, senha, tipo, cnpj, razaoSocial, null, null));
    }

    public String criarUsuario(String nome, String email, String senha, String tipo, String cpf,
                               String plano, String mensalidade) throws BikeshareException {
        return executar(() ->
                usuarios.criarPessoaFisica(nome, email, senha, tipo, cpf, plano, mensalidade));
    }

    public String criarUsuario(String nome, String email, String senha, String tipo, String cnpj,
                               String razaoSocial, String plano, String mensalidade)
            throws BikeshareException {
        return executar(() ->
                usuarios.criarPessoaJuridica(nome, email, senha, tipo, cnpj, razaoSocial, plano, mensalidade));
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
        executar(() -> usuarios.alterar(usuario, atributo, valor));
    }

    public void alterarUsuario(String usuario, String atributo, String valor1, String chave)
            throws BikeshareException {
        executar(() -> usuarios.alterarMetodoPix(usuario, exigirMetodoPagamento(atributo, valor1), chave));
    }

    public void alterarUsuario(String usuario, String atributo, String valor1, String numero, String validade)
            throws BikeshareException {
        executar(() ->
                usuarios.alterarMetodoCartao(usuario, exigirMetodoPagamento(atributo, valor1), numero, validade));
    }

    /**
     * As assinaturas de quatro e cinco parametros existem apenas para o cadastro do
     * metodo de pagamento; qualquer outro atributo nesse formato nao e alteravel.
     */
    private String exigirMetodoPagamento(String atributo, String valor1) throws BikeshareException {
        if (!"metodoPagamento".equals(atributo)) {
            throw new ValidacaoException(Mensagens.ATRIBUTO_NAO_ALTERAVEL);
        }
        return valor1;
    }

    // =====================================================================
    // US04 - Remocao de usuario
    // =====================================================================

    public void removerUsuario(String usuario) throws BikeshareException {
        executar(() -> usuarios.remover(usuario));
    }

    // =====================================================================
    // US05 - Cadastro de estacao
    // =====================================================================

    public String criarEstacao(String nome, String endereco, String capacidade) throws BikeshareException {
        return executar(() -> estacoes.criarEstacao(nome, endereco, capacidade));
    }

    // =====================================================================
    // US06 - Gestao de bicicletas na estacao
    // =====================================================================

    public void adicionarBicicleta(String estacao, String bicicleta, String tipo) throws BikeshareException {
        executar(() -> estacoes.adicionarBicicleta(estacao, bicicleta, tipo, null));
    }

    public void adicionarBicicleta(String estacao, String bicicleta, String tipo, String nivelBateria)
            throws BikeshareException {
        executar(() -> estacoes.adicionarBicicleta(estacao, bicicleta, tipo, nivelBateria));
    }

    public void removerBicicleta(String estacao, String bicicleta) throws BikeshareException {
        executar(() -> estacoes.removerBicicleta(estacao, bicicleta));
    }

    public String getAtributoBicicleta(String bicicleta, String atributo) throws BikeshareException {
        return estacoes.getAtributoBicicleta(bicicleta, atributo);
    }

    // =====================================================================
    // US07 - Consulta de disponibilidade
    // =====================================================================

    public String getBicicletasDisponiveis(String estacao) throws BikeshareException {
        return estacoes.getBicicletasDisponiveis(estacao);
    }

    public String getVagasDisponiveis(String estacao) throws BikeshareException {
        return estacoes.getVagasDisponiveis(estacao);
    }

    public String getEstadoBicicleta(String bicicleta) throws BikeshareException {
        return estacoes.getEstadoBicicleta(bicicleta);
    }

    // =====================================================================
    // US08 - Retirada de bicicleta
    // =====================================================================

    public String retirarBicicleta(String usuario, String estacao, String bicicleta, String data)
            throws BikeshareException {
        return executar(() -> alugueis.retirar(usuario, estacao, bicicleta, data));
    }

    // =====================================================================
    // US09 - Devolucao de bicicleta
    // =====================================================================

    public void devolverBicicleta(String aluguel, String estacao, String data) throws BikeshareException {
        executar(() -> alugueis.devolver(aluguel, estacao, data));
    }

    // =====================================================================
    // US10 - Consulta de historico de alugueis
    // =====================================================================

    public String getHistoricoAlugueis(String usuario) throws BikeshareException {
        return alugueis.getHistorico(usuario, null);
    }

    public String getHistoricoAlugueis(String usuario, String atributo) throws BikeshareException {
        return alugueis.getHistorico(usuario, atributo);
    }

    public String getAtributoAluguel(String aluguel, String atributo) throws BikeshareException {
        return alugueis.getAtributoAluguel(aluguel, atributo);
    }

    // =====================================================================
    // US11/US12 - Planos e tarifacao
    // =====================================================================

    public String criarPlano(String nome, String franquiaMinutos, String valorMinutoExcedente,
                             String mensalidade) throws BikeshareException {
        return executar(() -> planos.criarPlano(nome, franquiaMinutos, valorMinutoExcedente, mensalidade));
    }

    // =====================================================================
    // US13 - Regras de bloqueio / penalidades
    // =====================================================================

    public void lancaPenalidade(String usuario, String tipo, String valor, String motivo)
            throws BikeshareException {
        executar(() -> penalidades.lancarPenalidade(usuario, tipo, valor, motivo));
    }

    // =====================================================================
    // US14 - Manutencao
    // =====================================================================

    public void lancaDefeito(String bicicleta, String descricao) throws BikeshareException {
        executar(() -> manutencoes.lancarDefeito(bicicleta, descricao));
    }

    public void concluiManutencao(String bicicleta) throws BikeshareException {
        executar(() -> manutencoes.concluirManutencao(bicicleta));
    }

    // =====================================================================
    // US15 - Relatorios
    // =====================================================================

    public String getTotalFaturado(String dataInicial, String dataFinal) throws BikeshareException {
        return relatorios.getTotalFaturado(dataInicial, dataFinal);
    }

    // =====================================================================
    // US16 - Undo / redo
    // =====================================================================

    /** Desfaz o ultimo comando bem-sucedido, inclusive o de zerar o sistema. */
    public void undo() throws BikeshareException {
        trocarSistema(historico.desfazer(sistema));
    }

    /** Reaplica o comando desfeito mais recentemente. */
    public void redo() throws BikeshareException {
        trocarSistema(historico.refazer(sistema));
    }

    // =====================================================================
    // Ciclo de vida do sistema
    // =====================================================================

    /**
     * Descarta todo o estado, em memoria e em disco.
     *
     * <p>Zerar o sistema e um comando como os demais: o estado anterior vai para o
     * historico e pode ser recuperado com um desfazer.</p>
     */
    public void zerarSistema() {
        historico.registrar(Memento.capturar(sistema));
        trocarSistema(new SistemaBikeshare());
        RepositorioSistema.apagar();
    }

    /** Grava o estado atual para que a proxima execucao continue de onde parou. */
    public void encerrarSistema() {
        RepositorioSistema.salvar(sistema);
    }
}
