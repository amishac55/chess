package dataaccess.MemoryDAO;

import dataaccess.DataAccessException;
import model.GameData;
import utils.PlayerColor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements dataaccess.idao.GameDAO {
    private Map<Integer, GameData> gameDB = new HashMap<>();

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
    public void addGame(GameData gameData) throws DataAccessException {
        try {
            gameDB.put(gameData.getGameID(), gameData);
        } catch (Exception e) {
            throw new DataAccessException(500, "Error: internal server error");
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        try{
            return gameDB.values();
        } catch (Exception e) {
            throw new DataAccessException(500, "Error: internal server error");
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

    @Override
    public void addPlayer(Integer gameID, String username) throws DataAccessException {
        GameData currentGame = gameDB.get(gameID);
        if (currentGame == null) {
            throw new DataAccessException(400, "Error: bad request");
        }

        Map<PlayerColor, Boolean> availability = checkGameAvailability(gameID);

        if (!availability.get(PlayerColor.WHITE) && !availability.get(PlayerColor.BLACK)) {
            throw new DataAccessException(403, "Error: game is already full");
        }

        if (availability.get(PlayerColor.WHITE)) {
            currentGame.setWhiteUsername(username);
        } else if (availability.get(PlayerColor.BLACK)) {
            currentGame.setBlackUsername(username);
        }
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        try {
            gameDB.put(gameData.getGameID(), gameData);
        } catch (Exception e) {
            throw new DataAccessException(500, "Error: internal server error");
        }
    }

    @Override
    public void removeGame(Integer gameID) throws DataAccessException {
        try {
            gameDB.remove(gameID);
        } catch (Exception e) {
            throw new DataAccessException(400, "Error: Game does not exist");
        }
    }
}
