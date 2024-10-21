package requests;

import utils.PlayerColor;

public record JoinGameRequest (PlayerColor playerColor, Integer gameID) {
}
