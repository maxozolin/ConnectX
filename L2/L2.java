package connectx.L2;

import connectx.CXBoard;
import connectx.CXCellState;
import connectx.CXGameState;
import connectx.CXPlayer;
import connectx.extension.Pair;
import connectx.extension.StreakBoard;

import java.util.*;
import java.util.concurrent.TimeoutException;

public class L2 implements CXPlayer {
    private Random rand;
    private CXGameState myWin;
    private CXGameState yourWin;
    private long timeoutTime;
    private long startTime;

    private int currentPlayer;

    private final CXCellState[] Player = {CXCellState.P1, CXCellState.P2};

    /* Default empty constructor */
    public L2() {
    }

    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        // New random seed for each game
        rand    = new Random(System.currentTimeMillis());
        myWin   = first ? CXGameState.WINP1 : CXGameState.WINP2;
        yourWin = first ? CXGameState.WINP2 : CXGameState.WINP1;

        currentPlayer = first ? 0 : 1;
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
            int col = singleMoveWin(B,L);
            if(col != -1) {
                return col;
            }

            col = singleMoveBlock(B,L);

            if (col != -1) {
                return col;
            }

            int maxDepth = 6;
            Pair<Integer, Integer> pair = minimax(
                    (StreakBoard) B,
                    true,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE,
                    new int[maxDepth],
                    0,
                    maxDepth
                    );

