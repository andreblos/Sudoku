package util;

import model.Board;
import model.Space;

import java.util.List;

public final class BoardTemplate {

    private BoardTemplate() {}

    /** Renderiza o board com índices e bordas (externas duplas, internas simples). */
    public static String render(Board board, int size) {
        if (board == null || board.getSpaces() == null) {
            return "O jogo ainda não foi iniciado.\n";
        }
        return render(board.getSpaces(), size);
    }

    public static String render(List<List<Space>> spaces, int size) {
        if (!isValid(spaces, size)) return "Tabuleiro inválido.\n";

        // largura do dígito (1 para 1..9, 2 para 1..16, etc.)
        final int digitWidth = Math.max(1, String.valueOf(size).length());
        // padding lateral dentro da célula (aumente para deixar maior)
        final int padding = 2; // <<< ajuste aqui para “maiores”
        // largura efetiva da célula (conteúdo central + paddings)
        final int effCell = digitWidth + 2 * padding;
        // largura do índice de linha (0..size-1)
        final int rowIdxWidth = Math.max(1, String.valueOf(size - 1).length());
        final int group = group(size); // 3 para 9x9, 4 para 16x16, 1 caso contrário

        StringBuilder out = new StringBuilder();

        // Linha de índices de coluna (topo), alinhada ao centro das células
        out.append(columnIndexLine(size, effCell, rowIdxWidth, digitWidth, padding)).append('\n');

        // Borda superior (externa dupla)
        out.append(topBorder(size, effCell, rowIdxWidth)).append('\n');

        for (int r = 0; r < size; r++) {
            // linha de conteúdo
            out.append(rowLine(spaces, r, size, effCell, rowIdxWidth, digitWidth, padding)).append('\n');

            // separador abaixo da linha r:
            boolean atBlock = (group > 1) && ((r + 1) % group == 0);
            if (atBlock) {
                // linha dupla (destaca sub-bloco)
                out.append(doubleInnerSep(size, effCell, rowIdxWidth)).append('\n');
            } else {
                // linha simples normal
                out.append(singleInnerSep(size, effCell, rowIdxWidth)).append('\n');
            }
        }

        // Linha de índices de coluna (base)
        out.append(columnIndexLine(size, effCell, rowIdxWidth, digitWidth, padding)).append('\n');

        return out.toString();
    }

    /* ======================== linhas auxiliares ======================== */

    private static String rowLine(List<List<Space>> spaces, int r, int size,
                                  int effCell, int rowIdxWidth, int digitWidth, int padding) {
        StringBuilder sb = new StringBuilder();

        // índice da linha (esquerda)
        sb.append(padLeft(String.valueOf(r), rowIdxWidth)).append(' ').append('║');

        for (int c = 0; c < size; c++) {
            Integer val = spaces.get(r).get(c).getActual();
            String cell = (val == null) ? "" : val.toString();
            sb.append(repeat(' ', padding))
                    .append(center(cell, digitWidth))
                    .append(repeat(' ', padding));

            // separador entre células (interno simples) ou borda direita dupla
            if (c < size - 1) sb.append('│');
            else sb.append('║');
        }

        // índice da linha (direita)
        sb.append(' ').append(padLeft(String.valueOf(r), rowIdxWidth));

        return sb.toString();
    }

    private static String topBorder(int size, int effCell, int rowIdxWidth) {
        StringBuilder sb = new StringBuilder();
        sb.append(repeat(' ', rowIdxWidth)).append(' ').append('╔');
        for (int c = 0; c < size; c++) {
            sb.append(repeat('═', effCell));
            if (c < size - 1) sb.append('╤'); // junção dupla-horizontal com vertical interna simples
            else sb.append('╗');
        }
        return sb.toString();
    }

    private static String singleInnerSep(int size, int effCell, int rowIdxWidth) {
        // linha horizontal simples cruzando colunas internas simples e bordas externas duplas
        StringBuilder sb = new StringBuilder();
        sb.append(repeat(' ', rowIdxWidth)).append(' ').append('╟');
        for (int c = 0; c < size; c++) {
            sb.append(repeat('─', effCell));
            if (c < size - 1) sb.append('┼');
            else sb.append('╢');
        }
        return sb.toString();
    }

    private static String doubleInnerSep(int size, int effCell, int rowIdxWidth) {
        // linha horizontal dupla (para limites de sub-bloco) cruzando verticais internas simples
        StringBuilder sb = new StringBuilder();
        sb.append(repeat(' ', rowIdxWidth)).append(' ').append('╠');
        for (int c = 0; c < size; c++) {
            sb.append(repeat('═', effCell));
            if (c < size - 1) sb.append('╪'); // dupla-horizontal com vertical simples
            else sb.append('╣');
        }
        return sb.toString();
    }

    private static String bottomBorder(int size, int effCell, int rowIdxWidth) {
        StringBuilder sb = new StringBuilder();
        sb.append(repeat(' ', rowIdxWidth)).append(' ').append('╚');
        for (int c = 0; c < size; c++) {
            sb.append(repeat('═', effCell));
            if (c < size - 1) sb.append('╧'); // junção inferior (dupla x simples)
            else sb.append('╝');
        }
        return sb.toString();
    }

    private static String columnIndexLine(int size, int effCell, int rowIdxWidth,
                                          int digitWidth, int padding) {
        // Para alinhar, usamos o mesmo deslocamento do início do conteúdo:
        // [rowIdxWidth] + espaço + (largura da borda esquerda '║') + 'padding' espaços
        // Como não desenhamos a borda nesta linha, simulamos com espaços.
        StringBuilder sb = new StringBuilder();
        int indent = rowIdxWidth + 1 /*espaço*/ + 1 /*simula '║'*/ + padding;
        sb.append(repeat(' ', indent));

        for (int c = 0; c < size; c++) {
            sb.append(center(String.valueOf(c), digitWidth))
                    .append(repeat(' ', padding));         // completa lado direito do conteúdo
            sb.append(repeat(' ', padding));         // completa lado esquerdo da próxima célula
            if (c < size - 1) sb.append(' ');        // reserva 1 coluna do separador vertical
        }
        return sb.toString();
    }

    /* ======================== utilitários ======================== */

    private static boolean isValid(List<List<Space>> spaces, int size) {
        if (spaces == null || spaces.size() != size) return false;
        for (int r = 0; r < size; r++) {
            if (spaces.get(r) == null || spaces.get(r).size() != size) return false;
        }
        return true;
    }

    private static int group(int size) {
        int r = (int) Math.round(Math.sqrt(size));
        return (r * r == size) ? r : 1;
    }

    private static String repeat(char ch, int n) {
        StringBuilder sb = new StringBuilder(Math.max(n, 0));
        for (int i = 0; i < n; i++) sb.append(ch);
        return sb.toString();
    }

    private static String padLeft(String s, int width) {
        if (s == null) s = "";
        int pad = width - s.length();
        if (pad <= 0) return s;
        StringBuilder sb = new StringBuilder(width);
        for (int i = 0; i < pad; i++) sb.append(' ');
        return sb.append(s).toString();
    }

    private static String center(String s, int width) {
        if (s == null) s = "";
        int pad = width - s.length();
        if (pad <= 0) return s;
        int left = pad / 2;
        int right = pad - left;
        return repeat(' ', left) + s + repeat(' ', right);
    }
}
