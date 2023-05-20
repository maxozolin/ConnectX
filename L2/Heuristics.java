package connectx.L2;

import connectx.CXCellState;
import connectx.extension.CellCoord;
import connectx.extension.Streak;
import connectx.extension.StreakBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Heuristics {
    public static boolean evenPosition(StreakBoard board) {
        int c1 = estimateConnectivity(board, CXCellState.P1);
        int c2 = estimateConnectivity(board, CXCellState.P2);

        return Math.abs(c1-c2) < 40;
    }

    public static int score(StreakBoard board, CXCellState player, CXCellState playingPlayer) {
        final List<Streak> myStreaks;
        final List<Streak> opponentStreaks;

        CXCellState opponent;
        if (player == CXCellState.P1) {
            opponent = CXCellState.P2;

            myStreaks = board.getStreaksP1();
            opponentStreaks = board.getStreaksP2();

        } else {
            myStreaks = board.getStreaksP2();

            opponent = CXCellState.P1;
            opponentStreaks = board.getStreaksP1();
        }

        if (playingPlayer == player) {
            if (checkDoubleAttack(board, myStreaks)) {
                return 200000000;
            }
            if (heuristicNMoveWins(board, myStreaks, 1)) {
                return 100000000;
            }
            if (checkDoubleAttack(board, opponentStreaks)) {
                return -200000000;
            }
            // return a more heuritic eval

        } else {
            if (heuristicNMoveWins(board, opponentStreaks, 1)) {
                return -100000000;
            }
            if (checkDoubleAttack(board, myStreaks)) {
                return 200000000;
            }
        }
        return estimateConnectivity(board, player) - estimateConnectivity(board, opponent);
    }


    // Scopi di debugging
    // static int count = 0;

    public static boolean heuristicNMoveWins(StreakBoard sb,
                                              List<Streak> playerStreaks,
                                              int n) {

        int streaksCount = 0;
        for (Streak streak : playerStreaks) {
            int count = 0;
            int multiplier = 0;
            if (!streak.isValid()) {
                continue;
            }
            for (CellCoord cell : streak.getCells()) {
                if (cell.getState() == streak.state) {
                    count++;
                }
            }
            if (count == sb.X - 1) {
                for (CellCoord cell : streak.getCells()) {
                    if (cell.getState() == CXCellState.FREE) {
                        // Check if a move can be made on here.
                        if (cell.i == sb.M-1 ||
                                sb.getBoard()[cell.i+1][cell.j] != CXCellState.FREE) {

                            multiplier = 1;
                        }
                    }
                }
                streaksCount += multiplier;
            }
        }
        // System.out.println("There is a double attack for P2");
        return streaksCount >= n;
    }
    public static boolean checkDoubleAttack(StreakBoard sb,
                                      List<Streak> playerStreaks) {

        return heuristicNMoveWins(sb, playerStreaks, 2);
    }

    /**
     * Questo metodo elabora un punteggio legato alla connettività in una posizione per un
     * determinato giocatore.
     *
     * Tanto più il punteggio è alto, più è considerata 'buona' in termini di connettività,
     * la posizione di questo giocatore.
     *
     * Questo metodo NON tiene conto della connettività dell'avversario nella stessa posizione.
     *
     * @param board scacchiera
     * @param player giocatore di cui calcolare la connettività
     *
     * @return euristica di connettività, da comparare con altre
     * posizioni possibili oppure con l'avversario nella stessa posizione.
     */
    public static int estimateConnectivity(StreakBoard board, CXCellState player) {

        List<Streak> streaks;

        if (player == CXCellState.P1) {
            streaks = board.getStreaksP1();

        } else {
            streaks = board.getStreaksP2();
        }

        streaks.forEach(Streak::checkValidity);

        final int validStreaksNumber = Streak.numberOfValidStreaks(streaks);

        final List<Streak> validStreaks = streaks.stream().filter(Streak::isValid).toList();

        int[] streaksScores = new int[validStreaks.size()];

        int streakMinus2Count = 0;
        int streakMinus1Count = 0;
        int streakFull = 0;
        // System.out.println("[DEBUG] validStreaks number: " + validStreaks.size());
        for (int i = 0; i < validStreaks.size(); i++) {
            int streakScore = 1;
            Streak streak = validStreaks.get(i);

            int multiplier = 1;

            // multiplier += ((streak.colEnd - streak.colStart) / 2) * 3;
            int count = 0;
            for (CellCoord cell : streak.getCells()) {
                if (cell.getState() != CXCellState.FREE) {
                    multiplier += 0;
                    if (cell.getState() != player) {
                        // System.out.println("Evento che non dovrebe mai accadere: " + streak.isValid());
                        // multiplier += 1;
                        // continue;
                    }


                    if (cell.getState() == player) {
                        // multiplier += 2;
                        count++;
                    }
                }
            }
            if (count == board.X - 3) {
                multiplier = 5;
            }
            if (count == board.X - 2) {
                multiplier = 10; // 30
                streakMinus2Count++;
            }
            if (count == board.X - 1) {
                // System.out.println("Big multiplier");
                multiplier = 20; // 100
                streakMinus1Count++;
            }
            if (count == streakFull) {
                streakFull++;
            }
            streakScore = streakScore * multiplier;

            streaksScores[i] = streakScore;
        }

        List<Integer> skipList = new ArrayList<>();
        for (int i = 0; i < validStreaks.size(); i++) {
            if (skipList.contains(i)) {
                continue;
            }
            for (int j = 0; j < validStreaks.size()/2; j++) {
                if (i == j) {
                    continue;
                }
                if (skipList.contains(i)) {
                    continue;
                }

                if (Streak.doIntersect(validStreaks.get(i), validStreaks.get(j))) {
                    streaksScores[i] = (int)((float)streaksScores[i] * 1);

                    skipList.add(i);
                }
            }
        }

        int bonusScore = 0;
        if (streakMinus1Count == 1) {
            bonusScore = 100;
        } else if (streakMinus1Count == 2) {
            bonusScore = 300;
        } else if (streakMinus1Count == 3) {
            bonusScore = 500;
        }

        if (streakFull > 0) {
            return 10000000 * streakFull;
        }

        return validStreaksNumber*4 +
                streakMinus2Count * 2
                + bonusScore;

        //return IntStream.of(streaksScores).sum(); // validStreaksNumber * 2
                // + streakMinus2Count * 2
                // + bonusScore;

        /*IntStream.of(streaksScores).sum()
                + validStreaksNumber*2
                + streakMinus2Count * 20
                + streakMinus1Count * 100;*/
    }
}
