package chess;
import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board[r][c] = null;
            }
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                this.board[r][c] = null;
            }
        }
        // Initialize white pieces
        initializePieces(ChessGame.TeamColor.WHITE, 0, 1);

        // Initialize black pieces
        initializePieces(ChessGame.TeamColor.BLACK, 7, 6);
    }
    private void placePiece(ChessGame.TeamColor color, ChessPiece.PieceType pieceType, int row, int col) {
        board[row][col] = new ChessPiece(color, pieceType);
    }
    private void initializePieces(ChessGame.TeamColor color, int mainRow, int pawnRow) {
        // Rooks
        placePiece(color, ChessPiece.PieceType.ROOK, mainRow, 0);
        placePiece(color, ChessPiece.PieceType.ROOK, mainRow, 7);

        // Knights
        placePiece(color, ChessPiece.PieceType.KNIGHT, mainRow, 1);
        placePiece(color, ChessPiece.PieceType.KNIGHT, mainRow, 6);

        // Bishops
        placePiece(color, ChessPiece.PieceType.BISHOP, mainRow, 2);
        placePiece(color, ChessPiece.PieceType.BISHOP, mainRow, 5);

        // Queen
        placePiece(color, ChessPiece.PieceType.QUEEN, mainRow, 3);

        // King
        placePiece(color, ChessPiece.PieceType.KING, mainRow, 4);

        // Pawns
        for (int col = 0; col < 8; col++) {
            placePiece(color, ChessPiece.PieceType.PAWN, pawnRow, col);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ChessBoard otherBoard = (ChessBoard) obj;
        return Arrays.deepEquals(this.board, otherBoard.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(this.board);
    }


}