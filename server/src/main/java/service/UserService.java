package service;

import dataaccess.DAOFactory;
import dataaccess.DataAccessException;
import dataaccess.idao.AuthDAO;
import dataaccess.idao.UserDAO;
import model.AuthData;
import model.UserData;

public class UserService {

    private final UserDAO userDao;
    private final AuthDAO authDao;

    public UserService() throws DataAccessException {
        DAOFactory daoFactory = DAOFactory.getInstance();
        this.userDao = daoFactory.getUserDAO();
        this.authDao = daoFactory.getAuthDAO();
    }

    public AuthData register(UserData userData) throws DataAccessException {
        if (userData == null || userData.username() ==null|| userData.password() ==null || userData.email()==null){
            throw new DataAccessException(400, "Error: Bad Request");
        }

        userDao.createUser(userData);
        return authDao.createAuth(userData.username());
    }

    public AuthData login(String username, String password) throws DataAccessException {
        if (username == null || password == null){
            throw new DataAccessException(400, "Error: Bad Request");
        }

        if (userDao.authenticateUser(username, password)){
            return authDao.createAuth(username);
        } else {
            throw new DataAccessException(401, "Error: Invalid credentials");
        }
    }

    public void logout(String authToken) throws DataAccessException {
        if (authDao.verifyAuth(authToken)) {
            authDao.deleteAuth(authToken);
        } else {
            throw new DataAccessException(401, "Error: Invalid authentication token");
        }
    }
}
