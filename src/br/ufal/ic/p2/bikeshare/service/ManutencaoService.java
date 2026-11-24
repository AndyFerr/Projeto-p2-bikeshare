package br.ufal.ic.p2.bikeshare.service;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.RegraDeNegocioException;
import br.ufal.ic.p2.bikeshare.model.bicicleta.Bicicleta;
import br.ufal.ic.p2.bikeshare.model.bicicleta.EstadoBicicleta;
import br.ufal.ic.p2.bikeshare.util.TextoUtil;

/**
 * Lancamento de defeitos e conclusao de manutencoes (US14).
 *
 * <p>Manutencao e estado operacional, nao posicao: a bicicleta em manutencao continua
 * ocupando a vaga da estacao onde esta, apenas deixa de ser contada como disponivel e
 * nao pode ser retirada nem removida do parque.</p>
 */
public class ManutencaoService {

    private final EstacaoService estacoes;

    public ManutencaoService(EstacaoService estacoes) {
        this.estacoes = estacoes;
    }

    public void lancarDefeito(String idBicicleta, String descricao) throws BikeshareException {
        Bicicleta bicicleta = estacoes.exigirBicicleta(idBicicleta);
        ValidadorEntrada.exigirPreenchido(descricao, Mensagens.DESCRICAO_NULA);

        if (bicicleta.getEstado() == EstadoBicicleta.EM_MANUTENCAO) {
            throw new RegraDeNegocioException(Mensagens.BICICLETA_JA_EM_MANUTENCAO);
        }
        if (bicicleta.getEstado() == EstadoBicicleta.EM_USO) {
            throw new RegraDeNegocioException(Mensagens.DEFEITO_EM_BICICLETA_EM_USO);
        }
        bicicleta.registrarDefeito(TextoUtil.limpar(descricao));
    }

    public void concluirManutencao(String idBicicleta) throws BikeshareException {
        Bicicleta bicicleta = estacoes.exigirBicicleta(idBicicleta);
        if (bicicleta.getEstado() != EstadoBicicleta.EM_MANUTENCAO) {
            throw new RegraDeNegocioException(Mensagens.BICICLETA_NAO_EM_MANUTENCAO);
        }
        bicicleta.concluirManutencao();
    }
}
