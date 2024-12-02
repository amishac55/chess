package ui;

import chess.ChessGame;
import client.ServerFacade;
import exception.ResponseException;
import model.GameRecord;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import responses.GetGameResponse;
import utils.PlayerColor;

import java.util.*;

import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class PostLogin {

    ServerFacade serverFacade;
    List<GameRecord> games;
    ChessDisplay chessDisplay;
    Boolean phase5Continue = false;

    boolean inGame;

    public PostLogin(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
        games = new ArrayList<>();
    }

    public void run() {
        boolean loggedIn = true;
        inGame = false;
        out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
        while ((loggedIn && !inGame) || (loggedIn && phase5Continue)) {
            String[] input = getUserInput();
            switch (input[0]) {
                case "quit":
                    try {
                        serverFacade.logout();
                    } catch (ResponseException e) {
                        out.println("Error quitting: " + e.getMessage());
                    }
                    return;
                case "help":
                    printHelpMenu();
                    break;
                case "logout":
                    try {
                        serverFacade.logout();
                    } catch (ResponseException e) {
                        out.println("Error logging out: " + e.getMessage());
                    }
                    loggedIn = false;
                    break;
                case "list":
                    refreshGames();
                    printGames();
                    break;
                case "create":
                    if (input.length != 2) {
                        out.println("Please provide a name");
                        printCreate();
                        break;
                    }
                    try {
                        serverFacade.createGame(new CreateGameRequest(input[1]));
                        out.printf("Created game: %s%n", input[1]);
                        break;
                    } catch (ResponseException e) {
                        out.println("Error creating game: " + e.getMessage());
                    }
                case "join":
                    handleJoin(input);
                    phase5Continue = true;
                    break;
                case "observe":
                    handleObserve(input);
                    phase5Continue = true;
                    break;
                default:
                    out.println("Command not recognized, please try again");
                    printHelpMenu();
                    break;
            }
        }
        if (!loggedIn) {
            PreLogin prelogin = new PreLogin(serverFacade);
            prelogin.run();
        }
    }

    private String[] getUserInput() {
        out.print("\n[LOGGED IN] >>> ");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().split(" ");
    }

    private void refreshGames() {
        games = new ArrayList<>();
        try {
            games.addAll(serverFacade.listGames().games());
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    private void printGames() {
        for (int i = 0; i < games.size(); i++) {
            GameRecord game = games.get(i);
            String whiteUser = game.whiteUsername() != null ? game.whiteUsername() : "open";
            String blackUser = game.blackUsername() != null ? game.blackUsername() : "open";
            out.printf("%d -- Game Name: %s  |  White User: %s  |  Black User: %s %n", i, game.gameName(), whiteUser, blackUser);
        }
    }

    private void printHelpMenu() {
        printCreate();
        out.println("list - list all games");
        printJoin();
        printObserve();
        out.println("logout - log out of current user");
        out.println("quit - stop playing");
        out.println("help - show this menu");
    }

    private void handleJoin(String[] input) {
        if (input.length != 3 || !input[1].matches("\\d") || !input[2].toUpperCase().matches("WHITE|BLACK")) {
            out.println("Please provide a game ID and color choice");
            printJoin();
            return;
        }
        GameRecord joinGame = getGameRecord(input);
        if (joinGame == null) {
            return;
        }
        PlayerColor color = input[2].equalsIgnoreCase("WHITE") ? PlayerColor.WHITE : PlayerColor.BLACK;
        JoinGameRequest joinGameRequest = new JoinGameRequest(color, joinGame.gameID());
        try {
            if (serverFacade.joinGame(joinGameRequest)) {
                out.println("You have joined the game");
                inGame = true;
//                implement joinGame and then player gameplay
                getDisplay(joinGame);
            } else {
                out.println("Game does not exist or color taken");
                printJoin();
            }
        } catch (ResponseException e) {
            out.println("Error joining game: " + e.getMessage());
        }
    }

    private void handleObserve(String[] input) {
        if (input.length != 2 || !input[1].matches("\\d")) {
            out.println("Please provide a game ID");
            printObserve();
            return;
        }
        GameRecord observeGame = getGameRecord(input);
        if (observeGame == null){
            return;
        }
        try {
            if (serverFacade.observeGame(observeGame.gameID())) {
                out.println("You have joined the game as an observer");
                inGame = true;
                //          implement observeGame and then observe gameplay
                getDisplay(observeGame);
            } else {
                out.println("Game does not exist");
                printObserve();
            }
        } catch (ResponseException e) {
            out.println("Error observing game: " + e.getMessage());
        }
    }

    private void getDisplay(GameRecord gameRecord) throws ResponseException {
        GetGameResponse getGameResponse = serverFacade.getGame(gameRecord.gameID());
        chessDisplay = new ChessDisplay(getGameResponse.gameData().getChessGame());
        chessDisplay.displayBoard(ChessGame.TeamColor.WHITE, null);
        chessDisplay.displayBoard(ChessGame.TeamColor.BLACK, null);
    }

    private GameRecord getGameRecord(String[] input) {
        int gameID = Integer.parseInt(input[1]);
        if (games.isEmpty() || games.size() <= gameID) {
            refreshGames();
            if (games.isEmpty()) {
                out.println("Error: please first create a game");
                return null;
            }
            if (games.size() <= gameID) {
                out.println("Error: that Game ID does not exist");
                printGames();
                return null;
            }
        }
        return games.get(gameID);
    }

    private void printCreate() {
        out.println("create <NAME> - create a new game");
    }

    private void printJoin() {
        out.println("join <ID> [WHITE|BLACK] - join a game as color");
    }

    private void printObserve() {
        out.println("observe <ID> - observe a game");
    }
}
