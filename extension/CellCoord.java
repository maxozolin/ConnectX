package connectx.extension;

import connectx.CXCellState;

public class CellCoord {
    private final StreakBoard board;

    /**
     * Cell row index
     */
    public final int i;
    /**
     * Cell column index
     */
    public final int j;

    public CellCoord(StreakBoard board, int i, int j) {
        this.board = board;
        this.i = i;
        this.j = j;
    }


    //public final CXCellState state;
    public CXCellState getState() {
        return board.getBoard()[i][j];
    }

    @Override
    public String toString() {
        return "CellCoord(i = " + i + ", j = " + j + ", state = " + getState() + ")\n";
    }
}
