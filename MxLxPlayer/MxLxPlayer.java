package connectx.MxLxPlayer;

import connectx.CXBoard;
import connectx.CXBoardPanel;
import connectx.CXCell;
import connectx.CXCellState;
import connectx.CXGameState;
import connectx.CXPlayer;
import connectx.MxLxPlayer.IllegalyEfficientBoard;
import connectx.MxLxPlayer.DecisionTree;
import connectx.MxLxPlayer.TreePrinter;
import connectx.MxLxPlayer.TimeKeeper;
import connectx.MxLxPlayer.CriticalMoves;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

import javax.swing.border.Border;

/**
 * Software player only a bit smarter than random.
 * <p>
 * It can detect a single-move win or loss. In all the other cases behaves
 * randomly.
 * </p>
 */
public class MxLxPlayer implements CXPlayer {
  private Random rand;
  private CXGameState myWin;
  private CXGameState yourWin;
  private TimeKeeper timeKeeper;
  private DecisionTree decisionTree;
  private Integer DEPTH = 5;
  public CXBoardPanel debugDrawPanel;
  private DebugStreakDisplayer debugDisplayer = new DebugStreakDisplayer();
  private final CXCellState[] Player = {CXCellState.P1, CXCellState.P2};
  private int currentMove;

  /* Default empty constructor */
  public MxLxPlayer() {
  }

  public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
    // New random seed for each game
    rand = new Random(System.currentTimeMillis());
    myWin = first ? CXGameState.WINP1 : CXGameState.WINP2;
    yourWin = first ? CXGameState.WINP2 : CXGameState.WINP1;
    timeKeeper = new TimeKeeper(timeout_in_secs);
    currentMove = 0;

