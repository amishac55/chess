package service;

import dataaccess.DAOFactory;
import dataaccess.DataAccessException;
import dataaccess.IDAO.AuthDAO;
import dataaccess.IDAO.GameDAO;
import dataaccess.IDAO.UserDAO;

public class ClearService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;

    public ClearService() {
        DAOFactory daoFactory = DAOFactory.getInstance();
        this.authDAO = daoFactory.getAuthDAO();
        this.gameDAO = daoFactory.getGameDAO();
        this.userDAO = daoFactory.getUserDAO();
    }

    public void clearAllData() throws DataAccessException {
        try {
            this.authDAO.clearAuthData();
            this.gameDAO.clearGameData();
            this.userDAO.clearUserData();
        } catch (DataAccessException e) {
            throw new DataAccessException(500, "Error clearing data: " + e.getMessage());
        }
    }
}
