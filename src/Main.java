import model.Board;
import model.Space;
import util.BoardTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static model.GameStatusEnum.COMPLETE;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static Board board;
    private static final int BOARD_LIMIT = 9;

    public static void main(String[] args) {

        final Map<String, String> positions = Stream.of(args)
                .map(s -> s.replace(" ", "")) // tolera espaços
                .collect(toMap(
                        k -> k.split(";")[0],   // "linha,coluna"
                        v -> v.split(";")[1]    // "valor,fixed"
                ));

        while (true) {
            System.out.println("Selecione uma das opções a seguir");
            System.out.println("1 - Iniciar um novo jogo");
            System.out.println("2 - Colocar um novo número");
            System.out.println("3 - Remover um número");
            System.out.println("4 - Visualizar jogo atual");
            System.out.println("5 - Veirificar status do jogo");
            System.out.println("6 - Limpar jogo");
            System.out.println("7 - Finalizar jogo");
            System.out.println("8 - Sair");

            int option = scanner.nextInt();

            switch (option) {
                case 1 -> startGame(positions);
                case 2 -> inputNumber();
                case 3 -> removeNumber();
                case 4 -> showCurrentGame();
                case 5 -> showGameStatus();
                case 6 -> clearGame();
                case 7 -> finishGame();
                case 8 -> System.exit(0);
                default -> System.out.println("Opção inválida! Selecione uma das opções do Menu.");
            }
        }
    }

    private static void startGame(Map<String, String> positions) {
        if (nonNull(board)) {
            System.out.println("O jogo já foi iniciado!");
            return;
        }
        List<List<Space>> spaces = new ArrayList<>();
        for (int i = 0; i < BOARD_LIMIT; i++) {
            spaces.add(new ArrayList<>());
            for (int j = 0; j < BOARD_LIMIT; j++) {
                String key1 = i + "," + j;
                String key2 = i + ", " + j;
                String positionConfig = positions.getOrDefault(key1,
                        positions.getOrDefault(key2, "0,false")); // default vazio
                String[] parts = positionConfig.split(",");
                int expected = Integer.parseInt(parts[0].trim());
                boolean fixed = Boolean.parseBoolean(parts[1].trim());
                Space currentSpace = new Space(expected, fixed);
                spaces.get(i).add(currentSpace);
            }
        }
        board = new Board(spaces);
        System.out.println("O jogo foi iniciado!");
    }

    private static void inputNumber() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado");
            return;
        }
        System.out.println("Informe a coluna em que o número será inserido: ");
        int col = runUntilGetValidNumber(0, BOARD_LIMIT - 1);
        System.out.println("Informe a linha em que o número será inserido: ");
        int row = runUntilGetValidNumber(0, BOARD_LIMIT - 1);
        System.out.printf("Informe o número que vai entrar na posição [%s, %s]%n", col, row);
        int value = runUntilGetValidNumber(1, BOARD_LIMIT);
        if (!board.changeValue(col, row, value)) {
            System.out.printf("A posição [%s, %s] tem um valor fixo %n", col, row);
        }
    }

    private static void removeNumber() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado");
            return;
        }
        System.out.println("Informe a coluna do número a remover: ");
        int col = runUntilGetValidNumber(0, BOARD_LIMIT - 1);
        System.out.println("Informe a linha do número a remover: ");
        int row = runUntilGetValidNumber(0, BOARD_LIMIT - 1);
        if (!board.clearValue(col, row)) {
            System.out.printf("A posição [%s, %s] tem um valor fixo %n", col, row);
        }
    }

    private static void showCurrentGame() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }
        System.out.println("O seu jogo se encontra da seguinte forma: ");
        // usa a classe util.BoardTemplate (nada de BOARD_TEMPLATE aqui)
        System.out.print(BoardTemplate.render(board, BOARD_LIMIT));    }

    private static void finishGame() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }
        // substitui gameIsFinished() por checagem de status
        if (board.getStatus() == COMPLETE) {
            System.out.println("Parabéns você concluiu o jogo");
            showCurrentGame();
            board = null;
        } else if (board.hasErrors()) {
            System.out.println("Seu jogo contém erros, verifique seu board e ajuste-o");
        } else {
            System.out.println("Você ainda precisa preencher algum espaço");
        }
    }

    private static void clearGame() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }
        System.out.println("Tem certeza que deseja limpar o jogo e perder todo seu progresso? (sim/não)");
        String confirm = scanner.next();
        while (!confirm.equalsIgnoreCase("sim") && !confirm.equalsIgnoreCase("não")) {
            System.out.println("Informe 'sim' ou 'não'");
            confirm = scanner.next();
        }
        if (confirm.equalsIgnoreCase("sim")) {
            board.reset();
        }
    }

    private static void showGameStatus() {
        if (isNull(board)) {
            System.out.println("O jogo ainda não foi iniciado.");
            return;
        }
        System.out.printf("O jogo atualmente se encontra no status: %s%n", board.getStatus().getLabel());
        if (board.hasErrors()) {
            System.out.println("O jogo contém erros.");
        } else {
            System.out.println("O jogo não contém erros");
        }
    }

    private static int runUntilGetValidNumber(final int min, final int max) {
        int current = scanner.nextInt();
        while (current < min || current > max) {
            System.out.printf("Informe um número entre %s e %s%n", min, max);
            current = scanner.nextInt();
        }
        return current;
    }
}
