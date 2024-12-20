package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import responses.CommonResponse;
import service.ClearService;
import spark.Request;
import spark.Response;

public class ClearHandler {
    private static final Gson GSON = new Gson();

    public Object clear(Request request, Response response) {
        try {
            new ClearService().clearAllData();
            response.status(200);
            CommonResponse clearResponse = new CommonResponse("Data cleared successfully");
            return GSON.toJson(clearResponse);
        } catch (DataAccessException e) {
            response.status(500);
            return GSON.toJson(new CommonResponse(e.getMessage()));
        }
    }
}
