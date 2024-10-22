package dataaccess.idao;

import dataaccess.DataAccessException;
import model.UserData;

public interface UserDAO {

    void createUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void removeUser(String username) throws DataAccessException;

    boolean authenticateUser(String username, String password) throws DataAccessException;

    void clearUserData() throws DataAccessException;
}
