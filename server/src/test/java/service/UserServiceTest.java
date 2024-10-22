package service;

import dataaccess.DAOFactory;
import dataaccess.DataAccessException;
import dataaccess.idao.AuthDAO;
import dataaccess.idao.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userServiceToTest;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        DAOFactory factory = DAOFactory.getInstance();
        userDAO = factory.getUserDAO();
        authDAO = factory.getAuthDAO();
        userServiceToTest = new UserService();

        // Clear all data before each test
        new ClearService().clearAllData();
    }

    @AfterEach
    void tearDown() throws DataAccessException {
        // Clear all data after each test
        new ClearService().clearAllData();
    }

    @Test
    void testRegisterPass() throws DataAccessException {
        UserData user = new UserData("user", "pass", "user@test.com");
        AuthData authData = userServiceToTest.register(user);

        assertNotNull(authData);
        assertEquals("user", authData.username());
        assertNotNull(userDAO.getUser("user"));
    }

    @Test
    void shouldThrowExceptionOnRegisterWhenUsernameTaken() throws DataAccessException {
        UserData existingUser = new UserData("existingUser", "password", "existing@test.com");
        userServiceToTest.register(existingUser);

        assertThrows(DataAccessException.class, () -> userServiceToTest.register(existingUser));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringNull() {
        assertThrows(DataAccessException.class, () -> userServiceToTest.register(null));
    }

    @Test
    void testLoginPass() throws DataAccessException {
        String username = "user";
        String password = "pass";
        UserData user = new UserData(username, password, "user@test.com");
        userServiceToTest.register(user);

        AuthData returnAuth = userServiceToTest.login(username, password);
        assertNotNull(returnAuth);
        assertEquals(username, returnAuth.username());
    }

    @Test
    void shouldThrowExceptionWhenLoginWithInvalidCredentials() throws DataAccessException {
        String username = "user";
        String password = "pass";
        String wrongPassword = "abcd";
        UserData user = new UserData(username, password, "user@test.com");
        userServiceToTest.register(user);

        assertThrows(DataAccessException.class, () -> userServiceToTest.login(username, wrongPassword));
    }

    @Test
    void shouldThrowExceptionWhenLoginWithNull() {
        assertThrows(DataAccessException.class, () -> userServiceToTest.login(null, "pass"));
        assertThrows(DataAccessException.class, () -> userServiceToTest.login("user", null));
    }

    @Test
    void testLogoutSuccess() throws DataAccessException {
        UserData newUser = new UserData("user", "pass", "user@test.com");
        AuthData auth = userServiceToTest.register(newUser);

        assertDoesNotThrow(() -> userServiceToTest.logout(auth.authToken()));
        assertNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    void shouldThrowExceptionWhenLogoutWithInvalidToken() {
        String invalidAuthToken = "abcd";
        assertThrows(DataAccessException.class, () -> userServiceToTest.logout(invalidAuthToken));
    }
}