package server;

import handler.*;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", (req, res) -> new RegisterHandler().register(req, res));
        Spark.post("/session", (req, res) -> new LoginHandler().login(req, res));
        Spark.delete("/session", (req, res) -> new LogoutHandler().logout(req, res));

        Spark.get("/game", (req, res) -> new GameHandler().listGames(req, res));
        Spark.get("/game/:gameID", (req, res) -> new GameHandler().getGame(req, res));
        Spark.post("/game", (req, res) -> new GameHandler().createGame(req, res));
        Spark.put("/game", (req, res) -> new GameHandler().joinGame(req, res));
        Spark.put("/game/:gameID", (req, res) -> new GameHandler().observeGame(req, res));
        Spark.put("/game/move", (req, res) -> new GameHandler().makeMove(req, res));

        Spark.delete("/db", (req, res) -> new ClearHandler().clear(req, res));

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
