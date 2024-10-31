package dataaccess;

import dataaccess.sqldao.SQLUserDAO;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SQLUserDAOTest {

    private static SQLUserDAO userDAO;
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeAll
    static void setUp() throws DataAccessException {
        userDAO = new SQLUserDAO();
    }

    @AfterAll
    static void tearDown() throws DataAccessException {
        userDAO.clearUserData();
    }

    @BeforeEach
    void clearDatabase() throws DataAccessException {
        userDAO.clearUserData();
    }

    @Test
    void createUserShouldCreateUserSuccessfully() throws DataAccessException {
        UserData user = createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);

        assertDoesNotThrow(() -> userDAO.createUser(user));
        verifyUserExists(TEST_USERNAME, TEST_EMAIL);
    }

    @Test
    void createUserShouldThrowExceptionForDuplicateUsername() throws DataAccessException {
        userDAO.createUser(createUser(TEST_USERNAME, TEST_PASSWORD, "test1@example.com"));
        assertThrows(DataAccessException.class, () -> userDAO.createUser(createUser(TEST_USERNAME, "password456", "test2@example.com")));
    }

    @Test
    void getUserShouldReturnExistingUser() throws DataAccessException {
        userDAO.createUser(createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL));
        verifyUserExists(TEST_USERNAME, TEST_EMAIL);
    }

    @Test
    void getUserShouldReturnNullForNonExistentUser() throws DataAccessException {
        assertNull(userDAO.getUser("nonExistentUser"));
    }

    @Test
    void authenticateUserShouldAuthenticateWithCorrectPassword() throws DataAccessException {
        userDAO.createUser(createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL));
        assertTrue(userDAO.authenticateUser(TEST_USERNAME, TEST_PASSWORD));
    }

    @Test
    void authenticateUserShouldNotAuthenticateWithIncorrectPassword() throws DataAccessException {
        userDAO.createUser(createUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL));
        assertFalse(userDAO.authenticateUser(TEST_USERNAME, "wrongPassword"));
    }

    @Test
    void authenticateUserShouldThrowExceptionForNonExistentUser() {
        assertThrows(DataAccessException.class, () -> userDAO.authenticateUser("nonExistentUser", "password123"));
    }

    @Test
    void clearUserDataShouldClearAllUserData() throws DataAccessException {
        userDAO.createUser(createUser("user1", "password1", "user1@example.com"));
        userDAO.createUser(createUser("user2", "password2", "user2@example.com"));

        assertDoesNotThrow(userDAO::clearUserData);
        assertNull(userDAO.getUser("user1"));
        assertNull(userDAO.getUser("user2"));
    }

    private UserData createUser(String username, String password, String email) {
        return new UserData(username, password, email);
    }

    private void verifyUserExists(String username, String email) throws DataAccessException {
        UserData retrievedUser = userDAO.getUser(username);
        assertNotNull(retrievedUser);
        assertEquals(username, retrievedUser.username());
        assertEquals(email, retrievedUser.email());
    }
}
