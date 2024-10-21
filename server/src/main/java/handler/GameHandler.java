package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import responses.CommonResponse;
import responses.CreateGameResponse;
import responses.ListGamesResponse;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.ArrayList;

public class GameHandler {
    private static final Gson gson = new Gson();

    public Object createGame(Request request, Response response) {
        String authToken = request.headers("authorization");
        CreateGameRequest createGameRequest = gson.fromJson(request.body(), CreateGameRequest.class);
        try {
            Integer newGameID = new GameService().createGame(createGameRequest.gameName(), authToken);
            response.status(200);
            return gson.toJson(new CreateGameResponse(newGameID, "Game created"));
        } catch (DataAccessException e) {
            response.status(e.getStatusCode());
            return gson.toJson(new CreateGameResponse(null, "Error: " + e.getMessage()));
        }
    }

    public Object listGames(Request request, Response response) {
        String authToken = request.headers("authorization");
        try {
            ArrayList<ListGamesResponse.GameRecord> games = new GameService().listGames(authToken);
            response.status(200);
            return gson.toJson(new ListGamesResponse(games, null));
        } catch (DataAccessException e) {
            response.status(e.getStatusCode());
            return gson.toJson(new ListGamesResponse(null, "Error: " + e.getMessage()));
        }
    }

    public Object joinGame(Request request, Response response) {
        String authToken = request.headers("authorization");
        JoinGameRequest joinGameRequest = gson.fromJson(request.body(), JoinGameRequest.class);
        try {
            new GameService().joinGame(joinGameRequest.gameID(), joinGameRequest.playerColor(), authToken);
            response.status(200);
            return gson.toJson(new CommonResponse(null));
        } catch (DataAccessException e) {
            response.status(e.getStatusCode());
            return gson.toJson(new CommonResponse(e.getMessage()));
        }
    }
}
