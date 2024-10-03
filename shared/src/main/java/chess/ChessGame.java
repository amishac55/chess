package chess;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor color;
    private ChessBoard board;

    public ChessGame() {
        this.color = TeamColor.WHITE;
        this.board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.color;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.color = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {

        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return Collections.emptyList(); // Return an empty list if piece is null
        }

        ChessBoard boardCopy = board.clone();
        Collection<ChessMove> possibleMoves = piece.pieceMoves(boardCopy, startPosition);
        chess.ChessGame.TeamColor team = piece.getTeamColor();

        List<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : possibleMoves) {
            // Check if the move does not leave the team in check
            if (!badMove(boardCopy, move, team)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    private boolean badMove(ChessBoard board, ChessMove move, chess.ChessGame.TeamColor team) {
        ChessBoard boardCopy = board.clone();
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessPiece piece = boardCopy.getPiece(startPos);
        boardCopy.addPiece(endPos, piece);
        boardCopy.addPiece(startPos, null);

        return isInCheck(team, boardCopy);
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPos = move.getStartPosition();
        ChessPiece piece = board.getPiece(startPos);

        if (piece == null || !piece.getTeamColor().equals(getTeamTurn()) || !validMoves(startPos).contains(move)) {
            throw new InvalidMoveException();
        }

        ChessPosition end
        Pos = move.getEndPosition();
        ChessPiece.PieceType promotion = move.getPromotionPiece();

        if (promotion != null) {
            piece = new ChessPiece(piece.getTeamColor(), promotion);
        }

        board.addPiece(endPos, piece);
        board.addPiece(startPos, null);
        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {

        return isInCheck(teamColor, board);
    }

    public boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        ChessPosition kingPosition = findKing(teamColor, board);
        Set<ChessPosition> enemyMoves = getOpponentMoves(teamColor, board);
        return enemyMoves.contains(kingPosition);
    }

    public Set<ChessPosition> getOpponentMoves(TeamColor teamColor, ChessBoard board) {
        Set<ChessPosition> enemyMoves = new HashSet<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(board, new ChessPosition(row, col));
                    moves.forEach(move -> enemyMoves.add(move.getEndPosition()));
                }
            }
        }
        return enemyMoves;

        private ChessPosition findKing(TeamColor teamColor, ChessBoard board) {
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                    if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                        return new ChessPosition(row, col);
                    }
                }
            }
            return null;
        }

        /**
         * Determines if the given team is in checkmate
         *
         * @param teamColor which team to check for checkmate
         * @return True if the specified team is in checkmate
         */
        public boolean isInCheckmate (TeamColor teamColor){
            if (!isInCheck(teamColor, board)) {
                return false;
            }

            return isMoveAvailable(teamColor);
        }

        /**
         * Determines if the given team is in stalemate, which here is defined as having
         * no valid moves
         *
         * @param teamColor which team to check for stalemate
         * @return True if the specified team is in stalemate, otherwise false
         */
        public boolean isInStalemate (TeamColor teamColor){

            if (isInCheck(teamColor, board)) {
                return false;
            }
            return isMoveAvailable(teamColor);
            ;
        }


        private boolean isMoveAvailable (TeamColor teamColor){
            for (int row = 1; row <= 8; row++) {
                for (int col = 1; col <= 8; col++) {
                    ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                    if (piece != null && piece.getTeamColor() == teamColor) {
                        Collection<ChessMove> moves = validMoves(new ChessPosition(row, col));
                        if (!moves.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        /**
         * Sets this game's chessboard with a given board
         *
         * @param board the new board to use
         */
        public void setBoard (ChessBoard board){
            this.board = board;
        }

        /**
         * Gets the current chessboard
         *
         * @return the chessboard
         */
        public ChessBoard getBoard () {
            return this.board;
        }
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}