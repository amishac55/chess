package dataaccess.memorydao;

import dataaccess.DataAccessException;
import model.GameData;
import model.GameRecord;
import utils.PlayerColor;

import java.util.*;

public class MemoryGameDAO implements dataaccess.idao.GameDAO {
    private Map<Integer, GameData> gameDB = new HashMap<>();
    private Map<Integer, ArrayList<String>> observerDB = new HashMap<>();

    @Override
    public void addObserver(Integer gameID, String username) throws DataAccessException {
        ArrayList<String> observers = observerDB.get(gameID);
        observers.add(username);
        observerDB.put(gameID, observers);
    }

    @Override
    public List<String> getObservers(Integer gameID) throws DataAccessException {
        return observerDB.get(gameID);
    }

    @Override
    public void clearGameData() throws DataAccessException {
        try {
            gameDB.clear();
        } catch (Exception e) {
            throw new DataAccessException(500, "Error: internal server error");
        }
    }

    @Override
    public GameData getGame(Integer gameID) throws DataAccessException {
        try {
            return gameDB.get(gameID);
        } catch (Exception e) {
            throw new DataAccessException(400, "Error: Game does not exist");
        }
    }

    @Override
    public Integer addGame(GameData gameData) throws DataAccessException {
        try {
            gameDB.put(gameData.getGameID(), gameData);
            return gameData.getGameID();
        } catch (Exception e) {
            throw new DataAccessException(500, "Error: internal server error");
        }
    }

    @Override
    public Collection<GameRecord> listGames() throws DataAccessException {
        Collection<GameRecord> gameRecords = new ArrayList<>();
        try{
            for (GameData game : gameDB.values()) {
                Integer gameID = game.getGameID();
                List<String> observers = observerDB.get(gameID);
                GameRecord gameRecord = new GameRecord(game.getGameID(), game.getWhiteUsername(), game.getBlackUsername(),
                        game.getGameName(), observers);
                gameRecords.add(gameRecord);
            }
        } catch (Exception e) {
            throw new DataAccessException(500, "Error: internal server error");
        }
        return gameRecords;
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        addGame(gameData);
    }

    @Override
    public void deleteGame(Integer gameID) throws DataAccessException {
        try{
            gameDB.remove(gameID);
        } catch (Exception e) {
            throw new DataAccessException(500, "Error: game with " + gameID.toString() + " does not exist");
        }
    }

    public Map<PlayerColor, Boolean> checkGameAvailability(Integer gameID) throws DataAccessException {
        GameData game = gameDB.get(gameID);
        if (game == null) {
            throw new DataAccessException(404, "Error: game not found");
        }
        Map<PlayerColor, Boolean> availability = new HashMap<>();
        availability.put(PlayerColor.WHITE, game.getWhiteUsername() == null);
        availability.put(PlayerColor.BLACK, game.getBlackUsername() == null);
        return availability;
    }

    @Override
    public void addPlayer(Integer gameID, String username, PlayerColor requestedPlayerColor) throws DataAccessException {
        GameData currentGame = gameDB.get(gameID);
        if (currentGame == null || requestedPlayerColor == null) {
            throw new DataAccessException(400, "Error: bad request");
        }

        Map<PlayerColor, Boolean> availability = checkGameAvailability(gameID);

        if (!availability.get(PlayerColor.WHITE) && !availability.get(PlayerColor.BLACK)) {
            throw new DataAccessException(403, "Error: game is already full");
        }

        if (requestedPlayerColor.equals(PlayerColor.BLACK) || requestedPlayerColor.equals(PlayerColor.WHITE)) {
            if (!availability.get(requestedPlayerColor)) {
                throw new DataAccessException(403, "Error: " + requestedPlayerColor + " is already taken. Please choose the alternate color.");
            }
            if (requestedPlayerColor.equals(PlayerColor.BLACK)) {
                currentGame.setBlackUsername(username);
            } else {
                currentGame.setWhiteUsername(username);
            }
        } else {
            throw new DataAccessException(400, "Error: bad request");
        }
    }

}
