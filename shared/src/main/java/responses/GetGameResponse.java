package responses;

import model.GameData;

public record GetGameResponse (GameData gameData, String message) {
}
