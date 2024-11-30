package dataaccess.idao;

import dataaccess.DataAccessException;
import model.GameData;
import model.GameRecord;
import utils.PlayerColor;

import java.util.Collection;
import java.util.List;

public interface GameDAO {
    void addObserver(Integer gameID, String username) throws DataAccessException;

    List<String> getObservers(Integer gameID) throws DataAccessException;

    void clearGameData() throws DataAccessException;

    GameData getGame(Integer gameID) throws DataAccessException;

    Integer addGame(GameData gameData) throws DataAccessException;

    Collection<GameRecord> listGames() throws DataAccessException;

    void updateGame(GameData gameData) throws DataAccessException;

    void deleteGame(Integer gameID) throws DataAccessException;

    void addPlayer(Integer gameID, String username, PlayerColor requestedPlayerColor) throws DataAccessException;

    default Integer createGame(String gameName) throws DataAccessException {
        GameData newGameData = new GameData(gameName);
        return addGame(newGameData);
    }
}
