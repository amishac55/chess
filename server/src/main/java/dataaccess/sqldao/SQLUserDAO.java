package dataaccess.sqldao;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.idao.UserDAO;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class SQLUserDAO extends SQLBaseClass implements UserDAO {

    public SQLUserDAO() throws DataAccessException {
        String[] createStatements = {
                """
            CREATE TABLE IF NOT EXISTS userTable (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`username`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
        };
        configureDatabase(createStatements);
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

        try (var conn = DatabaseManager.getConnection()) {
            var checkStatement = "SELECT username FROM userTable WHERE username=?";
            try (var ps = conn.prepareStatement(checkStatement)) {
                ps.setString(1, user.username());
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new DataAccessException(403, "Error: username already taken");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, "Error: Internal server error while checking for existing user. " + e.getMessage());
        }


        var insertStatement = "INSERT INTO userTable (username, password, email) VALUES (?, ?, ?)";
        try {
            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            executeUpdate(insertStatement, user.username(), hashedPassword, user.email());
        } catch (DataAccessException e) {
            throw new DataAccessException(500, "Error: Internal server error while creating user. " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM userTable WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, "Error: Internal server error. " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT password FROM userTable WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String hashedPassword = rs.getString("password");
                        return BCrypt.checkpw(password, hashedPassword);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, "Error: Internal server error. " + e.getMessage());
        }
        throw new DataAccessException(401, "Error: username does not exist");
    }

    @Override
    public void clearUserData() throws DataAccessException {
        try {
            var statement = "TRUNCATE userTable";
            executeUpdate(statement);
        } catch (Exception e) {
            throw new DataAccessException(400, "Error: Unable to clear user data " + e.getMessage());
        }
    }

    protected void executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, String.format("Unable to update database: %s, %s", statement, e.getMessage()));
        }
    }
}
