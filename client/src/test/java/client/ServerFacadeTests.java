package client;

import exception.ResponseException;
import org.junit.jupiter.api.*;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.CreateGameResponse;
import responses.GetGameResponse;
import responses.ListGamesResponse;
import server.Server;
import utils.PlayerColor;
import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        serverFacade = ServerFacade.getInstance("http://localhost:"+port);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    public void setup() {
        // Clear database before each test
        try {
            serverFacade.executeRequest("/db", ServerFacade.HttpMethod.DELETE, null, null);
        } catch (ResponseException e) {
            fail("Failed to clear database: " + e.getMessage());
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void createGamePositive() throws ResponseException {
        registerTestUser();
        CreateGameRequest request = new CreateGameRequest("Test Game");
        CreateGameResponse response = serverFacade.createGame(request);

        assertNotNull(response);
        assertNotNull(response.gameID());
        assertTrue(response.gameID() > 0);
    }
    @Test
    public void createGameNegative() {
        // Setup - don't login
        CreateGameRequest request = new CreateGameRequest("Test Game");

        // Execute & Verify
        assertThrows(ResponseException.class, () -> serverFacade.createGame(request));
    }
    @Test
    public void joinGamePositive() throws ResponseException {
        registerTestUser();
        Integer gameId = createTestGame();
        JoinGameRequest request = new JoinGameRequest(PlayerColor.WHITE, gameId);

        Boolean result = serverFacade.joinGame(request);
        assertTrue(result);

    }

    @Test
    public void joinGameNegative() throws ResponseException {
        // Setup
        registerTestUser();
        JoinGameRequest request = new JoinGameRequest(PlayerColor.WHITE, 999);

        // Execute
        Boolean result = serverFacade.joinGame(request);

        // Verify
        assertFalse(result);
    }

    @Test
    public void observeGamePositive() throws ResponseException {
        registerTestUser();
        Integer gameId = createTestGame();

        Boolean result = serverFacade.observeGame(gameId);
        assertTrue(result);
    }

    @Test
    public void observeGameNegative() throws ResponseException {
        // Setup
        registerTestUser();

        // Execute
        Boolean result = serverFacade.observeGame(999);

        // Verify
        assertFalse(result);
    }

    @Test
    public void listGamesPositive() throws ResponseException {
        registerTestUser();
        createTestGame();
        createTestGame();
        ListGamesResponse response = serverFacade.listGames();

        assertNotNull(response);
        assertEquals(2, response.games().size());
    }

    @Test
    public void listGamesNegative() throws ResponseException {

        registerTestUser();
        createTestGame();
        createTestGame();
        ListGamesResponse response = serverFacade.listGames();

        assertNotNull(response);
        assertNotEquals(3, response.games().size()); // Assuming the database is not cleared between tests
    }

    @Test
    public void getGamePositive() throws ResponseException {
        registerTestUser();
        Integer gameId = createTestGame();
        GetGameResponse response = serverFacade.getGame(gameId);

        assertNotNull(response);
        assertNotNull(response.gameData());
        assertEquals(gameId, response.gameData().getGameID());
    }

    @Test
    public void getGameNegative() throws ResponseException {
        // Setup
        registerTestUser();

        // Execute & Verify
        assertThrows(ResponseException.class, () -> serverFacade.getGame(999));
    }

    @Test
    public void loginPositive() throws ResponseException {
        // Setup
        RegisterRequest registerRequest = new RegisterRequest("testUser", "password", "test@email.com");
        serverFacade.register(registerRequest);
        serverFacade.logout(); // Clear auth token
        LoginRequest loginRequest = new LoginRequest("testUser", "password");

        // Execute
        serverFacade.login(loginRequest);

        // Verify
        assertNotNull(serverFacade.getAuthToken());
    }

    @Test
    public void loginNegative() {
        // Setup
        LoginRequest request = new LoginRequest("wrongUser", "wrongPass");

        // Execute & Verify
        assertThrows(ResponseException.class, () -> serverFacade.login(request));
    }

    @Test
    public void registerPositive() throws ResponseException {
        // Setup
        RegisterRequest request = new RegisterRequest("newUser", "password", "new@email.com");

        // Execute
        serverFacade.register(request);

        // Verify
        assertNotNull(serverFacade.getAuthToken());
    }

    @Test
    public void registerNegative() throws ResponseException {
        // Setup
        RegisterRequest request = new RegisterRequest("testUser", "password", "test@email.com");
        serverFacade.register(request);
        serverFacade.logout();

        // Execute & Verify
        assertThrows(ResponseException.class, () -> serverFacade.register(request));
    }

    @Test
    public void logoutPositive() throws ResponseException {
        // Setup
        registerTestUser();
        assertNotNull(serverFacade.getAuthToken());

        // Execute
        serverFacade.logout();

        // Verify
        assertNull(serverFacade.getAuthToken());
    }

    @Test
    public void logoutNegative() {
        // Setup - don't login

        // Execute & Verify
        assertThrows(ResponseException.class, () -> serverFacade.logout());
    }

    public static void registerTestUser() throws ResponseException {
        RegisterRequest request = new RegisterRequest("testUser", "password", "test@email.com");
        serverFacade.register(request);
    }

    public static Integer createTestGame() throws ResponseException {
        CreateGameRequest request = new CreateGameRequest("Test Game");
        CreateGameResponse response = serverFacade.createGame(request);
        return response.gameID();
    }

}
