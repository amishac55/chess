package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import requests.RegisterRequest;
import responses.RegisterResponse;
import service.UserService;
import spark.Request;
import spark.Response;

public class RegisterHandler {
    private static final Gson GSON = new Gson();

    public Object register(Request request, Response response) {
        RegisterRequest registerRequest = GSON.fromJson(request.body(), RegisterRequest.class);
        UserData newUserData = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());

        try {
            AuthData newUserAuth = new UserService().register(newUserData);
            response.status(200);
            return GSON.toJson(new RegisterResponse(newUserAuth.username(), newUserAuth.authToken(), "User registered successfully"));
        } catch (DataAccessException e) {
            response.status(e.getStatusCode());
            response.body(e.getMessage());
            return GSON.toJson(new RegisterResponse(null, null, e.getMessage()));
        }
    }
}