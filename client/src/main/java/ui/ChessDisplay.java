package ui;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class ChessDisplay {
    private static final String EMPTY_SQUARE = "   ";
    private static final int BOARD_SIZE = 8;
    private ChessGame game;

    public ChessDisplay(ChessGame game) {
        this.game = game;
    }

    public void updateGame(ChessGame game) {
        this.game = game;
    }

    public void displayBoard(ChessGame.TeamColor perspective, ChessPosition selectedPos) {
        StringBuilder output = new StringBuilder(EscapeSequences.SET_TEXT_BOLD);
        HashSet<ChessPosition> validMoveSquares = getValidMoveSquares(selectedPos);
        boolean isReversed = perspective == ChessGame.TeamColor.BLACK;
        int viewCount = perspective == null ? 2 : 1;

        for (int view = 0; view < viewCount; view++) {
            renderBoard(output, isReversed, selectedPos, validMoveSquares);
            if (view < viewCount - 1) {
                output.append("\n");
            }
            isReversed = !isReversed;
        }

        output.append(EscapeSequences.RESET_TEXT_BOLD_FAINT);
        out.println(output);
        out.printf("Turn: %s\n", game.getTeamTurn());
    }

    private void renderBoard(StringBuilder output, boolean isReversed,
                             ChessPosition selectedPos, HashSet<ChessPosition> validMoveSquares) {
        renderFileLabels(output, isReversed);
        renderBoardRows(output, isReversed, selectedPos, validMoveSquares);
        renderFileLabels(output, isReversed);
    }

    private HashSet<ChessPosition> getValidMoveSquares(ChessPosition selectedPos) {
        if (selectedPos == null) {
            return new HashSet<>();
        }

        Collection<ChessMove> validMoves = game.validMoves(selectedPos);
        HashSet<ChessPosition> validSquares = HashSet.newHashSet(validMoves.size());
        validMoves.forEach(move -> validSquares.add(move.getEndPosition()));
        return validSquares;
    }

    private void renderFileLabels(StringBuilder output, boolean isReversed) {
        output.append(EscapeSequences.SET_BG_COLOR_BLACK)
                .append(SET_TEXT_COLOR_YELLOW)
                .append(getFileLabels(isReversed))
                .append(EscapeSequences.RESET_BG_COLOR)
                .append(EscapeSequences.RESET_TEXT_COLOR)
                .append("\n");
    }

    private String getFileLabels(boolean isReversed) {
        return isReversed ? "    h  g  f  e  d  c  b  a    "
                : "    a  b  c  d  e  f  g  h    ";
    }

    private void renderBoardRows(StringBuilder output, boolean isReversed,
                                 ChessPosition selectedPos, HashSet<ChessPosition> validMoveSquares) {
        for (int i = BOARD_SIZE; i > 0; i--) {
            int row = isReversed ? (i * -1) + 9 : i;
            renderRow(output, row, isReversed, selectedPos, validMoveSquares);
        }
    }

    private void renderRow(StringBuilder output, int row, boolean isReversed,
                           ChessPosition selectedPos, HashSet<ChessPosition> validMoveSquares) {
        appendRankLabel(output, row);

        for (int col = 1; col <= BOARD_SIZE; col++) {
            int column = isReversed ? (col * -1) + 9 : col;
            String bgColor = determineSquareColor(row, column, selectedPos, validMoveSquares);
            output.append(bgColor)
                    .append(renderPiece(row, column))
                    .append(EscapeSequences.RESET_BG_COLOR);
        }

        appendRankLabel(output, row);
        output.append("\n");
    }

    private void appendRankLabel(StringBuilder output, int rank) {
        output.append(EscapeSequences.SET_BG_COLOR_BLACK)
                .append(SET_TEXT_COLOR_YELLOW)
                .append(" ").append(rank).append(" ")
                .append(EscapeSequences.RESET_BG_COLOR)
                .append(EscapeSequences.RESET_TEXT_COLOR);
    }

    private String determineSquareColor(int row, int column,
                                        ChessPosition selectedPos, HashSet<ChessPosition> validMoveSquares) {
        ChessPosition square = new ChessPosition(row, column);
        if (square.equals(selectedPos)) {
            return EscapeSequences.SET_BG_COLOR_BLUE;
        }
        if (validMoveSquares.contains(square)) {
            return SET_BG_COLOR_DARK_GREEN;
        }
        return ((row + column) % 2 == 1) ? SET_BG_COLOR_LIGHT_GREY
                : SET_BG_COLOR_DARK_GREY;
    }

    private String renderPiece(int row, int column) {
        ChessPiece piece = game.getBoard().getPiece(new ChessPosition(row, column));
        if (piece == null){
            return EMPTY_SQUARE;
        }

        return piece.getTeamColor() == ChessGame.TeamColor.WHITE
                ? renderWhitePiece(piece.getPieceType())
                : renderBlackPiece(piece.getPieceType());
    }

    private String renderWhitePiece(ChessPiece.PieceType type) {
        return EscapeSequences.SET_TEXT_COLOR_WHITE + getPieceSymbol(type) +
                EscapeSequences.RESET_TEXT_COLOR;
    }

    private String renderBlackPiece(ChessPiece.PieceType type) {
        return EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_TEXT_BOLD +
                getPieceSymbol(type) +
                EscapeSequences.RESET_TEXT_BOLD_FAINT +
                EscapeSequences.RESET_TEXT_COLOR;
    }

    private String getPieceSymbol(ChessPiece.PieceType type) {
        return switch (type) {
            case KING -> " K ";
            case QUEEN -> " Q ";
            case BISHOP -> " B ";
            case KNIGHT -> " N ";
            case ROOK -> " R ";
            case PAWN -> " P ";
        };
    }
}
