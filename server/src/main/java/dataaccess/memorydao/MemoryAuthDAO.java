package dataaccess.memorydao;
import dataaccess.DataAccessException;
import dataaccess.idao.AuthDAO;
import model.AuthData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    public Map<String, AuthData> authDB =new HashMap<>();

    @Override
    public AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authDB.put(authToken, authData);
        return authData;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try {
            authDB.remove(authToken);
        } catch (Exception e) {
            throw new DataAccessException(400, "Error: username does not exist");
        }
    }

    @Override
    public boolean verifyAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException(401, "Invalid authentication token");
        }
        try {
            return authDB.containsKey(authToken);
        } catch (Exception e) {
            throw new DataAccessException(400, "username does not exist");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try {
            return authDB.get(authToken);
        } catch (Exception e) {
            throw new DataAccessException(400, "Error: username does not exist");
        }
    }

    @Override
    public void clearAuthData() {
        authDB.clear();
    }
}
