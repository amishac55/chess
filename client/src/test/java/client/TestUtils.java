package client;

import exception.ResponseException;
import requests.CreateGameRequest;
import requests.RegisterRequest;
import responses.CreateGameResponse;

public class TestUtils {

    private static final ServerFacade SERVER_FACADE = ServerFacade.getInstance();

    public static void registerTestUser() throws ResponseException {
        RegisterRequest request = new RegisterRequest("testUser", "password", "test@email.com");
        SERVER_FACADE.register(request);
    }

    public static Integer createTestGame() throws ResponseException {
        CreateGameRequest request = new CreateGameRequest("Test Game");
        CreateGameResponse response = SERVER_FACADE.createGame(request);
        return response.gameID();
    }
}