package service;

import dataaccess.DAOFactory;
import dataaccess.DataAccessException;
import dataaccess.IDAO.AuthDAO;
import dataaccess.IDAO.GameDAO;
import dataaccess.IDAO.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClearServiceTest {

    private ClearService clearService;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;
    private AuthData authData;

    @BeforeEach
    void setUp() throws DataAccessException {
        DAOFactory factory = DAOFactory.getInstance();
        authDAO = factory.getAuthDAO();
        gameDAO = factory.getGameDAO();
        userDAO = factory.getUserDAO();
        clearService = new ClearService();

        userDAO.createUser(new UserData("user", "pass", "user@test.com"));
        authData = authDAO.createAuth("user");
        gameDAO.createGame("game");
    }

    @AfterEach
    void tearDown() throws DataAccessException {
        clearService.clearAllData();
    }

    @Test
    void testClearAllDataSuccess() throws DataAccessException {
        assertNotNull(userDAO.getUser("user"));
        assertNotNull(authDAO.getAuth(authData.authToken()));
        assertFalse(gameDAO.listGames().isEmpty());

        clearService.clearAllData();

        assertNull(userDAO.getUser("user"));
        assertNull(authDAO.getAuth(authData.authToken()));
        assertTrue(gameDAO.listGames().isEmpty());
    }
}