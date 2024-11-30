package dataaccess.sqldao;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.idao.GameDAO;
import model.GameData;
import model.GameRecord;
import utils.PlayerColor;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SQLGameDAO extends SQLBaseClass implements GameDAO {

    public SQLGameDAO() throws DataAccessException {
        String[] createStatements = {
                """
            CREATE TABLE IF NOT EXISTS gameTable (
              `gameID` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256),
              `blackUsername` varchar(256),
              `gameName` varchar(256) NOT NULL,
              `game` TEXT NOT NULL,
              `observers` TEXT,
              PRIMARY KEY (`gameID`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
        };
        configureDatabase(createStatements);
    }

    @Override
    public Integer addGame(GameData gameData) throws DataAccessException {
        var statement = "INSERT INTO gameTable (whiteUsername, blackUsername, gameName, game, observers) VALUES (?, ?, ?, ?, ?)";
        var json = new Gson().toJson(gameData.getChessGame());
        var observersJson = new Gson().toJson(new ArrayList<>()); // Default value for observers
        return executeInsert(statement, gameData.getWhiteUsername(), gameData.getBlackUsername(), gameData.getGameName(), json, observersJson);
    }

    @Override
    public GameData getGame(Integer gameID) throws DataAccessException {
        if (gameID != null) {
            try (var conn = getConnection()) {
                var statement = prepareStatement(conn, "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM gameTable WHERE gameID=?");
                statement.setInt(1, gameID);
                try (var rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return readGameWithChessGame(rs);
                    }
                }
            } catch (Exception e) {
                throw new DataAccessException(400, String.format("Unable to read data: %s", e.getMessage()));
            }
        }
        return null;
    }

    private GameData readGameWithChessGame(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        var gameJson = rs.getString("game");

        ChessGame chessGame = new Gson().fromJson(gameJson, ChessGame.class);

        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
    }

    private Connection getConnection() throws SQLException, DataAccessException {
        return DatabaseManager.getConnection();
    }

    private PreparedStatement prepareStatement(Connection conn, String statement) throws SQLException {
        return conn.prepareStatement(statement);
    }

    private GameRecord readGamesForListing(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        var observersJson = rs.getString("observers");

        List<String> observers = new ArrayList<>();
        if (observersJson != null && !observersJson.isEmpty()) {
            observers = new Gson().fromJson(observersJson, new TypeToken<List<String>>(){}.getType());
        }

        return new GameRecord(gameID, whiteUsername, blackUsername, gameName, observers);
    }

    @Override
    public Collection<GameRecord> listGames() throws DataAccessException {
        var result = new ArrayList<GameRecord>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, observers FROM gameTable";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGamesForListing(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return result;
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        var statement = "UPDATE gameTable SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        try {
            String gameJson = new Gson().toJson(gameData.getChessGame());
            executeUpdate(statement,
                    gameData.getWhiteUsername(),
                    gameData.getBlackUsername(),
                    gameData.getGameName(),
                    gameJson);
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to update game: %s", e.getMessage()));
        }
    }

    @Override
    public void deleteGame(Integer gameID) throws DataAccessException {
        try {
            var statement = "DELETE FROM gameTable WHERE gameID=?";
            executeUpdate(statement, gameID);
        } catch (Exception e) {
            throw new DataAccessException(400, "Error: Game does not exist " + e.getMessage());
        }
    }

    @Override
    public void addPlayer(Integer gameID, String username, PlayerColor requestedPlayerColor) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            GameData game = getGame(gameID);
            if (game == null) {
                throw new DataAccessException(400, "Error: bad request");
            }

            String updateStatement;
            if (requestedPlayerColor == PlayerColor.WHITE) {
                if (game.getWhiteUsername() != null) {
                    throw new DataAccessException(403, "Error: already taken");
                }
                updateStatement = "UPDATE gameTable SET whiteUsername = ? WHERE gameID = ?";
            } else if (requestedPlayerColor == PlayerColor.BLACK) {
                if (game.getBlackUsername() != null) {
                    throw new DataAccessException(403, "Error: already taken");
                }
                updateStatement = "UPDATE gameTable SET blackUsername = ? WHERE gameID = ?";
            } else {
                throw new DataAccessException(400, "Error: bad request");
            }

            try (var ps = conn.prepareStatement(updateStatement)) {
                ps.setString(1, username);
                ps.setInt(2, gameID);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException(500, "Error: failed to update game");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, String.format("Unable to add player to game: %s", e.getMessage()));
        }
    }

    @Override
    public void addObserver(Integer gameID, String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            GameData game = getGame(gameID);
            if (game == null) {
                throw new DataAccessException(400, "Error: game not found");
            }

            List<String> observers = getObservers(gameID);
            if (!observers.contains(username)) {
                observers.add(username);
            }

            String observersJson = new Gson().toJson(observers);
            String updateStatement = "UPDATE gameTable SET observers = ? WHERE gameID = ?";

            try (var ps = conn.prepareStatement(updateStatement)) {
                ps.setString(1, observersJson);
                ps.setInt(2, gameID);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DataAccessException(500, "Error: failed to update game observers");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, String.format("Unable to add observer to game: %s", e.getMessage()));
        }
    }

    @Override
    public List<String> getObservers(Integer gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            String query = "SELECT observers FROM gameTable WHERE gameID = ?";
            try (var ps = conn.prepareStatement(query)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String observersJson = rs.getString("observers");
                        if (observersJson != null && !observersJson.isEmpty()) {
                            return new Gson().fromJson(observersJson, new TypeToken<List<String>>(){}.getType());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, String.format("Unable to get observers for game: %s", e.getMessage()));
        }
        return new ArrayList<>();
    }

    @Override
    public void clearGameData() throws DataAccessException {
        try {
            var statement = "TRUNCATE gameTable";
            executeUpdate(statement);
        } catch (Exception e) {
            throw new DataAccessException(400, "Unable to clear data: " + e.getMessage());
        }
    }


    private void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                executeUpdate(ps, params);
                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    rs.getInt(1);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private Integer executeInsert(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                executeUpdate(ps, params);
                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to insert into database: %s, %s", statement, e.getMessage()));
        }
        return null;
    }

    static void executeUpdate(PreparedStatement ps, Object[] params) throws SQLException {
        for (var i = 0; i < params.length; i++) {
            var param = params[i];
            switch (param) {
                case String p -> ps.setString(i + 1, p);
                case Integer p -> ps.setInt(i + 1, p);
                case null -> ps.setNull(i + 1, NULL);
                default -> {
                }
            }
        }
        ps.executeUpdate();
    }
}
