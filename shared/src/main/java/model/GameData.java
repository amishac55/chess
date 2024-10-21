package model;

import chess.ChessGame;

public class GameData {
    final Integer gameID;
    String whiteUsername;
    String blackUsername;
    final String gameName;
    final ChessGame chessGame;

    public GameData(Integer gameID, String gameName, ChessGame chessGame) {
        this.gameID = gameID;
        this.gameName = gameName;
        this.chessGame = chessGame;
        this.whiteUsername=null;
        this.blackUsername=null;
    }

    public GameData(Integer gameID, String whiteUsername, String blackUsername, String gameName, ChessGame chessGame) {
        this.gameID=gameID;
        this.whiteUsername=whiteUsername;
        this.blackUsername=blackUsername;
        this.gameName=gameName;
        this.chessGame=chessGame;
    }

    public GameData(String gameName, Integer gameID){
        this.gameID = gameID;
        this.whiteUsername=null;
        this.blackUsername=null;
        this.gameName=gameName;
        this.chessGame=new ChessGame();
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public void setBlackUsername(String blackUsername) {
        this.blackUsername = blackUsername;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public void setWhiteUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public Integer getGameID() {
        return gameID;
    }

    public String getGameName() {
        return gameName;
    }

    public ChessGame getChessGame() {
        return chessGame;
    }

    @Override
    public String toString() {
        return "GameData{" +
                "gameName='" + gameName + '\'' +
                ", blackUsername='" + blackUsername + '\'' +
                ", whiteUsername='" + whiteUsername + '\'' +
                ", gameID=" + gameID +
                '}';
    }
}
