package responses;

import model.GameRecord;

import java.util.ArrayList;

public record ListGamesResponse(ArrayList<GameRecord> games, String message) {

}
