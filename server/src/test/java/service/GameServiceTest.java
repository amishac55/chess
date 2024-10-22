package service;

import dataaccess.DAOFactory;
import dataaccess.DataAccessException;
import dataaccess.idao.AuthDAO;
import dataaccess.idao.GameDAO;
import dataaccess.idao.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import responses.ListGamesResponse;
import utils.PlayerColor;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameServiceToTest;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private ClearService clearService;
    private AuthData testAuthData;

    @BeforeEach
    void setUp() throws DataAccessException {
        DAOFactory factory = DAOFactory.getInstance();
        gameDAO = factory.getGameDAO();
        authDAO = factory.getAuthDAO();
        userDAO = factory.getUserDAO();
        clearService = new ClearService();
        gameServiceToTest = new GameService();

        clearService.clearAllData();

        userDAO.createUser(new UserData("user", "pass", "user@test.com"));
        testAuthData = authDAO.createAuth("user");
    }

    @AfterEach
    void tearDown() throws DataAccessException {
        clearService.clearAllData();
    }

    @Test
    void shouldCreateGameWhenAuthTokenIsValid() throws DataAccessException {
        String validToken = testAuthData.authToken();
        String gameName = "valorant";

        Integer gameId = gameServiceToTest.createGame(gameName, validToken);

        assertNotNull(gameId);
        assertNotNull(gameDAO.getGame(gameId));
        assertEquals(gameName, gameDAO.getGame(gameId).getGameName());
    }

    @Test
    void shouldThrowExceptionWhenCreatingGameWithInvalidAuth() {
        String invalidToken = "whoopsie";
        String gameName = "valorant";

        assertThrows(DataAccessException.class, () -> gameServiceToTest.createGame(gameName, invalidToken));
    }

    @Test
    void shouldJoinGameWhenAuthTokenIsValid() throws DataAccessException {
        String validToken = testAuthData.authToken();
        Integer gameId = gameServiceToTest.createGame("valorant", validToken);

        assertDoesNotThrow(() -> gameServiceToTest.joinGame(gameId, PlayerColor.WHITE, validToken));

        GameData game = gameDAO.getGame(gameId);
        assertEquals("user", game.getWhiteUsername());
    }

    @Test
    void shouldNotJoinGameWhenAuthTokenIsInvalid() {
        assertThrows(DataAccessException.class, () -> gameServiceToTest.joinGame(1, PlayerColor.WHITE, "whoopsie"));
    }

    @Test
    void shouldThrowExceptionWhenGameDoesNotExist() throws DataAccessException {
        String validToken = testAuthData.authToken();

        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> gameServiceToTest.joinGame(999, PlayerColor.WHITE, validToken));
        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    void shouldListGamesWhenAuthTokenIsValid() throws DataAccessException {
        String validToken = testAuthData.authToken();
        gameServiceToTest.createGame("pubg", validToken);
        gameServiceToTest.createGame("barbieDreamHouse", validToken);

        ArrayList<ListGamesResponse.GameRecord> resultGameRecords = gameServiceToTest.listGames(validToken);

        assertEquals(2, resultGameRecords.size(), "Game record size should be 2");
        assertEquals("pubg", resultGameRecords.get(0).gameName());
        assertEquals("barbieDreamHouse", resultGameRecords.get(1).gameName());
    }

    @Test
    void shouldThrowExceptionWhenListingGamesWithInvalidAuthToken() {
        assertThrows(DataAccessException.class, () -> gameServiceToTest.listGames("whoopsie"));
    }
}