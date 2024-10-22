package dataaccess.MemoryDAO;

import dataaccess.DataAccessException;
import dataaccess.idao.UserDAO;
import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {
    Map<String, UserData> userDB = new HashMap<>();

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try {
            userDB.put(user.username(), user);
        } catch (Exception e) {
            throw new DataAccessException(500, "Error: Internal server error. Thanks Beyonce");
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try {
            return userDB.get(username);
        } catch (Exception e) {
            throw new DataAccessException(401, "Error: " + username + " does not exist");
        }
    }

    @Override
    public void removeUser(String username) throws DataAccessException {
        try {
            userDB.remove(username);
        } catch (Exception e) {
            throw new DataAccessException(401, "Error: " + username + " does not exist");
        }
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        if (userDB.containsKey(username)) {
            UserData user = userDB.get(username);
            return user.password().equals(password);
        } else {
            throw new DataAccessException(401, "Error: username does not exist");
        }
    }

    @Override
    public void clearUserData() throws DataAccessException {
        userDB.clear();
    }
}
