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
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

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

  /* Default empty constructor */
  public MxLxPlayer() {
  }

  public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
    // New random seed for each game
    rand = new Random(System.currentTimeMillis());
    myWin = first ? CXGameState.WINP1 : CXGameState.WINP2;
    yourWin = first ? CXGameState.WINP2 : CXGameState.WINP1;
    timeKeeper = new TimeKeeper(timeout_in_secs);

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
    timeKeeper.setStartTime(System.currentTimeMillis());
    StreakBoard streakB = new StreakBoard(B);

    Integer[] L = B.getAvailableColumns();
    int save = L[rand.nextInt(L.length)]; // Save a random column

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

    if (timeKeeper.ranOutOfTime())
      return save;

    List<Streak> p1Streaks = streakB.getStreaksP1();
    List<Streak> p2Streaks = streakB.getStreaksP2();
    try{
      // CAREFUL WHEN DOING THIS TYPE OF ACCESS
      // DO NOT ACCESS DIRECTLY, WILL NOT COMPILE 
      Field slf = debugDrawPanel.getClass().getField("streakList");
      slf.set(debugDrawPanel, p1Streaks);

    } catch (Exception ex){

    }
    //System.out.println(p1Streaks);

    try {
      if (checkDoubleAttack(streakB, streakB.getStreaksP1(), "P1")) {
        System.out.println("There is double attack for P1");
      }
      if (checkDoubleAttack(streakB, streakB.getStreaksP2(), "P2")) {
        System.out.println("There is double attack for P2");
      }
      if (Heuristics.heuristicNMoveWins(streakB, streakB.getStreaksP2(), 1)) {
        System.out.println("P2 can win in 1 move");
      }

      col = CriticalMoves.singleMoveWin(B, L, myWin);
      if (col != -1) {
        System.out.println("Anticipated return (singleMoveWin)");
        return col;
      }

    } catch (Exception ex) {
      System.err.println(ex.getStackTrace());
    }

    return save;
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
