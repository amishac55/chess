package chess;
import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int r;
    private final int c;

    public ChessPosition(int r, int c) {
        this.r = r;
        this.c = c;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return r;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return c;
    }

    public String toString() {
        return "(" + r + "," + c + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPosition other = (ChessPosition) o;
        return r == other.r && c == other.c;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, c);
    }

}