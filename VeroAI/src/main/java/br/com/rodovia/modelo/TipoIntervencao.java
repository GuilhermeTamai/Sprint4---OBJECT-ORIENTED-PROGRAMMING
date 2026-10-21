package br.com.rodovia.modelo;

/** Tipos de intervencao aceitos. O nome do enum e o valor gravado em tipo_intervencao. */
public enum TipoIntervencao {
    ROCADA_MECANIZADA,
    PULVERIZACAO;

    public static TipoIntervencao deTexto(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return TipoIntervencao.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
