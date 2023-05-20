package connectx.MxLxPlayer;

import connectx.CXBoard;
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

  /* Default empty constructor */
  public MxLxPlayer() {}

  public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
    // New random seed for each game
    rand = new Random(System.currentTimeMillis());
    myWin = first ? CXGameState.WINP1 : CXGameState.WINP2;
    yourWin = first ? CXGameState.WINP2 : CXGameState.WINP1;
    timeKeeper = new TimeKeeper(timeout_in_secs);

    // Forse non va qua ma nella prima mossa, per adesso metto qua
    CXBoard pretend_board = new CXBoard(M, N, K);
    decisionTree = new DecisionTree(pretend_board, first, DEPTH);
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


    Integer[] L = B.getAvailableColumns();
    int save = L[rand.nextInt(L.length)]; // Save a random column

    int col = CriticalMoves.singleMoveWin(B, L, myWin);
    if (col != -1){
      System.err.printf("[+] Can win: %s\n", col);
      return col;
    }

    if(timeKeeper.ranOutOfTime()) return save;

    col = CriticalMoves.singleMoveBlock(B, L, yourWin);
    if (col != -1) {
      System.err.printf("[!] Have to block: %s\n", col);
      return col;
    }

    if(timeKeeper.ranOutOfTime()) return save;
    

    Integer[] L_not_stupid = CriticalMoves.notOpponentWinsNext(L, B, yourWin);
    if(L_not_stupid.length > 0){
      save = L_not_stupid[rand.nextInt(L_not_stupid.length)]; // Save a random column that is not stupid
    }

    if(timeKeeper.ranOutOfTime()) return save;
    return save;
  }


  public String playerName() {
    return "MxLxPlayer";
  }
}
