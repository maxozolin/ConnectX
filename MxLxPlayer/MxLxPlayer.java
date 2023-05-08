package connectx.MxLxPlayer;

import connectx.CXBoard;
import connectx.CXCell;
import connectx.CXCellState;
import connectx.CXGameState;
import connectx.CXPlayer;
import connectx.MxLxPlayer.IllegalyEfficientBoard;
import connectx.MxLxPlayer.MxLxDecisionTree;
import connectx.MxLxPlayer.TreePrinter;
import java.lang.reflect.Field;
import java.util.Arrays;
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
  private int TIMEOUT;
  private long START;
  private MxLxDecisionTree decisionTree;
  private Integer DEPTH = 5;

  /* Default empty constructor */
  public MxLxPlayer() {}

  public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
    // New random seed for each game
    rand = new Random(System.currentTimeMillis());
    myWin = first ? CXGameState.WINP1 : CXGameState.WINP2;
    yourWin = first ? CXGameState.WINP2 : CXGameState.WINP1;
    TIMEOUT = timeout_in_secs;

    // Forse non va qua ma nella prima mossa, per adesso metto qua
    CXBoard pretend_board = new CXBoard(M, N, K);
    decisionTree = new MxLxDecisionTree(pretend_board, first, DEPTH);
    // TreePrinter.printTree(decisionTree);
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
    // 20 ** 7
    START = System.currentTimeMillis(); // Save starting time

    Integer[] L = B.getAvailableColumns();
    int save = L[rand.nextInt(L.length)]; // Save a random column
    // TreePrinter.printTree(decisionTree);

    try {
      int col = singleMoveWin(B, L);
      if (col != -1)
        return col;

      col = singleMoveBlock(B, L);
      if (col != -1) {
        System.out.printf("ABOUT TO LOSE!: col[%s]\n", col);
        return col;
      }

      return save;
    } catch (TimeoutException e) {
      System.err.println("Timeout!!! Random column selected");
      return save;
    }
  }

  private void checktime() throws TimeoutException {
    if ((System.currentTimeMillis() - START) / 1000.0 >= TIMEOUT * (99.0 / 100.0))
      throw new TimeoutException();
  }
  /**
   * Check if we can win in a single move
   *
   * Returns the winning column if there is one, otherwise -1
   */
  private int singleMoveWin(CXBoard B, Integer[] L) throws TimeoutException {
    for (int i : L) {
      checktime(); // Check timeout at every iteration
      CXGameState state = B.markColumn(i);
      if (state == myWin)
        return i; // Winning column found: return immediately
      B.unmarkColumn();
    }
    return -1;
  }

  /**
   * Check if we can block adversary's victory
   *
   * Returns a blocking column if there is one, otherwise a random one
   */
  private int singleMoveBlock(CXBoard B, Integer[] L) throws TimeoutException {
    // Single move block implementation was O(L.size()^{2}), which is bad.
    // We can make it O(L.size())
    // Returns -1 if no need to block
    // If no way to block will still return the first in case opposite AI is dumb and we can block
    // both in time :)

    int ret = -1;
    IllegalyEfficientBoard.swapCurrentPlayer(B);
    try {
      Class<?> cls = Class.forName("connectx.CXBoard");
      Field mc_field = cls.getDeclaredField("MC");
      mc_field.setAccessible(true);
      LinkedList<CXCell> MC = (LinkedList<CXCell>) mc_field.get(B);
      System.out.printf("MC: [\n");
      for(CXCell c : MC){
        System.out.printf(" ( c: %s | r: %s | %s ) ,\n", c.j, c.i, c.state);
      }
      System.out.printf("]\n");
      System.out.printf("COLS:\t");
      for (int i : L) {
        CXGameState marked = B.markColumn(i);
        System.out.printf("%s:%s\t", i, marked);
        B.unmarkColumn();
        if (marked == yourWin) {
          ret = i;
          break;
        }
      }
      System.out.printf("\n");
      System.out.printf("%s\n", yourWin);
      System.out.printf("CURRENT: %s\n", B.currentPlayer());

      return ret;
    } catch (Exception e) {
      System.err.println(e);
      return -1;
    }
  }

  public String playerName() {
    return "MxLxPlayer";
  }
}
