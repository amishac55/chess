package model;

import java.util.List;

public record GameRecord(int gameID, String whiteUsername, String blackUsername, String gameName,
                         List<String> observers) {
}
