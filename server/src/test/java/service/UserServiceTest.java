package service;

import dataaccess.DAOFactory;
import dataaccess.DataAccessException;
import dataaccess.IDAO.AuthDAO;
import dataaccess.IDAO.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserDAO mockUserDao;
    @Mock
    private AuthDAO mockAuthDao;
    @Mock
    private DAOFactory mockDaoFactory;

    private UserService userServiceToTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockDaoFactory.getUserDAO()).thenReturn(mockUserDao);
        when(mockDaoFactory.getAuthDAO()).thenReturn(mockAuthDao);
        DAOFactory.setInstance(mockDaoFactory);
        userServiceToTest = new UserService();
    }

    @Test
    void testRegisterPass() throws DataAccessException {
        UserData newUser = new UserData("newUser", "password123", "newuser@example.com");
        AuthData expectedAuth = new AuthData("authToken123", "newUser");
        when(mockUserDao.getUser("newUser")).thenReturn(null);
        when(mockAuthDao.createAuth("newUser")).thenReturn(expectedAuth);

        AuthData actualAuth = userServiceToTest.register(newUser);
        assertEquals(expectedAuth, actualAuth);
        verify(mockUserDao).createUser(newUser);
    }

    @Test
    void shouldThrowExceptionOnRegisterWhenUsernameTaken() throws DataAccessException {
        UserData existingUser = new UserData("existingUser", "password123", "existing@example.com");
        when(mockUserDao.getUser("existingUser")).thenReturn(existingUser);
        assertThrows(DataAccessException.class, () -> userServiceToTest.register(existingUser));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringNull() {
        assertThrows(DataAccessException.class, () -> userServiceToTest.register(null));
    }

    @Test
    void testLoginPass() throws DataAccessException {
        String username = "testuser";
        String password = "password";
        AuthData expectedAuth = new AuthData("authToken123", username);

        when(mockUserDao.authenticateUser(username, password)).thenReturn(true);
        when(mockAuthDao.createAuth(username)).thenReturn(expectedAuth);

        AuthData result = userServiceToTest.login(username, password);
        assertEquals(expectedAuth, result);
    }

    @Test
    void shouldThrowExceptionWhenLoginWithInvalidCredentials() throws DataAccessException {
        String username = "testUser";
        String incorrectPassword = "wrongPassword";
        when(mockUserDao.authenticateUser(username, incorrectPassword)).thenReturn(false);
        assertThrows(DataAccessException.class, () -> userServiceToTest.login(username, incorrectPassword));
    }

    @Test
    void shouldThrowExceptionWhenLoginWithNull() {
        assertThrows(DataAccessException.class, () -> userServiceToTest.login(null, "password"));
        assertThrows(DataAccessException.class, () -> userServiceToTest.login("username", null));
    }

    @Test
    void testLogoutSuccess() throws DataAccessException {
        String authToken = "validAuthToken";
        when(mockAuthDao.verifyAuth(authToken)).thenReturn(true);
        assertDoesNotThrow(() -> userServiceToTest.logout(authToken));
        verify(mockAuthDao).deleteAuth(authToken);
    }

    @Test
    void shouldThrowExceptionWhenLogoutWithInvalidToken() throws DataAccessException {
        String authToken = "invalidAuthToken";
        when(mockAuthDao.verifyAuth(authToken)).thenReturn(false);
        assertThrows(DataAccessException.class, () -> userServiceToTest.logout(authToken));
    }
}
