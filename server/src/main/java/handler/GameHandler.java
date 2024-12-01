package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import model.GameRecord;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.MakeMoveRequest;
import responses.CommonResponse;
import responses.CreateGameResponse;
import responses.GetGameResponse;
import responses.ListGamesResponse;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.ArrayList;

public class GameHandler {
    private static final Gson GSON = new Gson();

    public Object createGame(Request request, Response response) {
        String authToken = request.headers("authorization");
        CreateGameRequest createGameRequest = GSON.fromJson(request.body(), CreateGameRequest.class);
        try {
            Integer newGameID = new GameService().createGame(createGameRequest.gameName(), authToken);
            response.status(200);
            return GSON.toJson(new CreateGameResponse(newGameID, "Game created"));
        } catch (DataAccessException e) {
            response.status(e.getStatusCode());
            response.body(e.getMessage());
            return GSON.toJson(new CreateGameResponse(null, "Error: " + e.getMessage()));
        }
    }

    public Object listGames(Request request, Response response) {
        String authToken = request.headers("authorization");
        try {
            ArrayList<GameRecord> games = new GameService().listGames(authToken);
            response.status(200);
            return GSON.toJson(new ListGamesResponse(games, null));
        } catch (DataAccessException e) {
            response.status(e.getStatusCode());
            response.body(e.getMessage());
            return GSON.toJson(new ListGamesResponse(null, "Error: " + e.getMessage()));
        }
    }

    public Object getGame(Request request, Response response) {
        String authToken = request.headers("authorization");
        int gameID = Integer.parseInt(request.params("gameID"));

        try {
            GameData gameData = new GameService().getGame(gameID, authToken);
            GetGameResponse getGameResponse = new GetGameResponse(gameData, null);
            response.status(200);
            return GSON.toJson(getGameResponse);
        } catch (DataAccessException e) {
            setErrorResponse(response, e);
            return GSON.toJson(new GetGameResponse(null, e.getMessage()));
        }
    }

    public Object joinGame(Request request, Response response) {
        String authToken = request.headers("authorization");
        JoinGameRequest joinGameRequest = GSON.fromJson(request.body(), JoinGameRequest.class);
        try {
            new GameService().joinGame(joinGameRequest.gameID(), joinGameRequest.playerColor(), authToken);
            response.status(200);
            return GSON.toJson(new CommonResponse(null));
        } catch (DataAccessException e) {
            response.status(e.getStatusCode());
            response.body(e.getMessage());
            return GSON.toJson(new CommonResponse(e.getMessage()));
        }
    }

    public Object observeGame(Request request, Response response) {
        String authToken = request.headers("authorization");
        int gameID = Integer.parseInt(request.params("gameID"));

        try {
            new GameService().observeGame(gameID, authToken);
            response.status(200);
            return GSON.toJson(new CommonResponse(null));
        } catch (DataAccessException e) {
            setErrorResponse(response, e);
            return GSON.toJson(new CommonResponse(e.getMessage()));
        }
    }

    public Object makeMove(Request request, Response response) {
        String authToken = request.headers("authorization");
        MakeMoveRequest makeMoveRequest = GSON.fromJson(request.body(), MakeMoveRequest.class);
        GameService gameService = null;
        try {
            gameService = new GameService();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            gameService.makeMove(authToken, makeMoveRequest.gameData());
            response.status(200);
            return GSON.toJson(new CommonResponse(null));
        } catch (DataAccessException e) {
            setErrorResponse(response, e);
            return GSON.toJson(new CommonResponse(e.getMessage()));
        }
    }

    private void setErrorResponse(Response response, DataAccessException e) {
        response.status(e.getStatusCode());
        response.body(e.getMessage());
    }

}
