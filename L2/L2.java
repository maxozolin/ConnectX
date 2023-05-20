package connectx.L2;

import connectx.*;
import connectx.extension.CellCoord;
import connectx.extension.Pair;
import connectx.extension.Streak;
import connectx.extension.StreakBoard;

import java.util.*;
import java.util.concurrent.TimeoutException;

public class L2 implements CXPlayer {
    private Random rand;
    private CXGameState myWin;
    private CXGameState yourWin;
    private long timeoutTime;
    private long startTime;

    private int l2Player;

    private final CXCellState[] Player = {CXCellState.P1, CXCellState.P2};

    /* Default empty constructor */
    public L2() {
    }

    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        // New random seed for each game
        rand    = new Random(System.currentTimeMillis());
        myWin   = first ? CXGameState.WINP1 : CXGameState.WINP2;
        yourWin = first ? CXGameState.WINP2 : CXGameState.WINP1;

        l2Player = first ? 0 : 1;
        //player = first ? CXCellState.P1 : CXCellState.P2;

        timeoutTime = timeout_in_secs;
    }

    /**
     * Selects a free colum on game board.
     * <p>
     * Selects a winning column (if any), otherwise selects a column (if any)
     * that prevents the adversary to win with his next move. If both previous
     * cases do not apply, selects a random column.
     * </p>
     */
    public int selectColumn(CXBoard B) {
        B = new StreakBoard(B);

        startTime = System.currentTimeMillis(); // Save starting time

        Integer[] L = B.getAvailableColumns();
        int save    = L[rand.nextInt(L.length)]; // Save a random column

        try {
            // DEBUG: Check delle double wins con le streaks:
            // Una streak dovrebbe essere valida, avere X-1 player cells
            // e ci dovrebbero essere due tali streaks.

            /*
            // Print utili per il debug.

            StreakBoard sb = (StreakBoard) B;
            List<Streak> p1Streaks = sb.getStreaksP1();
            List<Streak> p2Streaks = sb.getStreaksP2();

            if (checkDoubleAttack(sb, sb.getStreaksP1(), "P1")) {
                System.out.println("[DEBUG] There is double attack for P1");
            }
            if (checkDoubleAttack(sb, sb.getStreaksP2(), "P2")) {
                System.out.println("[DEBUG] There is double attack for P2");
            }
            if (checkDoubleAttack(sb, sb.getStreaksP2(), "P2")) {
                System.out.println("[DEBUG] There is double attack for P2");
            }
            if (Heuristics.heuristicNMoveWins(sb, sb.getStreaksP2(), 1)) {
                System.out.println("[DEBUG] P2 can win in 1 move");
            }
            */

            int col = singleMoveWin(B,L, myWin);
            if(col != -1) {
                // System.out.println("[DEBUG] Anticipated return (singleMoveWin)");
                return col;
            }

            // L = singleMoveBlock(B, L).toArray(Integer[]::new);

            /*
            if (col != -1) {
                System.out.println("Anticipated return (singleMoveBlock)");
                return col;
            }*/

            int maxDepth = 3;

            int maxScore = Integer.MIN_VALUE;
            int bestMove = -1;

            /*
            // Utile per il debug
            int positionScore = minimax2(
                    (StreakBoard) B,
                    true,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    0,
                    maxDepth
            );*/

            for (Integer colMove : L) {
                B.markColumn(colMove);
                int score = minimax2(
                        (StreakBoard) B,
                        false,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE,
                        0,
                        maxDepth
                );
                B.unmarkColumn();

                // System.out.println("[DEBUG] MOVE AT COL " + colMove + " | SCORE: " + score);
                if (maxScore <= score) {
                    maxScore = score;
                    bestMove = colMove;
                }
            }

            // System.out.println("[DEBUG] Our score at this time: " + positionScore);
            // System.out.println("[DEBUG] Maximizing our score for this move: " + maxScore);
            return bestMove;
        } catch (TimeoutException e) {
            System.err.println("Timeout!!! Random column selected");
            return save;
        }
    }

    private int minimax2(
            StreakBoard B,
            boolean maximizing,
            int alpha,
            int beta,
            int depth,
            int depthMax
    ) throws TimeoutException {
        final CXGameState localMySide;
        // final CXGameState localOpponentSide;
        final CXCellState localPlayer;

        localPlayer = Player[B.currentPlayer()];
        localMySide = localPlayer == CXCellState.P1 ? CXGameState.WINP1 : CXGameState.WINP2;

        // localOpponentSide = localPlayer == CXCellState.P1 ? CXGameState.WINP2 : CXGameState.WINP1;

        if (B.gameState() == CXGameState.DRAW) {
            return 0;
        }
        // If game is finished, stop here
        if (B.gameState() != CXGameState.OPEN) {
            if (maximizing) {
                if (B.gameState() == localMySide) {
                    return Integer.MAX_VALUE;
                } else {
                    return Integer.MIN_VALUE;
                }
            } else {
                if (B.gameState() == localMySide) {
                    return Integer.MIN_VALUE;
                } else {
                    return Integer.MAX_VALUE;
                }
            }
        }

        if (depth == depthMax) {
            // Euristica
            int sign = maximizing ? +1 : -1;
            return sign * Heuristics.score(B, localPlayer, Player[B.currentPlayer()]);
        }

        int canWinSingleMove = singleMoveWin(B, B.getAvailableColumns(), localMySide);

        if (canWinSingleMove != -1) {
            if (maximizing) {
                return Integer.MAX_VALUE;
            } else {
                return Integer.MIN_VALUE;
            }
        }

        // List<Pair<Integer, Integer>> movesWithScores = new ArrayList<>();

        Integer[] L = B.getAvailableColumns();

        int value;
        if (maximizing) {
            value = Integer.MIN_VALUE;
        } else {
            value = Integer.MAX_VALUE;
        }

        /*
        // Disabilitiamo la potatura perché NON migliora la precisione dell'algoritmo,
        // anzi la degrada per com'è attualmente.
        //
        // Potatura
        for(int i : L) {
            checktime(); // Check timeout at every iteration
            CXGameState state = B.markColumn(i);

            // If we win straight-away, we pick that
            if (state == localMySide) {
                if (maximizing) {
                    return Integer.MAX_VALUE;
                } else {
                    return Integer.MIN_VALUE;
                }
            }

            int connectivityScore = Heuristics.estimateConnectivity(B, Player[B.currentPlayer()]);
            movesWithScores.add(new Pair<>(i, connectivityScore));

            B.unmarkColumn();
        }

        movesWithScores.sort((a, b) -> b.second.compareTo(a.second));
        List<Integer> bestMoves = new ArrayList<>(movesWithScores
                .subList(0, Math.min(4, movesWithScores.size()))
                .stream()
                .map(pair -> pair.first)
                .toList());
        */

        if (maximizing) {
            for (int i : L) {
                B.markColumn(i);
                value = Math.max(value, minimax2(B, false, alpha, beta, depth+1, depthMax));
                B.unmarkColumn();
                if (value > beta) {
                    return beta; // Beta-cutoff
                }
                alpha = Math.max(alpha, value);
            }
        } else {
            for (int i : L) {
                B.markColumn(i);
                value = Math.min(value, minimax2(B, true, alpha, beta, depth+1, depthMax));
                B.unmarkColumn();

                if (value < alpha) {
                    return alpha; // Alpha-cutoff
                }
                beta = Math.min(beta, value);
            }
        }
        return value;
    }
    private void checktime() throws TimeoutException {
        if ((System.currentTimeMillis() - startTime) / 1000.0 >= timeoutTime * (99.0 / 100.0))
            throw new TimeoutException();
    }

    /**
     * Check if we can win in a single move
     *
     * Returns the winning column if there is one, otherwise -1
     */
    private int singleMoveWin(CXBoard B, Integer[] L, CXGameState winningSide) throws TimeoutException {
        for(int i : L) {
            checktime(); // Check timeout at every iteration
            CXGameState state = B.markColumn(i);
            B.unmarkColumn();

            if (state == winningSide) {
                return i; // Winning column found: return immediately
            }
        }
        return -1;
    }


    /**
     * Questo metodo attualmente è pressoché identico a Heuristics::checkDoubleAttack.
     *
     * O modificarlo in futuro rendendolo critica anziché euristica, oppure può essere rimosso
     * e si può utilizzare in sua sostituzione il metodo in Heuristics
     */
    private boolean checkDoubleAttack(StreakBoard sb,
                                       List<Streak> playerStreaks,
                                       String playerName) {

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
            if (count == sb.X) {
                System.out.println("PLAYER " + playerName + " HAS WON");
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
        return streaksCount >= 2;
    }
    private boolean isThereDoubleAttack(CXBoard B, Integer[] L, CXGameState winningSide) throws TimeoutException {
        int doSingleMoveWin = singleMoveWin(B, L, winningSide);

        // Caso A
        if (doSingleMoveWin != -1) {
            return true;
        }

        // Caso B; c'è almeno una mossa tale che, per **qualsiasi** mossa dell'avversario, noi vinciamo
        // alla mossa successiva

        int count;

        for (Integer col : L) {
            B.markColumn(col);

            if (B.gameState() != CXGameState.OPEN) {
                B.unmarkColumn();
                continue;

                // Skip
            }

            count = 0;
            for (Integer col2 : B.getAvailableColumns()) {
                CXGameState state = B.markColumn(col2);

                if (state == yourWin) {
                    // Interrompiamo prima se c'è una vittoria per l'avversario
                    B.unmarkColumn();
                    break;
                }

                if (B.gameState() != CXGameState.OPEN) {
                    B.unmarkColumn();
                    continue;
                }

                if (singleMoveWin(B, B.getAvailableColumns(), winningSide) != -1) {
                    count++;
                }

                B.unmarkColumn();
            }
            B.unmarkColumn();

            if (count == L.length) {
                // Almeno un match trovato in cui c'è un doppio attacco.
                return true;
            }
        }
        return false;
    }

    /**
     * Check if we can block adversary's victory
     *
     * Returns a list of columns that don't allow opponent's victory right-away
     * (unless opponent has double attack)
     */
    private int singleMoveBlock(CXBoard B, Integer[] L, CXGameState opponentSide) throws TimeoutException {

        boolean isThereSingleMoveBlock = false;

        TreeSet<Integer> T = new TreeSet<>(); // We collect here safe column indexes

        for(int i : L) {
            checktime();
            T.add(i); // We consider column i as a possible move
            B.markColumn(i); // Our move

            int j;
            boolean stop;

            for(j = 0, stop=false; j < L.length && !stop; j++) {
                //try {Thread.sleep((int)(0.2*1000*TIMEOUT));} catch (Exception e) {} // Uncomment to test timeout
                checktime();
                if(!B.fullColumn(L[j])) {
                    CXGameState state = B.markColumn(L[j]); // Opponent move.
                    if (state == opponentSide) {
                        T.remove(i); // We ignore the i-th column as a possible move
                        stop = true; // We don't need to check more
                    }
                    B.unmarkColumn(); //
                }
            }
            B.unmarkColumn();
        }

        if (T.size() > 0) {
            Integer[] X = T.toArray(new Integer[T.size()]);
            return X[rand.nextInt(X.length)];
        } else {
            return L[rand.nextInt(L.length)];
        }
    }

    public String playerName() {
        return "L2";
    }
}
