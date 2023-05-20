package connectx.MxLxPlayer;

import connectx.CXBoard;
import connectx.CXGameState;
import java.util.concurrent.TimeoutException;
import java.util.LinkedList;
import connectx.MxLxPlayer.IllegalyEfficientBoard;;


public class CriticalMoves{
  /*
    Returns winning move column (> 0) if can win in a single move
    Otherwise returns -1

    REMOVED: checktime.
    Note: 
      For loops are fast, no reason to time, wasted computation for a function
      that is called a lot.

      There is an argument to make that it is useful to have something that is always
       called throw TimeoutExceptions when it is time to return but if there is a bug
       somewhere with timing good luck finding it!


    B: Board
    L: List of available columns
    myWin: In what gameState is it considered my win
    timeKeeper: TimeKeeper Instance to check the time
  */
  static public int singleMoveWin(CXBoard B, Integer[] L, CXGameState myWin) {
    for (Integer i : L) {
      CXGameState state = B.markColumn(i);
      B.unmarkColumn();
      if (state == myWin)
        return i; // Winning column found: return immediately
    }
    return -1;
  }


  /*
    Single move block implementation was O(L.size()^{2}), which is bad.
    We can make it O(L.size())
    Returns -1 if no need to block
    If no way to block will still return the first in case opposite AI is dumb and we can block
    both in time :)
    
    B: board
    L: available moves
    yourWin: GameState in which the opponent wins
  */

  static public int singleMoveBlock(CXBoard B, Integer[] L, CXGameState yourWin) {

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

  /*
    Returns a list of moves that do not allow the opponent to win next move.
  */
  static public Integer[] notOpponentWinsNext(Integer[] L,CXBoard B, CXGameState yourWin){
    LinkedList<Integer> newL = new LinkedList<Integer>();
    for (Integer i: L){
      CXGameState state = CXGameState.OPEN;
      B.markColumn(i);
      // Puo succedere che c'e solo un posto rimanente nella colonna
      // e mettere un secondo sarebbe illegale
      try{
        state = B.markColumn(i);
        B.unmarkColumn();
      } catch (IllegalStateException e){
        // If we try to play in a full column it fails
        // But for this use case we don't actually care
      }

      B.unmarkColumn();
      if (state != yourWin){
        newL.add(i);
      }
      else {
        System.err.printf("[+] Avoiding bad move : %s\n", i);
      }
    }

    return newL.toArray(new Integer[newL.size()]);
  }

}
