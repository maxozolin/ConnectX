package connectx.test;

import connectx.extension.StreakBoard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStreaks {

    @Test
    public void testStreak01() {
        StreakBoard board = new StreakBoard(7, 7, 4);

        board.markColumn(3); // P1 | i = 6, j = 3
        board.markColumn(4); // P2 | i = 6, j = 4
        board.markColumn(3); // P1 | i = 5, j = 3
        board.markColumn(4); // P2 | i = 5, j = 4
        board.markColumn(4); // P1 | i = 4, j = 4
        board.markColumn(3); // P2 | i = 4, j = 3

        System.out.println("getStreaksP1");
        System.out.println(board.getStreaksP1());
        System.out.println("getStreaksP2");
        System.out.println(board.getStreaksP2());
    }
}
