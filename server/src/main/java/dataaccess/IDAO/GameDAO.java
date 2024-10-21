package dataaccess.IDAO;

import chess.ChessBoard;
import chess.ChessGame;
import dataaccess.DataAccessException;
import model.GameData;
import utils.PlayerColor;
import utils.UniqueIDGenerator;

import java.util.Collection;
import java.util.Map;

public interface GameDAO {
    void clearGameData() throws DataAccessException;

    GameData getGame(Integer gameID) throws DataAccessException;

    void addGame(GameData gameData) throws DataAccessException;

    Collection<GameData> listGames() throws DataAccessException;

    void addPlayer(Integer gameID, String username, PlayerColor requestedPlayerColor) throws DataAccessException;

    void addPlayer(Integer gameID, String username) throws DataAccessException;

    public void updateGame(GameData gameData) throws DataAccessException;

    void removeGame(Integer gameID) throws DataAccessException;

    default Integer createGame(String gameName) throws DataAccessException {
        Integer gameID = UniqueIDGenerator.generateUniqueId();
        ChessGame newGame = new ChessGame();
        newGame.setBoard(new ChessBoard());
        GameData newGameData = new GameData(gameID, gameName, newGame);
        addGame(newGameData);
        return gameID;
    }
}
