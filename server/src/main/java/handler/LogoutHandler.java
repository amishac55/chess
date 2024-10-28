package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import responses.CommonResponse;
import service.UserService;
import spark.Request;
import spark.Response;

public class LogoutHandler {
    private static final Gson GSON = new Gson();

    public Object logout(Request request, Response response) {
        String authToken = request.headers("authorization");
        try {
            new UserService().logout(authToken);
            response.status(200);
            return GSON.toJson(new CommonResponse("Logged out successfully"));
        } catch (DataAccessException e) {
            response.status(e.getStatusCode());
            response.body(e.getMessage());
            return GSON.toJson(new CommonResponse(e.getMessage()));
        }
    }
}
