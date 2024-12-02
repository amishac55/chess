package client;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;
import requests.*;
import responses.CreateGameResponse;
import responses.GetGameResponse;
import responses.ListGamesResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class ServerFacade {
    private static final String APPLICATION_JSON = "application/json";
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static ServerFacade instance;
    private final String baseUrl;
    private final Gson gson;
    private String authToken;

    private ServerFacade(String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        this.gson = new Gson();
    }

    public static synchronized ServerFacade getInstance(String baseUrl) {
        if (instance == null) {
            instance = new ServerFacade(baseUrl);
        }
        return instance;
    }

    public static synchronized ServerFacade getInstance() {
        return getInstance(null);
    }

    public String getAuthToken() {
        return authToken;
    }

    public CreateGameResponse createGame(CreateGameRequest request) throws ResponseException {
        return executeRequest("/game", HttpMethod.POST, request, CreateGameResponse.class);
    }

    public Boolean joinGame(JoinGameRequest request) throws ResponseException {
        try {
            executeRequest("/game", HttpMethod.PUT, request, null);
            return true;
        } catch (ResponseException e) {
            return false;
        }
    }

    public Boolean observeGame(Integer gameID) throws ResponseException {
        try {
            executeRequest("/game/" + gameID, HttpMethod.PUT, null, null);
            return true;
        } catch (ResponseException e) {
            return false;
        }
    }

    public ListGamesResponse listGames() throws ResponseException {
        return executeRequest("/game", HttpMethod.GET, null, ListGamesResponse.class);
    }

    public GetGameResponse getGame(Integer gameID) throws ResponseException {
        return executeRequest("/game/" + gameID, HttpMethod.GET, null, GetGameResponse.class);
    }


    public void login(LoginRequest request) throws ResponseException {
        AuthData authData = executeRequest("/session", HttpMethod.POST, request, AuthData.class);
        authToken = authData.authToken();
    }

    public void logout() throws ResponseException {
        executeRequest("/session", HttpMethod.DELETE, null, null);
        authToken = null;
    }

    public void register(RegisterRequest request) throws ResponseException {
        AuthData authData = executeRequest("/user", HttpMethod.POST, request, AuthData.class);
        authToken = authData.authToken();
    }

    public <T> T executeRequest(String endpoint, HttpMethod method, Object request, Class<T> responseClass) throws ResponseException {
        try {
            HttpURLConnection connection = createConnection(endpoint, method);
            sendRequest(connection, request);
            return processResponse(connection, responseClass);
        } catch (IOException e) {
            throw new ResponseException("Failed to execute request: " + e.getMessage(), 500);
        }
    }

    private HttpURLConnection createConnection(String endpoint, HttpMethod method) throws IOException {
        URI uri = URI.create(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

        connection.setRequestMethod(method.name());
        connection.setDoOutput(method.hasBody());
        connection.setRequestProperty("Accept", APPLICATION_JSON);

        if (authToken != null) {
            connection.setRequestProperty("Authorization", authToken);
        }

        return connection;
    }

    private void sendRequest(HttpURLConnection connection, Object request) throws IOException {
        if (request == null) {
            return;
        }

        try (OutputStream outputStream = connection.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.write(gson.toJson(request));
            writer.flush();
        }
    }

    private <T> T processResponse(HttpURLConnection connection, Class<T> responseClass) throws IOException, ResponseException {
        validateResponse(connection);

        if (responseClass == null) {
            return null;
        }

        try (InputStream inputStream = connection.getInputStream();
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, responseClass);
        }
    }

    private void validateResponse(HttpURLConnection connection) throws IOException, ResponseException {
        int responseCode = connection.getResponseCode();
        if (responseCode / 100 != 2) {
            throw new ResponseException("Server returned error: " +
                    " - " + connection.getResponseMessage(), connection.getResponseCode());
        }
    }

    public enum HttpMethod {
        GET(false), POST(true), PUT(true), DELETE(false);

        private final boolean hasBody;

        HttpMethod(boolean hasBody) {
            this.hasBody = hasBody;
        }

        public boolean hasBody() {
            return hasBody;
        }
    }
}
