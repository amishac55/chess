package dataaccess;

import dataaccess.IDAO.AuthDAO;
import dataaccess.IDAO.UserDAO;
import dataaccess.IDAO.GameDAO;
import dataaccess.MemoryDAO.MemoryAuthDAO;
import dataaccess.MemoryDAO.MemoryGameDAO;
import dataaccess.MemoryDAO.MemoryUserDAO;

public class DAOFactory {
    private static DAOFactory instance;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    private DAOFactory() {
        // Initialize your DAOs here
        this.userDAO = new MemoryUserDAO();
        this.authDAO = new MemoryAuthDAO();
        this.gameDAO = new MemoryGameDAO();
    }

    public static synchronized DAOFactory getInstance() {
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
