package dataaccess.idao;

import dataaccess.DataAccessException;
import model.AuthData;

public interface AuthDAO {

    AuthData createAuth(String username);

    void deleteAuth(String authToken) throws DataAccessException;

    boolean verifyAuth(String authToken) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    void clearAuthData();
}
