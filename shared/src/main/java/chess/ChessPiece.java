
package chess;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import chess.ChessGame.TeamColor;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece implements Cloneable {
    private final TeamColor pieceColor;
    private final PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceType = type;
        this.pieceColor = pieceColor;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(chess.ChessBoard board, ChessPosition myPosition) {

        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPiece currentPiece = board.getPiece(myPosition);

        if (currentPiece == null) {
            return validMoves;
        }

        switch (currentPiece.getPieceType()) {
            case PAWN:
                validMoves.addAll(getPawnMoves(board, myPosition, currentPiece.getTeamColor()));
                break;
            case BISHOP:
                validMoves.addAll(getDirectionalMoves(board, myPosition, currentPiece.getTeamColor(),
                        1, 1, -1, -1, 1, -1, -1, 1));
                break;
            case ROOK:
                validMoves.addAll(getDirectionalMoves(board, myPosition, currentPiece.getTeamColor(),
                        1, 0, -1, 0, 0, 1, 0, -1));
                break;
            case QUEEN:
                validMoves.addAll(getDirectionalMoves(board, myPosition, currentPiece.getTeamColor(),
                        1, 1, -1, -1, 1, -1, -1, 1, 1, 0, -1, 0, 0, 1, 0, -1));
                break;
            case KNIGHT:
                validMoves.addAll(getKnightMoves(board, myPosition, currentPiece.getTeamColor()));
                break;
            case KING:
                validMoves.addAll(getKingMoves(board, myPosition, currentPiece.getTeamColor()));
                break;
        }

        return validMoves;
    }

    private Collection<ChessMove> getPawnMoves(chess.ChessBoard board, ChessPosition position, ChessGame.TeamColor teamColor) {
        Collection<ChessMove> pawnMoves = new ArrayList<>();
        int forwardRow = (teamColor == ChessGame.TeamColor.WHITE) ? position.getRow() + 1 : position.getRow() - 1;
        int currentCol = position.getColumn();

        //checks if Pawn can attack an Opponent piece
        checkPawnAttack(board, position, teamColor, pawnMoves, forwardRow, currentCol + 1);
        checkPawnAttack(board, position, teamColor, pawnMoves, forwardRow, currentCol - 1);

        // Move forward
        if (isValidPosition(forwardRow, currentCol) && board.getPiece(new ChessPosition(forwardRow, currentCol)) == null) {
            addPawnMove(pawnMoves, position, forwardRow, currentCol, teamColor);
        }

        // Double move for pawns on starting row
        if (isInitialPawnRow(position, teamColor)) {
            int doubleMoveRow = (teamColor == ChessGame.TeamColor.WHITE) ? forwardRow + 1 : forwardRow - 1;

            // Check if the square directly in front is empty
            if (isValidPosition(forwardRow, currentCol) && board.getPiece(new ChessPosition(forwardRow, currentCol)) == null) {

                // If the square in front is empty, check for the double move
                if (isValidPosition(doubleMoveRow, currentCol) && board.getPiece(new ChessPosition(doubleMoveRow, currentCol)) == null) {
                    pawnMoves.add(new ChessMove(position, new ChessPosition(doubleMoveRow, currentCol), null));
                }
            }
        }

        return pawnMoves;
    }

    private void checkPawnAttack(chess.ChessBoard board, ChessPosition position, ChessGame.TeamColor teamColor,
                                 Collection<ChessMove> pawnMoves, int forwardRow, int attackCol) {
        if (isValidPosition(forwardRow, attackCol)) {
            ChessPiece targetPiece = board.getPiece(new ChessPosition(forwardRow, attackCol));
            if (targetPiece != null && targetPiece.getTeamColor() != teamColor) {
                addPawnMove(pawnMoves, position, forwardRow, attackCol, teamColor);
            }
        }
    }

    private void addPawnMove(Collection<ChessMove> pawnMoves, ChessPosition from, int row, int col, ChessGame.TeamColor teamColor) {
        ChessPosition to = new ChessPosition(row, col);
        if (row == 1 || row == 8) {
            // Promotion
            pawnMoves.add(new ChessMove(from, to, PieceType.QUEEN));
            pawnMoves.add(new ChessMove(from, to, PieceType.ROOK));
            pawnMoves.add(new ChessMove(from, to, PieceType.BISHOP));
            pawnMoves.add(new ChessMove(from, to, PieceType.KNIGHT));
        } else {
            pawnMoves.add(new ChessMove(from, to, null));
        }
    }

    private boolean isInitialPawnRow(ChessPosition position, ChessGame.TeamColor teamColor) {
        return (teamColor == ChessGame.TeamColor.WHITE && position.getRow() == 2) ||
                (teamColor == ChessGame.TeamColor.BLACK && position.getRow() == 7);
    }

    //for Queen, Rook, Bishop
    private Collection<ChessMove> getDirectionalMoves(chess.ChessBoard board, ChessPosition position, ChessGame.TeamColor teamColor,
                                                      int... directions) {
        Collection<ChessMove> moves = new ArrayList<>();
        for (int i = 0; i < directions.length; i += 2) {
            moves.addAll(getMovesInDirection(board, position, teamColor, directions[i], directions[i + 1]));
        }
        return moves;
    }

    private Collection<ChessMove> getMovesInDirection(chess.ChessBoard board, ChessPosition position, ChessGame.TeamColor teamColor,
                                                      int rowDelta, int colDelta) {
        Collection<ChessMove> moves = new ArrayList<>();
        int row = position.getRow();
        int col = position.getColumn();
        while (true) {
            row += rowDelta;
            col += colDelta;

            if (!isValidPosition(row, col)) {
                break;
            }

            ChessPosition newPosition = new ChessPosition(row, col);
            ChessPiece targetPiece = board.getPiece(newPosition);

            if (targetPiece == null) {
                moves.add(new ChessMove(position, newPosition, null));
            } else {
                if (targetPiece.getTeamColor() != teamColor) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
                break; // Blocked by a piece
            }
        }
        return moves;
    }

    private Collection<ChessMove> getKnightMoves(chess.ChessBoard board, ChessPosition position, ChessGame.TeamColor teamColor) {
        Collection<ChessMove> knightMoves = new ArrayList<>();
        int[][] knightOffsets = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] offset : knightOffsets) {
            int newRow = position.getRow() + offset[0];
            int newCol = position.getColumn() + offset[1];

            if (isValidPosition(newRow, newCol)) {
                ChessPiece targetPiece = board.getPiece(new ChessPosition(newRow, newCol));
                if (targetPiece == null || targetPiece.getTeamColor() != teamColor) {
                    knightMoves.add(new ChessMove(position, new ChessPosition(newRow, newCol), null));
                }
            }
        }
        return knightMoves;
    }

    private Collection<ChessMove> getKingMoves(chess.ChessBoard board, ChessPosition position, ChessGame.TeamColor teamColor) {
        Collection<ChessMove> kingMoves = new ArrayList<>();
        int[][] kingOffsets = {
                {0, 1}, {0, -1}, {1, 0}, {-1, 0},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] offset : kingOffsets) {
            int newRow = position.getRow() + offset[0];
            int newCol = position.getColumn() + offset[1];

            if (isValidPosition(newRow, newCol)) {
                ChessPiece targetPiece = board.getPiece(new ChessPosition(newRow, newCol));
                if (targetPiece == null || targetPiece.getTeamColor() != teamColor) {
                    kingMoves.add(new ChessMove(position, new ChessPosition(newRow, newCol), null));
                }
            }
        }
        return kingMoves;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 1      && row <= 8 && col >= 1 && col <= 8;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ChessPiece other = (ChessPiece) obj;
        return pieceColor == other.pieceColor && pieceType == other.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, pieceType);
    }

    @Override
    public ChessPiece clone() {
        try {
            ChessPiece clone = (ChessPiece) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
