package connectx.extension;

import connectx.CXCellState;

/**
 * Sarebbe un CellPointer in altri termini. Utilizzato per riferirsi alle coordinate di una cella
 * della scacchiera, ma il valore della cella verrà richiesto al momento alla scacchiera,
 * quindi questi oggetti non hanno valore necessariamente costante nel tempo, però altresì
 * non richiedono aggiornamenti manuali o di essere ri-creati.
 */
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