            return pair.first;
        } catch (TimeoutException e) {
            System.err.println("Timeout!!! Random column selected");
            return save;
        }
    }

    private Pair<Integer, Integer> minimax(StreakBoard B, boolean isMax, int alpha, int beta, int[] bestScores, int depth, int depthMax) throws TimeoutException {
        if (B.gameState() == CXGameState.DRAW) {
            return new Pair<>(-1, 0);
        }
        if (B.gameState() == CXGameState.WINP1) {
            if (isMax && myWin == CXGameState.WINP1) {
                return new Pair<>(-1, Integer.MAX_VALUE);
            } else {
                return new Pair<>(-1, Integer.MIN_VALUE);
            }
        }
        if (B.gameState() == CXGameState.WINP2) {
            if (isMax && myWin == CXGameState.WINP2) {
                return new Pair<>(-1, Integer.MAX_VALUE);
            } else {
                return new Pair<>(-1, Integer.MIN_VALUE);
            }
        }

        int canWinSingleMove = singleMoveWin(B, B.getAvailableColumns());

        if (canWinSingleMove != -1) {
            int score = isMax ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            return new Pair<>(canWinSingleMove, score);
        }

        if (depth == depthMax) {
            // Valutazione euristica

            int value = Euristics.score(B, Player[B.currentPlayer()], Player[B.currentPlayer()]);

            if (isMax) {
                alpha = value;
                // bestScores[depth] = alpha;
                return new Pair<>(null, alpha);
            } else {
                beta = value;
                return new Pair<>(null, -beta);
            }
        } else {

            // Albero delle mosse
            Integer[] L = B.getAvailableColumns();

            List<Pair<Integer, Integer>> movesWithScores = new ArrayList<>();
            for(int i : L) {
                checktime(); // Check timeout at every iteration
                CXGameState state = B.markColumn(i);

                int connectivityScore = Euristics.estimateConnectivity(B, Player[currentPlayer]);

                B.unmarkColumn();

                movesWithScores.add(new Pair<>(i, connectivityScore));
            }

            // Reverse sort
            movesWithScores.sort((a, b) -> b.second.compareTo(a.second));

            List<Integer> bestMoves = movesWithScores
                    .subList(0, Math.min(4, movesWithScores.size()))
                    .stream()
                    .map(pair -> pair.first)
                    .toList();

            List<Pair<Integer, Integer>> movesAndScoresFinal = new ArrayList<>();

            for (int i : bestMoves) {
                checktime(); // Check timeout at every iteration
                try {
                    B.markColumn(i);
                } catch (Exception ex) {
                    System.err.println("TRIED TO MARK UNAVAILABLE COLUMN " + i + ":");
                    System.err.println("AVAILABLE COLUMNS:");
                    System.err.println(List.of(B.getAvailableColumns()));
                    System.err.println("L COLUMNS");
                    System.err.println(List.of(L));
                    System.err.println("STREAKBOARD MARK UNMARK COUNTS: " + StreakBoard.markCount + " " + StreakBoard.unmarkCount);
                    // ex.printStackTrace();
                    System.err.flush();
                    throw ex;
                }
                Pair<Integer, Integer> pair = minimax(B, !isMax, alpha, beta, bestScores, depth+1, depthMax);
                int value = pair.second;

                B.unmarkColumn();

                int score;
                if (!isMax) {
                    // Maximizing

                    if (value >= beta) {
                        value = beta;

                        score = beta;
                    } else if (value > alpha) {
                        alpha = value;
                        //return alpha;
                        score = alpha;
                    } else {
                        score = value;
                    }
                } else {
                    // Minimizing

                    if (value <= alpha) {
                        score = alpha;
                    } else if (value < beta) {
                        score = beta;
                    } else {
                        score = value;
                    }
                }
                movesAndScoresFinal.add(new Pair<>(i, score));
            }

            if (isMax) {
                // Reverse sort, we want highest element.
                movesAndScoresFinal.sort((a, b) -> b.second.compareTo(a.second));
            } else {
                // Normal sort, we want lowest element.
                movesAndScoresFinal.sort(Comparator.comparing(a -> a.second));
            }

            return movesAndScoresFinal.get(0);
        }
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
    private int singleMoveWin(CXBoard B, Integer[] L) throws TimeoutException {
        for(int i : L) {
            checktime(); // Check timeout at every iteration
            CXGameState state = B.markColumn(i);
            if (state == myWin)
                return i; // Winning column found: return immediately
            B.unmarkColumn();
        }
        return -1;
    }

    private boolean isThereDoubleAttack(CXBoard B, Integer[] L) throws TimeoutException {
        if (B.currentPlayer() == currentPlayer) {
            return singleMoveWin(B, L) != -1;
        } else {

            int matches = 0;

            for (int i : L) {
                checktime(); // Check timeout at every iteration
                CXGameState state = B.markColumn(i);

                if (state == yourWin) {
                    // Interrompiamo prima se c'è una vittoria per l'avversario
                    return false;
                }
                boolean winFound = false;
                Integer[] L2 = B.getAvailableColumns();
                for (int j : L2) {
                    checktime(); // Check timeout at every iteration
                    B.markColumn(j);
                    if (state == myWin) {
                        winFound = true;
                        break;
                    }
                    B.unmarkColumn();
                }
                B.unmarkColumn();

                if (winFound) {
                    matches += 1;
                    // Abbiamo bisogno di trovare la vittoria L.length volte per essere
                    // certi di un doppio attacco
                }
            }

            return matches == L.length;
        }

    }

    /**
     * Check if we can block adversary's victory
     *
     * Returns a blocking column if there is one, otherwise -1
     */
    private int singleMoveBlock(CXBoard B, Integer[] L) throws TimeoutException {
        TreeSet<Integer> T = new TreeSet<Integer>(); // We collect here safe column indexes

        for(int i : L) {
            checktime();
            T.add(i); // We consider column i as a possible move
            B.markColumn(i);

            int j;
            boolean stop;

            for(j = 0, stop=false; j < L.length && !stop; j++) {
                //try {Thread.sleep((int)(0.2*1000*TIMEOUT));} catch (Exception e) {} // Uncomment to test timeout
                checktime();
                if(!B.fullColumn(L[j])) {
                    CXGameState state = B.markColumn(L[j]);
                    if (state == yourWin) {
                        T.remove(i); // We ignore the i-th column as a possible move
                        stop = true; // We don't need to check more
                    }
                    B.unmarkColumn(); //
                }
            }
            B.unmarkColumn();
        }

        if (T.size() > 0) {
            // Integer[] X = T.toArray(new Integer[T.size()]);
            Integer[] X = T.toArray(new Integer[0]);
            return X[rand.nextInt(X.length)];
        } else {
            return -1; // L[rand.nextInt(L.length)];
        }
    }

    public String playerName() {
        return "L2";
    }
}
