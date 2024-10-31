package dataaccess.sqldao;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.idao.GameDAO;
import model.GameData;
import utils.PlayerColor;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

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
              PRIMARY KEY (`gameID`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
        };
        configureDatabase(createStatements);
    }

    @Override
    public void addGame(GameData gameData) throws DataAccessException {
        var statement = "INSERT INTO gameTable (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        var json = new Gson().toJson(gameData.getChessGame());
        executeUpdate(statement, gameData.getGameID(), gameData.getWhiteUsername(), gameData.getBlackUsername(), gameData.getGameName(), json);
    }

    @Override
    public GameData getGame(Integer gameID) throws DataAccessException {
        if (gameID != null) {
            try (var conn = getConnection()) {
                var statement = prepareStatement(conn, "SELECT * FROM gameTable WHERE gameID=?");
                statement.setInt(1, gameID);
                try (var rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            } catch (Exception e) {
                throw new DataAccessException(400, String.format("Unable to read data: %s", e.getMessage()));
            }
        }
        return null;
    }

    private Connection getConnection() throws SQLException, DataAccessException {
        return DatabaseManager.getConnection();
    }

    private PreparedStatement prepareStatement(Connection conn, String statement) throws SQLException {
        return conn.prepareStatement(statement);
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        var gameJson = rs.getString("game");

        ChessGame chessGame = new Gson().fromJson(gameJson, ChessGame.class);

        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM gameTable";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
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
                    gameJson,
                    gameData.getGameID());
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
