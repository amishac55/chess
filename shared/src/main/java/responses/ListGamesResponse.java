package responses;

import java.util.ArrayList;

public record ListGamesResponse(ArrayList<GameRecord> games, String message) {

    public record GameRecord(int gameID, String whiteUsername, String blackUsername, String gameName) {}
}
