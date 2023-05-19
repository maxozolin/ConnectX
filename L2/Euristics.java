package connectx.L2;

import connectx.CXCell;
import connectx.CXCellState;
import connectx.extension.Streak;
import connectx.extension.StreakBoard;

import java.util.List;
import java.util.stream.IntStream;

public class Euristics {
    public static boolean evenPosition(StreakBoard board) {
        int c1 = estimateConnectivity(board, CXCellState.P1);
        int c2 = estimateConnectivity(board, CXCellState.P2);

        return Math.abs(c1-c2) < 20;
    }
/*
    public static int estimateDominance(StreakBoard board, CXCellState player) {
        CXCellState opponent;

        if (player == CXCellState.P1) {
            opponent = CXCellState.P2;
        } else {
            opponent = CXCellState.P1;
        }

        return -estimateConnectivity(board, opponent);
    }*/

    public static int score(StreakBoard board, CXCellState player, CXCellState playingPlayer) {
        CXCellState opponent;
        if (player == CXCellState.P1) {
            opponent = CXCellState.P2;
        } else {
            opponent = CXCellState.P1;
        }
        int score = estimateConnectivity(board, player) - estimateConnectivity(board, opponent);

        if (playingPlayer == player) {
            score += 10;
        } else {
            score -= 10;
        }
        // 10 numero "magico" perché il giocatore che ha il turno è avvantaggiato

        return score;
    }
    public static int estimateConnectivity(StreakBoard board, CXCellState player) {
        // Euristica:
        // +1 per ogni streak valida
        // +5 per ogni pedina in una streak
        // Se due scie hanno intersezione, il loro valore vale doppio

        List<Streak> streaks;

        if (player == CXCellState.P1) {
            streaks = board.getStreaksP1();

        } else {
            streaks = board.getStreaksP2();
        }

        final int validStreaksNumber = Streak.numberOfValidStreaks(streaks);

        final List<Streak> validStreaks = streaks.stream().filter(Streak::isValid).toList();

        int[] streaksScores = new int[validStreaks.size()];

        for (int i = 0; i < validStreaks.size(); i++) {
            int score = 0;
            Streak streak = validStreaks.get(i);

            for (CXCell cell : streak.getCells()) {
                if (cell.state != CXCellState.FREE) {
                    score += 5;
                }
            }
            streaksScores[i] = score;
        }

        for (int i = 0; i < validStreaks.size(); i++) {
            for (int j = 0; j < validStreaks.size()/2; j++) {
                if (i == j) {
                    continue;
                }

                if (Streak.doIntersect(validStreaks.get(i), validStreaks.get(j))) {
                    streaksScores[i] *= 2;
                }
            }
        }

        return IntStream.of(streaksScores).sum() + validStreaksNumber;
    }
}
