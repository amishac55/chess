package service;

import dataaccess.DAOFactory;
import dataaccess.DataAccessException;
import dataaccess.idao.AuthDAO;
import dataaccess.idao.GameDAO;
import model.AuthData;
import responses.ListGamesResponse;
import utils.PlayerColor;

import java.util.ArrayList;

public class GameService {
    private final GameDAO gameDao;
    private final AuthDAO authDAO;

    public GameService() {
        DAOFactory daoFactory = DAOFactory.getInstance();
        this.gameDao = daoFactory.getGameDAO();
        this.authDAO = daoFactory.getAuthDAO();
    }

    public Integer createGame(String gameName, String authToken) throws DataAccessException {
        if (!authDAO.verifyAuth(authToken)) {
            throw new DataAccessException(401, "Error: Invalid authentication token");
        }
        return this.gameDao.createGame(gameName);
    }

    public void joinGame(Integer gameID, PlayerColor requestedPlayerColor, String authToken) throws DataAccessException {
        if (!authDAO.verifyAuth(authToken)) {
            throw new DataAccessException(401, "Error: Invalid authentication token");
        }
        try {
            AuthData authData = this.authDAO.getAuth(authToken);
            this.gameDao.addPlayer(gameID, authData.username(), requestedPlayerColor);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getStatusCode(), e.getMessage());
        }
    }

    public ArrayList<ListGamesResponse.GameRecord> listGames(String authToken) throws DataAccessException {
        if (!authDAO.verifyAuth(authToken)) {
            throw new DataAccessException(401, "Error: Invalid authentication token");
        }
        ArrayList<ListGamesResponse.GameRecord> gameRecords = new ArrayList<>();
        for (var gameData : gameDao.listGames()) {
            gameRecords.add(new ListGamesResponse.GameRecord(gameData.getGameID(),
                    gameData.getWhiteUsername(), gameData.getBlackUsername(),
                    gameData.getGameName()));
        }
        return gameRecords;
    }

}
