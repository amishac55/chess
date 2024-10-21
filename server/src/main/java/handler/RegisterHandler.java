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
    private static final Gson gson = new Gson();

    public Object register(Request request, Response response) {
        RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);
        UserData newUserData = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());

        try {
            AuthData newUserAuth = new UserService().register(newUserData);
            response.status(200);
            return gson.toJson(new RegisterResponse(newUserAuth.username(), newUserAuth.authToken(), "User registered successfully"));
        } catch (DataAccessException e) {
            response.status(e.getStatusCode());
            return gson.toJson(new RegisterResponse(null, null, e.getMessage()));
        }
    }
}