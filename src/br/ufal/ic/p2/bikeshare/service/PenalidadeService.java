package br.ufal.ic.p2.bikeshare.service;

import br.ufal.ic.p2.bikeshare.exceptions.BikeshareException;
import br.ufal.ic.p2.bikeshare.exceptions.Mensagens;
import br.ufal.ic.p2.bikeshare.exceptions.ValidacaoException;
import br.ufal.ic.p2.bikeshare.model.penalidade.Penalidade;
import br.ufal.ic.p2.bikeshare.model.penalidade.PenalidadeDano;
import br.ufal.ic.p2.bikeshare.model.penalidade.PenalidadeUsoIndevido;
import br.ufal.ic.p2.bikeshare.model.usuario.Usuario;
import br.ufal.ic.p2.bikeshare.util.TextoUtil;

import java.math.BigDecimal;

/**
 * Lancamento manual de penalidades pelo operador (US13).
 *
 * <p>Apenas as penalidades cujo valor o operador apura podem ser lancadas por aqui; a
 * penalidade de atraso e automatica e so nasce da expiracao de um aluguel, por isso
 * nao e aceita como tipo neste comando.</p>
 */
public class PenalidadeService {

    private final UsuarioService usuarios;

    public PenalidadeService(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    public void lancarPenalidade(String idUsuario, String tipo, String valor, String motivo)
            throws BikeshareException {
        Usuario usuario = usuarios.exigirUsuario(idUsuario);

        String tipoInformado = TextoUtil.limpar(tipo);
        if (!"dano".equals(tipoInformado) && !"usoIndevido".equals(tipoInformado)) {
            throw new ValidacaoException(Mensagens.TIPO_PENALIDADE_INVALIDO);
        }

        BigDecimal valorPenalidade = ValidadorEntrada.exigirDecimal(valor,
                Mensagens.VALOR_NULO, Mensagens.VALOR_NAO_NUMERICO);
        ValidadorEntrada.exigirPositivo(valorPenalidade, Mensagens.VALOR_NAO_POSITIVO);
        ValidadorEntrada.exigirPreenchido(motivo, Mensagens.MOTIVO_NULO);

        Penalidade penalidade = "dano".equals(tipoInformado)
                ? new PenalidadeDano(valorPenalidade, TextoUtil.limpar(motivo))
                : new PenalidadeUsoIndevido(valorPenalidade, TextoUtil.limpar(motivo));
        usuario.lancarPenalidade(penalidade);
    }
}
