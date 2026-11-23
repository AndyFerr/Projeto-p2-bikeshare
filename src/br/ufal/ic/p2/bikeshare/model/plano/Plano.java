package br.ufal.ic.p2.bikeshare.model.plano;

import br.ufal.ic.p2.bikeshare.config.ParametrosSistema;
import br.ufal.ic.p2.bikeshare.util.NumeroUtil;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Modalidade de cobranca contratada pelo usuario.
 *
 * <p>Um plano e descrito por tres numeros: quantos minutos de cada corrida sao
 * gratuitos (franquia), quanto custa cada minuto alem da franquia e qual a
 * mensalidade. O plano <em>avulso</em> e o caso degenerado &mdash; franquia zero e
 * nenhuma mensalidade &mdash;, de modo que a mesma formula serve para avulsos,
 * assinantes e para os planos criados em tempo de execucao pela US12.</p>
 */
public class Plano implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String AVULSO = "avulso";
    public static final String ASSINANTE = "assinante";

    private final String nome;
    private final int franquiaMinutos;
    private final BigDecimal valorMinutoExcedente;

    /** Mensalidade do plano; {@code null} em planos que nao cobram mensalidade. */
    private final BigDecimal mensalidade;

    public Plano(String nome, int franquiaMinutos, BigDecimal valorMinutoExcedente, BigDecimal mensalidade) {
        this.nome = nome;
        this.franquiaMinutos = franquiaMinutos;
        this.valorMinutoExcedente = NumeroUtil.arredondar(valorMinutoExcedente);
        this.mensalidade = mensalidade == null ? null : NumeroUtil.arredondar(mensalidade);
    }

    /** Plano avulso: cobra todos os minutos e nao tem mensalidade. */
    public static Plano avulso() {
        return new Plano(AVULSO, ParametrosSistema.franquiaAvulso(),
                ParametrosSistema.valorMinutoAvulso(), null);
    }

    /** Plano assinante: franquia gratuita por corrida e mensalidade padrao. */
    public static Plano assinante() {
        return new Plano(ASSINANTE, ParametrosSistema.franquiaAssinante(),
                ParametrosSistema.valorMinutoExcedenteAssinante(),
                ParametrosSistema.mensalidadeAssinante());
    }

    public String getNome() {
        return nome;
    }

    public int getFranquiaMinutos() {
        return franquiaMinutos;
    }

    public BigDecimal getValorMinutoExcedente() {
        return valorMinutoExcedente;
    }

    public boolean possuiMensalidade() {
        return mensalidade != null;
    }

    public BigDecimal getMensalidade() {
        return mensalidade;
    }

    /**
     * Valor devido pelo tempo de uso: apenas os minutos que ultrapassam a franquia,
     * ao preco do minuto excedente do plano.
     */
    public BigDecimal calcularValorPorTempo(long minutos) {
        long excedentes = Math.max(0L, minutos - franquiaMinutos);
        return NumeroUtil.arredondar(valorMinutoExcedente.multiply(BigDecimal.valueOf(excedentes)));
    }
}
