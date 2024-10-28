package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import requests.LoginRequest;
import responses.LoginResponse;
import service.UserService;
import spark.Request;
import spark.Response;

public class LoginHandler {
    private static final Gson GSON = new Gson();

    public Object login(Request request, Response response) {
        LoginRequest loginRequest = GSON.fromJson(request.body(), LoginRequest.class);
        try {
            AuthData userAuthData = new UserService().login(loginRequest.username(), loginRequest.password());
            response.status(200);
            return GSON.toJson(new LoginResponse(userAuthData.username(), userAuthData.authToken(), "Login successful"));
        } catch (DataAccessException e) {
            response.status(e.getStatusCode());
            response.body(e.getMessage());
            return GSON.toJson(new LoginResponse(null, null, e.getMessage()));
        }
    }
}
