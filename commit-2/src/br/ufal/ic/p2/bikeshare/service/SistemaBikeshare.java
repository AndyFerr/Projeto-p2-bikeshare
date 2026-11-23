package br.ufal.ic.p2.bikeshare.service;

import br.ufal.ic.p2.bikeshare.model.bicicleta.Bicicleta;
import br.ufal.ic.p2.bikeshare.model.estacao.Estacao;
import br.ufal.ic.p2.bikeshare.model.plano.Plano;
import br.ufal.ic.p2.bikeshare.model.usuario.Usuario;
import br.ufal.ic.p2.bikeshare.util.TextoUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Estado completo do BIKESHARE.
 *
 * <p>Guarda as entidades cadastradas, os planos de tarifacao disponiveis e os
 * contadores que geram os identificadores. E a unica classe serializada: persistir o
 * sistema entre execucoes (BS03) e tirar um retrato dele para o desfazer/refazer
 * (US16) significam, os dois, copiar este objeto.</p>
 */
public class SistemaBikeshare implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();
    private final Map<String, Estacao> estacoes = new LinkedHashMap<>();
    private final Map<String, Bicicleta> bicicletas = new LinkedHashMap<>();
    private final Map<String, Plano> planos = new LinkedHashMap<>();

    private int proximoIdUsuario = 1;
    private int proximoIdEstacao = 1;

    public SistemaBikeshare() {
        registrarPlano(Plano.avulso());
        registrarPlano(Plano.assinante());
    }

    // ---- usuarios ------------------------------------------------------

    public Map<String, Usuario> getUsuarios() {
        return usuarios;
    }

    public Usuario buscarUsuario(String id) {
        return usuarios.get(TextoUtil.limpar(id));
    }

    /** Procura um usuario pelo email, ignorando diferenca de caixa. */
    public Usuario buscarUsuarioPorEmail(String email) {
        String chave = TextoUtil.chaveEmail(email);
        for (Usuario usuario : usuarios.values()) {
            if (TextoUtil.chaveEmail(usuario.getEmail()).equals(chave)) {
                return usuario;
            }
        }
        return null;
    }

    public void adicionarUsuario(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
    }

    public void removerUsuario(String id) {
        usuarios.remove(id);
    }

    /**
     * Gera o proximo identificador de usuario.
     *
     * <p>So e chamado depois que o cadastro passa por todas as validacoes, de modo que
     * tentativas recusadas nao consomem identificadores e a numeracao permanece
     * sequencial; o contador tambem nunca recua, entao ids de usuarios removidos nao
     * sao reaproveitados.</p>
     */
    public String gerarIdUsuario() {
        return "U" + proximoIdUsuario++;
    }

    // ---- estacoes ------------------------------------------------------

    public Map<String, Estacao> getEstacoes() {
        return estacoes;
    }

    public Estacao buscarEstacao(String id) {
        return estacoes.get(TextoUtil.limpar(id));
    }

    public void adicionarEstacao(Estacao estacao) {
        estacoes.put(estacao.getId(), estacao);
    }

    public void removerEstacao(String id) {
        estacoes.remove(id);
    }

    /** Gera o proximo identificador de estacao, em numeracao propria e sequencial. */
    public String gerarIdEstacao() {
        return "E" + proximoIdEstacao++;
    }

    // ---- bicicletas ----------------------------------------------------

    public Map<String, Bicicleta> getBicicletas() {
        return bicicletas;
    }

    public Bicicleta buscarBicicleta(String id) {
        return bicicletas.get(TextoUtil.limpar(id));
    }

    public void adicionarBicicleta(Bicicleta bicicleta) {
        bicicletas.put(bicicleta.getId(), bicicleta);
    }

    public void removerBicicleta(String id) {
        bicicletas.remove(id);
    }

    // ---- planos --------------------------------------------------------

    public Map<String, Plano> getPlanos() {
        return planos;
    }

    public Plano buscarPlano(String nome) {
        return planos.get(TextoUtil.limpar(nome));
    }

    public void registrarPlano(Plano plano) {
        planos.put(plano.getNome(), plano);
    }
}
