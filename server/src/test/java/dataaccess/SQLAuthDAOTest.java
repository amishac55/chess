package dataaccess;

import dataaccess.sqldao.SQLAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SQLAuthDAOTest {

    private static SQLAuthDAO authDAO;

    @BeforeAll
    static void initializeDAO() throws DataAccessException {
        authDAO = new SQLAuthDAO();
    }

    @AfterAll
    static void cleanUp() throws DataAccessException {
        authDAO.clearAuthData();
    }

    @BeforeEach
    void resetDatabase() throws DataAccessException {
        authDAO.clearAuthData();
    }

    @Test
    void createAuth_ShouldReturnValidAuthData() throws DataAccessException {
        AuthData authData = authDAO.createAuth("testUser");
        assertNotNull(authData);
        assertEquals("testUser", authData.username());
        assertNotNull(authData.authToken());
    }

    @Test
    void getAuth_ShouldReturnAuthData_WhenTokenExists() throws DataAccessException {
        AuthData createdAuth = authDAO.createAuth("testUser");
        AuthData retrievedAuth = authDAO.getAuth(createdAuth.authToken());

        assertNotNull(retrievedAuth);
        assertEquals(createdAuth.authToken(), retrievedAuth.authToken());
        assertEquals(createdAuth.username(), retrievedAuth.username());
    }

    @Test
    void getAuth_ShouldReturnNull_WhenTokenDoesNotExist() throws DataAccessException {
        AuthData retrievedAuth = authDAO.getAuth("nonExistentToken");
        assertNull(retrievedAuth);
    }

    @Test
    void verifyAuth_ShouldReturnTrue_WhenTokenExists() throws DataAccessException {
        AuthData authData = authDAO.createAuth("testUser");
        assertTrue(authDAO.verifyAuth(authData.authToken()));
    }

    @Test
    void verifyAuth_ShouldReturnFalse_WhenTokenDoesNotExist() throws DataAccessException {
        assertFalse(authDAO.verifyAuth("nonExistentToken"));
    }

    @Test
    void deleteAuth_ShouldRemoveAuthData_WhenTokenExists() throws DataAccessException {
        AuthData authData = authDAO.createAuth("testUser");

        assertDoesNotThrow(() -> authDAO.deleteAuth(authData.authToken()));
        assertNull(authDAO.getAuth(authData.authToken()));
    }

    @Test
    void deleteAuth_ShouldNotThrow_WhenTokenDoesNotExist() {
        assertDoesNotThrow(() -> authDAO.deleteAuth("nonExistentToken"));
    }

    @Test
    void clearAuthData_ShouldRemoveAllAuthData() throws DataAccessException {
        authDAO.createAuth("user1");
        authDAO.createAuth("user2");

        assertDoesNotThrow(() -> authDAO.clearAuthData());
        assertFalse(authDAO.verifyAuth("anyToken"));
    }
}
