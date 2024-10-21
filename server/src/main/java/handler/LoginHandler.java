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
    private static final Gson gson = new Gson();

    public Object login(Request request, Response response) {
        LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
        try {
            AuthData userAuthData = new UserService().login(loginRequest.username(), loginRequest.password());
            response.status(200);
            return gson.toJson(new LoginResponse(userAuthData.username(), userAuthData.authToken(), "Login successful"));
        } catch (DataAccessException e) {
            response.status(e.getStatusCode());
            return gson.toJson(new LoginResponse(null, null, e.getMessage()));
        }
    }
}
