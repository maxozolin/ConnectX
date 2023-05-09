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

  public void debug_RP(CXBoard B){

    //for (int i=0;i<B.RP.length;i++){
    //  System.out.printf("======");
    //}
    //System.out.println();
    //System.out.printf("|");
    //for (int i=0;i<B.RP.length;i++){
    //  System.out.printf(" %s  | ", i );
    //}
    //System.out.println();
    //for (int i=0;i<B.RP.length;i++){
    //  System.out.printf("------");
    //}
    //System.out.println();
    //System.out.printf("|");
    //for (int i=0;i<B.RP.length;i++){
    //  System.out.printf(" %s  | " , B.RP[i]);
    //}
    //System.out.println();
    //for (int i=0;i<B.RP.length;i++){
    //  System.out.printf("======");
    //}
    //System.out.println();
  }

  public int selectColumn(CXBoard B) {
    // 20 ** 7
    //int rows = B.B.length;
    //int cols = B.B[0].length;
    //for (int i = 0; i < rows; i++) {
    //  for (int j = 0; j < cols; j++) {
    //    System.out.printf(" [%s] ", B.B[i][j].toString().charAt(1));
    //  }
    //  System.out.println();
    //}

    START = System.currentTimeMillis(); // Save starting time

    Integer[] L = B.getAvailableColumns();
    int save = L[rand.nextInt(L.length)]; // Save a random column
    // TreePrinter.printTree(decisionTree);

    try {
      int col = singleMoveWin(B, L);
      if (col != -1){
        System.err.printf("Can win: %s\n", col);
        return col;
      }

      col = singleMoveBlock(B, L);
      if (col != -1) {
        System.err.printf("Have to block: %s\n", col);
        return col;
      }

      Integer[] L_not_stupid = notOponentWinsNext(L, B);

      //System.out.println(save);
      //We might only have stupid moves left, don't want to error out
      if(L_not_stupid.length > 0){
        save = L_not_stupid[rand.nextInt(L_not_stupid.length)]; // Save a random column
        //System.out.println(save);
      }

      return save;
    } catch (TimeoutException e) {
      System.err.println("Timeout!!! Random column selected");
      return save;
    }
  }

  private Integer[] notOponentWinsNext(Integer[] L,CXBoard B){

    LinkedList<Integer> newL = new LinkedList();
    for (Integer i: L){
      CXGameState state = CXGameState.OPEN;
      B.markColumn(i);
      // Puo succedere che c'e solo un posto rimanente nella colonna
      // e mettere un secondo sarebbe illegale
      try{
        state = B.markColumn(i);
        B.unmarkColumn();
      } catch (IllegalStateException e){
        //System.err.println(e.getMessage());
      }
      B.unmarkColumn();
      if (state != yourWin){
        newL.add(i);
      }
      else {
        System.err.printf("Avoiding stupid move [OpponentWinsNext]: %s\n", i);
      }
    }

    return newL.toArray(new Integer[newL.size()]);
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
    for (Integer i : L) {
      checktime(); // Check timeout at every iteration
      CXGameState state = B.markColumn(i);
      B.unmarkColumn();
      if (state == myWin)
        return i; // Winning column found: return immediately
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

    for (int i : L) {
      CXGameState marked = B.markColumn(i);
      B.unmarkColumn();
      if (marked == yourWin) {
        ret = i;
        break;
      }
    }

    IllegalyEfficientBoard.swapCurrentPlayer(B);

    return ret;
  }

  public String playerName() {
    return "MxLxPlayer";
  }
}