    // Forse non va qua ma nella prima mossa, per adesso metto qua
    CXBoard pretend_board = new CXBoard(M, N, K);
    // decisionTree = new DecisionTree(pretend_board, first, DEPTH);
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
    debugDisplayer.clear();
    int col = B.getAvailableColumns()[0];
    try {
      col = selectColumnBase(B);
    } catch (Exception ex) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      ex.printStackTrace(pw);
      System.err.println("======= ERR ========");
      System.err.println(sw.toString());
    }
    // int col = selectColumnDebug(B);
    // StreakBoard streakB = new StreakBoard(B);
    // streakB.markColumn(col);
    // List<Streak> p1Streaks = streakB.getStreaksP1();
    // List<Streak> p2Streaks = streakB.getStreaksP2();
    // debugDisplayer.updateMainDisplay(debugDrawPanel);

    return col;

  }

  public int selectColumnDebug(CXBoard B) {
    int col = B.getAvailableColumns()[0];
    StreakBoard streakB = new StreakBoard(B);
    Heuristics heuristics = new Heuristics();
    heuristics.debugStreakDisplayer = debugDisplayer;

    if (checkDoubleAttackv2(B, myWin)) {
      System.err.println("There is double attack for P1");
    }

    if (checkDoubleAttackv2(B, yourWin)) {
      System.err.println("There is double attack for P2");
    }
    return col;
  }

  public boolean firstMove(){
    currentMove += 1;
    return currentMove==1;
  }
  public int selectColumnBase(CXBoard B) {
    timeKeeper.setStartTime(System.currentTimeMillis());
    StreakBoard streakB = new StreakBoard(B);
    Heuristics heuristics = new Heuristics();
    heuristics.debugStreakDisplayer = debugDisplayer;

    Integer[] L = B.getAvailableColumns();
    int save = L[rand.nextInt(L.length)]; // Save a random column


    if(firstMove()){
      System.out.println("[+] First move, putting in  center");
      int cols = B.N;
      return cols/2;
    }

    int col = CriticalMoves.singleMoveWin(B, L, myWin);
    if (col != -1) {
      System.err.printf("[+] Can win: %s\n", col);
      return col;
    }

    if (timeKeeper.ranOutOfTime())
      return save;

    col = CriticalMoves.singleMoveBlock(B, L, yourWin);
    if (col != -1) {
      System.err.printf("[!] Have to block: %s\n", col);
      return col;
    }

    if (timeKeeper.ranOutOfTime())
      return save;

    Integer[] L_not_stupid = CriticalMoves.notOpponentWinsNext(L, B, yourWin);
    if (L_not_stupid.length > 0) {
      save = L_not_stupid[rand.nextInt(L_not_stupid.length)]; // Save a random column that is not stupid
    }

    // System.out.printf("--- Looking for Me ---\n");
    List<Integer> datt1 = findDoubleAttacksv2(B, myWin);

    // System.out.printf("--- Looking for You---\n");
    List<Integer> datt2 = findDoubleAttacksv2(B, yourWin);
    // Random choice because double attack for me, opponent can only block one
    if (datt1.size() != 0) {
      System.out.printf("[+] Double Attack for ME: %s\n", datt1);
      return datt1.get(rand.nextInt(datt1.size()));
    }

    // To determine how i want to block Double Attack, estimate for connectivity
    if (datt2.size() != 0) {
      System.out.printf("[-] Double Attack for OPPONENT: %s\n", datt2);
      int move =  datt2.get(rand.nextInt(datt2.size()));
      boolean move_stupid = true;
      for(Integer sm : L_not_stupid){
        if(sm==move){
          move_stupid = false;
        }
      }
      if(!move_stupid){
        return move;
      }
    }

    if (timeKeeper.ranOutOfTime())
      return save;

    int maxDepth = 3;

    int maxScore = Integer.MIN_VALUE;
    int bestMove = -1;

    try{
      int positionScore = minimax2(
          (StreakBoard) streakB,
          true,
          Integer.MIN_VALUE,
          Integer.MAX_VALUE,
          0,
          maxDepth);

      System.out.printf("Position Score: %s\n",positionScore);

      for (Integer colMove : L) {
        streakB.markColumn(colMove);
        int score = minimax2(
            (StreakBoard) streakB,
            false,
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            0,
            maxDepth);
        streakB.unmarkColumn();
        System.out.printf("\tMove [%s] evaluation: %s\n",colMove, score);

        // System.out.println("MOVE AT COL " + colMove + " | SCORE: " + score);
        if (maxScore <= score) {
          maxScore = score;
          bestMove = colMove;
        }
      }
    } catch (TimeoutException ex){
      //does not bother
      System.err.println("[!] Timeout from MINIMAX!");
      return save;
    }
    if (bestMove != -1) {
      return bestMove;
    }

    return save;
  }

  private int minimax2(
      StreakBoard B,
      boolean maximizing,
      int alpha,
      int beta,
      int depth,
      int depthMax) throws TimeoutException {
    final CXGameState localMySide;
    final CXGameState localOpponentSide;
    final CXCellState localPlayer;

    Heuristics localHeuristic = new Heuristics();
    localPlayer = Player[B.currentPlayer()];
    localMySide = localPlayer == CXCellState.P1 ? CXGameState.WINP1 : CXGameState.WINP2;

    localOpponentSide = localPlayer == CXCellState.P1 ? CXGameState.WINP2 : CXGameState.WINP1;
    // localOpponentSide = Player[1-B.currentPlayer()];

    /*
     * if (maximizing) {
     * localMySide = myWin;
     * localOpponentSide = yourWin;
     * localPlayer = Player[l2Player];
     * } else {
     * localPlayer = Player[1-l2Player];
     * localMySide = yourWin;
     * localOpponentSide = myWin;
     * }
     */

    if (B.gameState() == CXGameState.DRAW) {
      return 0;
    }
    // If game is finished, stop here
    if (B.gameState() != CXGameState.OPEN) {
      // return signify(B.gameState(), maximizing);
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
      return sign * localHeuristic.score(B, localPlayer, Player[B.currentPlayer()]);
    }

    int canWinSingleMove = CriticalMoves.singleMoveWin(B, B.getAvailableColumns(), localMySide);

    if (canWinSingleMove != -1) {
      // System.out.println("MAXIMIZING HERE!!!!");
      if (maximizing) {
        return Integer.MAX_VALUE;
      } else {
        return Integer.MIN_VALUE;
      }
      /*
       * if (Player[B.currentPlayer()] == localPlayer) {
       * if (maximizing) {
       * return Integer.MAX_VALUE;
       * } else {
       * return Integer.MIN_VALUE;
       * }
       * } else {
       * if (maximizing) {
       * return Integer.MIN_VALUE;
       * } else {
       * return Integer.MAX_VALUE;
       * }
       * }
       */
    }

    List<Pair<Integer, Integer>> movesWithScores = new ArrayList<>();

    Integer[] L = B.getAvailableColumns();

    int value;
    if (maximizing) {
      value = Integer.MIN_VALUE;
    } else {
      value = Integer.MAX_VALUE;
    }
    // List<Integer> bestMoves = Arrays.asList(L);

    // Potatura
    for (int i : L) {
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

      int connectivityScore = localHeuristic.estimateConnectivity(B, Player[B.currentPlayer()]);
      movesWithScores.add(new Pair<>(i, connectivityScore));

      B.unmarkColumn();
    }

    movesWithScores.sort((a, b) -> b.second.compareTo(a.second));
    List<Integer> bestMoves = new ArrayList<>(movesWithScores
        .subList(0, Math.min(4, movesWithScores.size()))
        .stream()
        .map(pair -> pair.first)
        .toList());

    if (maximizing) {
      for (int i : L) {
        B.markColumn(i);
        value = Math.max(value, minimax2(B, false, alpha, beta, depth + 1, depthMax));
        B.unmarkColumn();
        if (value > beta) {
          return beta; // Beta-cutoff
        }
        alpha = Math.max(alpha, value);
      }
    } else {
      for (int i : L) {
        B.markColumn(i);
        value = Math.min(value, minimax2(B, true, alpha, beta, depth + 1, depthMax));
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
    timeKeeper.checktime();
  }

  private boolean checkDoubleAttack_twoWins(CXBoard board, Integer startIndex,
      CXGameState winningState) {
    int winning_moves = 0;
    Integer cols = board.N;
    Integer toConnect = board.X;

    for (int i = Math.max(startIndex - toConnect, 0); i < Math.min(startIndex + toConnect, cols); i++) {
      try {
        CXGameState gs = board.markColumn(i);
        board.unmarkColumn();

        if (gs == winningState) {
          winning_moves++;
        }
      } catch (IllegalStateException ex) {
        // System.err.println(ex.getMessage());
      }
    }

    return winning_moves >= 2;
  }

  private boolean checkDoubleAttack_blockWins(CXBoard board, Integer blockingCol, CXGameState winningState) {
    try {
      // block
      board.markColumn(blockingCol);

      // moveAfterBlock
      CXGameState gs = board.markColumn(blockingCol);

      if (gs == winningState) {
        return true;
      }

    } catch (IllegalStateException ex) {
      // Don't care

    }
    return false;
  }

  private List<Integer> findDoubleAttacksv2(CXBoard board, CXGameState winningState) {
    boolean haveToSwich = (winningState == yourWin);
    CXGameState localOpponentWin = winningState == yourWin ? myWin : yourWin;
    CXGameState localMyWin = winningState == yourWin ? yourWin : myWin;

    List<Integer> ret = new ArrayList<Integer>();

    if (haveToSwich)
      IllegalyEfficientBoard.swapCurrentPlayer(board);

    boolean twoWins = false;
    boolean blockWins = false;

    for (Integer m : board.getAvailableColumns()) {
      boolean stop = false;

      board.markColumn(m);

      IllegalyEfficientBoard.swapCurrentPlayer(board);
      boolean datt = checkDoubleAttack_twoWins(board, m, winningState);
      IllegalyEfficientBoard.swapCurrentPlayer(board);
      if (datt) {
        stop = true;
        ret.add(m);
      }

      board.unmarkColumn();
    }

    // System.err.printf("MOVES : %s\n", board.getLastMove());
    for (Integer m : board.getAvailableColumns()) {
      board.markColumn(m);

      int haveToBlock = CriticalMoves.localizedSingleMoveBlock(board, m, localMyWin);

      // System.err.printf("Considering %s: %s\n", m, haveToBlock);
      if (haveToBlock != -1) {
        try {
          board.markColumn(haveToBlock);
        } catch (IllegalAccessError ex) {
          continue;
        }

        try {
          CXGameState gs = board.markColumn(haveToBlock);
          board.unmarkColumn();
          // System.err.printf("Considering %s: %s\nLocalWin: %s\n", m, gs, localMyWin);
          if (gs == localMyWin) {
            ret.add(m);
          }
        } catch (IllegalAccessError | IllegalStateException ex) {
        }
        board.unmarkColumn();

      }
      board.unmarkColumn();
    }
    // System.err.printf("MOVES : %s\n", board.getLastMove());

    if (haveToSwich)
      IllegalyEfficientBoard.swapCurrentPlayer(board);

    return ret;
  }

  private boolean checkDoubleAttackv2(CXBoard board, CXGameState winningState) {
    boolean haveToSwich = (winningState == yourWin);
    CXGameState opponentWin = winningState == yourWin ? myWin : yourWin;

    if (haveToSwich)
      IllegalyEfficientBoard.swapCurrentPlayer(board);

    boolean twoWins = false;
    boolean blockWins = false;
    Integer haveToBlock = CriticalMoves.singleMoveBlock(board, board.getAvailableColumns(), opponentWin);
    for (Integer m : board.getAvailableColumns()) {
      twoWins = Boolean.logicalOr(twoWins, checkDoubleAttack_twoWins(board, m, winningState));
    }
    if (haveToBlock != -1) {
      blockWins = Boolean.logicalOr(blockWins, checkDoubleAttack_twoWins(board, haveToBlock, winningState));
    }

    if (haveToSwich)
      IllegalyEfficientBoard.swapCurrentPlayer(board);

    return Boolean.logicalOr(twoWins, blockWins);
  }

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
            if (cell.i == sb.M - 1 ||
                sb.getBoard()[cell.i + 1][cell.j] != CXCellState.FREE) {

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

  public String playerName() {
    return "MxLxPlayer";
  }
}
