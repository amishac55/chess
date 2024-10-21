package service;

import dataaccess.DAOFactory;
import dataaccess.DataAccessException;
import dataaccess.IDAO.AuthDAO;
import dataaccess.IDAO.GameDAO;
import dataaccess.IDAO.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class ClearServiceTest {

    @Mock
    private AuthDAO authDAO;
    @Mock
    private GameDAO gameDAO;
    @Mock
    private UserDAO userDAO;
    @Mock
    private DAOFactory daoFactory;

    private ClearService clearService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(daoFactory.getAuthDAO()).thenReturn(authDAO);
        when(daoFactory.getGameDAO()).thenReturn(gameDAO);
        when(daoFactory.getUserDAO()).thenReturn(userDAO);
        DAOFactory.setInstance(daoFactory);
        clearService = new ClearService();
    }

    @Test
    void testClearAllDataSuccess() throws DataAccessException {
        assertDoesNotThrow(() -> clearService.clearAllData());
        verify(authDAO, times(1)).clearAuthData();
        verify(gameDAO, times(1)).clearGameData();
        verify(userDAO, times(1)).clearUserData();
    }
}
