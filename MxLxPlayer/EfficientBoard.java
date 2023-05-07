package connectx.MxLxPlayer;

import connectx.CXBoard;
import connectx.CXCell;
import connectx.CXCellState;
import connectx.CXGameState;
import connectx.CXPlayer;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

// Utility class for efficient board manipulation
@SuppressWarnings({"unchecked"})
public class EfficientBoard {
  public static void efficientMarkColumn(int col, CXBoard b) {
    System.err.println("Marking...");

    try {
      Class<?> cls = Class.forName("connectx.CXBoard");

      // You have nice fields, I'd like to see them
      Field rp_field = cls.getDeclaredField("RP");
      Field player_field = cls.getDeclaredField("Player");
      Field curr_player_field = cls.getDeclaredField("currentPlayer");
      Field board_field = cls.getDeclaredField("B");
      Field ac_field = cls.getDeclaredField("AC");
      Field mc_field = cls.getDeclaredField("MC");
      Field m_field = cls.getDeclaredField("M");

      // Don't really care that they are private
      rp_field.setAccessible(true);
      ac_field.setAccessible(true);
      player_field.setAccessible(true);
      curr_player_field.setAccessible(true);
      board_field.setAccessible(true);
      mc_field.setAccessible(true);

      // Let me access them from these variables
      CXCellState[][] B = (CXCellState[][]) board_field.get(b);
      TreeSet<Integer> AC = (TreeSet<Integer>) ac_field.get(b);
      CXCellState[] Player = (CXCellState[]) player_field.get(b);
      int RP[] = (int[]) rp_field.get(b);
      int currentPlayer = (int) curr_player_field.get(b);
      LinkedList<CXCell> MC = (LinkedList<CXCell>) mc_field.get(b);


      // Doing stuff with private fields :))))
      int row = RP[col]--;
      if (RP[col] == -1)
        AC.remove(col);

      B[row][col] = Player[currentPlayer];
      CXCell newc = new CXCell(row, col, Player[currentPlayer]);
      MC.add(newc); // Add move to the history
      currentPlayer = (currentPlayer + 1) % 2;
      Iterator<CXCell> it = MC.iterator();
      System.err.printf("%s\n", B);
      //while (it.hasNext()) {
      //  System.out.printf("%s -> ",it.next());
      //}
      //System.out.println();

    } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException
        | SecurityException e) {
      System.err.println(e.toString());
    }

    //  int row = b.RP[col]--;
    //  if (RP[col] == -1)
    //    AC.remove(col);
    //  System.out.println(String.format("", currentPlayer));
    //  B[row][col] = Player[currentPlayer];
    //  CXCell newc = new CXCell(row, col, Player[currentPlayer]);
    //  MC.add(newc); // Add move to the history
  }
  // I don't care about game_state, It's just to override the inefficient function that checks for
  // winning move
  // public CXGameState markColumn(int col) throws IndexOutOfBoundsException, IllegalStateException
  // {
  //  System.err.println("Marking...");
  //  int row = RP[col]--;
  //  if (RP[col] == -1)
  //    AC.remove(col);
  //  System.out.println(String.format("", currentPlayer));
  //  B[row][col] = Player[currentPlayer];
  //  CXCell newc = new CXCell(row, col, Player[currentPlayer]);
  //  MC.add(newc); // Add move to the history

  //  return gameState;
  //}

  // public void unmarkColumn() throws IllegalStateException {
  //  System.err.println("Unmarking...");
  //  super.unmarkColumn();
  //}
}
