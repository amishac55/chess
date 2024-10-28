package dataaccess;

import dataaccess.idao.AuthDAO;
import dataaccess.idao.UserDAO;
import dataaccess.idao.GameDAO;

import dataaccess.sqldao.SQLAuthDAO;
import dataaccess.sqldao.SQLGameDAO;
import dataaccess.sqldao.SQLUserDAO;

public class DAOFactory {
    private static DAOFactory instance;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    private DAOFactory() throws DataAccessException {
        this.userDAO = new SQLUserDAO();
        this.authDAO = new SQLAuthDAO();
        this.gameDAO = new SQLGameDAO();
    }

    public static synchronized DAOFactory getInstance() throws DataAccessException {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    public static synchronized void setInstance(DAOFactory daoFactory) {
        instance = daoFactory;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public AuthDAO getAuthDAO() {
        return authDAO;
    }

    public GameDAO getGameDAO() {
        return gameDAO;
    }
}
