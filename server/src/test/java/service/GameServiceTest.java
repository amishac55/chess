package service;

import dataaccess.DAOFactory;
import dataaccess.DataAccessException;
import dataaccess.IDAO.AuthDAO;
import dataaccess.IDAO.GameDAO;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import responses.ListGamesResponse;
import utils.PlayerColor;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServiceTest {

    private GameService gameServiceToTest;
    private GameDAO mockGameDao;
    private AuthDAO mockAuthDao;

    @BeforeEach
    void setUp() {
        DAOFactory mockDAOFactory = mock(DAOFactory.class);
        mockGameDao = mock(GameDAO.class);
        mockAuthDao = mock(AuthDAO.class);
        // Configure mock DAOFactory to return mock DAOs
        when(mockDAOFactory.getGameDAO()).thenReturn(mockGameDao);
        when(mockDAOFactory.getAuthDAO()).thenReturn(mockAuthDao);

        DAOFactory.setInstance(mockDAOFactory);
        gameServiceToTest = new GameService();
    }

    @Test
    void shouldCreateGameWhenAuthTokenIsValid() throws DataAccessException {
        String validToken = "validToken";
        String gameName = "TestGame";
        when(mockAuthDao.verifyAuth(validToken)).thenReturn(true);
        when(mockGameDao.createGame(gameName)).thenReturn(1);

        Integer gameId = gameServiceToTest.createGame("TestGame", "validToken");

        assertEquals(1, gameId, "Game ID should be 1");
        verify(mockGameDao).createGame(gameName);
        verify(mockAuthDao).verifyAuth(validToken);
    }

    @Test
    void shouldThrowExceptionWhenCreatingGameWithInvalidAuth() throws DataAccessException {
        String invalidToken = "invalidToken";
        String gameName = "TestGame";
        when(mockAuthDao.verifyAuth(invalidToken)).thenReturn(false);

        assertThrows(DataAccessException.class, () -> gameServiceToTest.createGame("TestGame", "invalidToken"));
        verify(mockGameDao, never()).createGame(anyString());
    }

    @Test
    void shouldJoinGameWhenAuthTokenIsValid() throws DataAccessException {
        when(mockAuthDao.verifyAuth("validToken")).thenReturn(true);
        when(mockAuthDao.getAuth("validToken")).thenReturn(new AuthData("validToken", "username"));

        assertDoesNotThrow(() -> gameServiceToTest.joinGame(1, PlayerColor.WHITE, "validToken"));
        verify(mockGameDao).addPlayer(1, "username", PlayerColor.WHITE);
    }

    @Test
    void shouldNotJoinGameWhenAuthTokenIsInvalid() throws DataAccessException {
        when(mockAuthDao.verifyAuth("invalidToken")).thenReturn(false);

        assertThrows(DataAccessException.class, () -> gameServiceToTest.joinGame(1, PlayerColor.WHITE, "invalidToken"));
        verify(mockGameDao, never()).addPlayer(anyInt(), anyString(), any(PlayerColor.class));
    }

    @Test
    void shouldThrowExceptionWhenGameDoesNotExist() throws DataAccessException {
        String validToken = "validToken";
        String username = "username";
        when(mockAuthDao.verifyAuth(validToken)).thenReturn(true);
        when(mockAuthDao.getAuth(validToken)).thenReturn(new AuthData(username, validToken));
        doThrow(new DataAccessException(404, "Game not found")).when(mockGameDao).addPlayer(anyInt(), anyString(), any(PlayerColor.class));

        DataAccessException exception = assertThrows(DataAccessException.class, () -> gameServiceToTest.joinGame(999, PlayerColor.WHITE, "validToken"));
        assertEquals(404, exception.getStatusCode(), "Status code should be 404");
        assertEquals("Game not found", exception.getMessage(), "Exception message should be 'Game not found'");
    }

    @Test
    void shouldListGamesWhenAuthTokenIsValid() throws DataAccessException {
        when(mockAuthDao.verifyAuth("validToken")).thenReturn(true);
        ArrayList<GameData> mockGames = new ArrayList<>(Arrays.asList(
                new GameData(1, "White1", "Black1", "Game1", null),
                new GameData(2, "White2", "Black2", "Game2", null)
        ));
        when(mockGameDao.listGames()).thenReturn(mockGames);

        ArrayList<ListGamesResponse.GameRecord> resultGameRecords = gameServiceToTest.listGames("validToken");

        assertEquals(2, resultGameRecords.size(), "Game record size should be 2");
        assertEquals(1, resultGameRecords.getFirst().gameID());
        assertEquals("White1", resultGameRecords.getFirst().whiteUsername());
        assertEquals("Black1", resultGameRecords.getFirst().blackUsername());
        assertEquals("Game1", resultGameRecords.getFirst().gameName());
        verify(mockAuthDao).verifyAuth("validToken");
        verify(mockGameDao).listGames();
    }

    @Test
    void shouldThrowExceptionWhenListingGamesWithInvalidAuthToken() throws DataAccessException {
        when(mockAuthDao.verifyAuth("invalidToken")).thenReturn(false);

        assertThrows(DataAccessException.class, () -> gameServiceToTest.listGames("invalidToken"));
        verify(mockGameDao, never()).listGames();
    }
}