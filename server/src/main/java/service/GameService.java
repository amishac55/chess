package service;

import dataaccess.DAOFactory;
import dataaccess.DataAccessException;
import dataaccess.idao.AuthDAO;
import dataaccess.idao.GameDAO;
import model.AuthData;
import model.GameData;
import model.GameRecord;
import utils.PlayerColor;

import java.util.ArrayList;

public class GameService {
    private final GameDAO gameDao;
    private final AuthDAO authDAO;

    public GameService() throws DataAccessException {
        DAOFactory daoFactory = DAOFactory.getInstance();
        this.gameDao = daoFactory.getGameDAO();
        this.authDAO = daoFactory.getAuthDAO();
    }

    public Integer createGame(String gameName, String authToken) throws DataAccessException {
        validateAuthToken(authToken);
        return this.gameDao.createGame(gameName);
    }

    public void joinGame(Integer gameID, PlayerColor requestedPlayerColor, String authToken) throws DataAccessException {
        validateAuthToken(authToken);
        try {
            AuthData authData = this.authDAO.getAuth(authToken);
            this.gameDao.addPlayer(gameID, authData.username(), requestedPlayerColor);

        } catch (DataAccessException e) {
            throw new DataAccessException(e.getStatusCode(), e.getMessage());
        }
    }

    public void observeGame(Integer gameID, String authToken) throws DataAccessException {
        validateAuthToken(authToken);
        try {
            AuthData authData = this.authDAO.getAuth(authToken);
            this.gameDao.addObserver(gameID, authData.username());
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getStatusCode(), e.getMessage());
        }
    }

    public void makeMove(String authToken, GameData gameData) throws DataAccessException {
        validateAuthToken(authToken);
        updateGameData(gameData);
    }

    private void validateAuthToken(String authToken) throws DataAccessException {
        if (!authDAO.verifyAuth(authToken)) {
            throw new DataAccessException(401, "Error: Invalid authentication token");
        }
    }

    private void updateGameData(GameData gameData) throws DataAccessException {
        try {
            gameDao.updateGame(gameData);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getStatusCode(), e.getMessage());
        }
    }


    public ArrayList<GameRecord> listGames(String authToken) throws DataAccessException {
        validateAuthToken(authToken);
        return (ArrayList<GameRecord>) gameDao.listGames();
    }

    public GameData getGame(Integer gameID, String authToken) throws DataAccessException {
        validateAuthToken(authToken);
        try {
            GameData game = gameDao.getGame(gameID);
            if (game == null) {
                throw new DataAccessException(404, "Error: Game not found");
            }
            return game;
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getStatusCode(), e.getMessage());
        }
    }

}
