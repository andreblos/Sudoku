package util;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class SudokuGenerator {

    private SudokuGenerator() {}

    /** Gera um mapa "linha,coluna" -> "valor,fixed" com nº ideal aleatório de dicas. */
    public static Map<String, String> generatePositions(int size) {
        int clues = chooseIdealClueCount(size);
        return generatePositions(size, clues);
    }

    /** Gera um mapa com quantidade de dicas alvo (clues). Mantém unicidade da solução. */
    public static Map<String, String> generatePositions(int size, int clues) {
        int[][] grid = new int[size][size];

        if (!fillSolution(grid, size)) {
            throw new IllegalStateException("Falha ao gerar solução do Sudoku.");
        }

        digHolesUnique(grid, size, clues);

        // Converte para o formato esperado: "r,c" -> "valor,fixed"
        Map<String, String> out = new HashMap<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int v = grid[r][c];
                if (v == 0) {
                    out.put(r + "," + c, "0,false");
                } else {
                    out.put(r + "," + c, v + ",true");
                }
            }
        }
        return out;
    }

    /** Escolhe um nº “ideal” aleatório de dicas (30% a 45% das casas). */
    public static int chooseIdealClueCount(int size) {
        int total = size * size;
        int min = (int) Math.ceil(total * 0.30);
        int max = (int) Math.ceil(total * 0.45);

        // Para 9x9, respeita o mínimo teórico conhecido de 17
        if (size == 9) min = Math.max(min, 17);

        if (min > max) { // fallback
            min = Math.max(17, total / 3);
            max = Math.max(min, total / 2);
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /* ===================== Gerador da solução (backtracking) ===================== */

    private static boolean fillSolution(int[][] grid, int size) {
        int[] cell = findEmpty(grid, size);
        if (cell == null) return true; // sem vazios -> solução pronta

        int r = cell[0], c = cell[1];
        List<Integer> nums = shuffled1toN(size);

        for (int n : nums) {
            if (isSafe(grid, size, r, c, n)) {
                grid[r][c] = n;
                if (fillSolution(grid, size)) return true;
                grid[r][c] = 0;
            }
        }
        return false;
    }

    private static int[] findEmpty(int[][] grid, int size) {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] == 0) return new int[]{r, c};
        return null;
    }

    private static List<Integer> shuffled1toN(int n) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) list.add(i);
        Collections.shuffle(list);
        return list;
    }

    private static boolean isSafe(int[][] grid, int size, int r, int c, int n) {
        // Linha e coluna
        for (int i = 0; i < size; i++) {
            if (grid[r][i] == n) return false;
            if (grid[i][c] == n) return false;
        }

        // Sub-bloco se size é quadrado perfeito
        int g = group(size);
        if (g > 1) {
            int r0 = (r / g) * g;
            int c0 = (c / g) * g;
            for (int i = 0; i < g; i++) {
                for (int j = 0; j < g; j++) {
                    if (grid[r0 + i][c0 + j] == n) return false;
                }
            }
        }
        return true;
    }

    private static int group(int size) {
        int r = (int) Math.round(Math.sqrt(size));
        return r * r == size ? r : 1;
    }

    /* ============== Remoção de células preservando unicidade da solução ============== */

    private static void digHolesUnique(int[][] grid, int size, int targetClues) {
        int total = size * size;
        int toRemove = Math.max(0, total - targetClues);

        List<int[]> cells = new ArrayList<>(total);
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                cells.add(new int[]{r, c});

        Collections.shuffle(cells);

        for (int[] cell : cells) {
            if (toRemove <= 0) break;

            int r = cell[0], c = cell[1];
            int backup = grid[r][c];
            if (backup == 0) continue;

            grid[r][c] = 0;

            // Testa unicidade: conta soluções até 2
            int solutions = countSolutions(copyOf(grid), size, 2);
            if (solutions != 1) {
                // Reverte (perdeu unicidade)
                grid[r][c] = backup;
            } else {
                toRemove--;
            }
        }
        // Se não conseguiu remover o suficiente mantendo unicidade,
        // aceita mais dicas (puzzle continua válido, só um pouco mais fácil).
    }

    private static int[][] copyOf(int[][] grid) {
        int[][] cp = new int[grid.length][grid.length];
        for (int i = 0; i < grid.length; i++)
            cp[i] = Arrays.copyOf(grid[i], grid[i].length);
        return cp;
    }

    /** Conta soluções (backtracking), parando ao atingir 'limit'. */
    private static int countSolutions(int[][] grid, int size, int limit) {
        return countSolutionsRec(grid, size, limit, 0);
    }

    private static int countSolutionsRec(int[][] grid, int size, int limit, int found) {
        if (found >= limit) return found;

        int[] cell = findEmpty(grid, size);
        if (cell == null) return found + 1; // achou 1 solução

        int r = cell[0], c = cell[1];
        // Ordem fixa aqui torna o teste de unicidade mais determinístico
        for (int n = 1; n <= size; n++) {
            if (isSafe(grid, size, r, c, n)) {
                grid[r][c] = n;
                found = countSolutionsRec(grid, size, limit, found);
                grid[r][c] = 0;
                if (found >= limit) break;
            }
        }
        return found;
    }
}
