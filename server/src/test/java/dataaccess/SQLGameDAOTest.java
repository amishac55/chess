package dataaccess;

import chess.ChessGame;
import dataaccess.sqldao.SQLGameDAO;
import model.GameData;
import model.GameRecord;
import org.junit.jupiter.api.*;
import utils.PlayerColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLGameDAOTest {

    private static SQLGameDAO gameDAO;

    @BeforeAll
    static void setUp() throws DataAccessException {
        gameDAO = new SQLGameDAO();
    }

    @AfterAll
    static void tearDown() throws DataAccessException {
        gameDAO.clearGameData();
    }

    @BeforeEach
    void clearDatabase() throws DataAccessException {
        gameDAO.clearGameData();
    }

    @Test
    void addGamePositive() throws DataAccessException {
        GameData game = new GameData(1, null, null, "Test Game", new ChessGame());
        assertDoesNotThrow(() -> gameDAO.addGame(game));

        GameData retrievedGame = gameDAO.getGame(1);
        assertNotNull(retrievedGame);
        assertEquals("Test Game", retrievedGame.getGameName());
    }

    @Test
    void addGameNegativeNullGameName() throws DataAccessException {
        GameData game = new GameData(1, null, null, null, new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.addGame(game));
    }

    @Test
    void getGamePositive() throws DataAccessException {
        GameData game = new GameData(1, "white", "black", "Test Game", new ChessGame());
        gameDAO.addGame(game);

        GameData retrievedGame = gameDAO.getGame(1);
        assertNotNull(retrievedGame);
        assertEquals("Test Game", retrievedGame.getGameName());
        assertEquals("white", retrievedGame.getWhiteUsername());
        assertEquals("black", retrievedGame.getBlackUsername());
    }

    @Test
    void getGameNegativeNonExistentId() throws DataAccessException {
        GameData retrievedGame = gameDAO.getGame(999);
        assertNull(retrievedGame);
    }

    @Test
    void listGamesPositive() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "Test Game 1", new ChessGame());
        GameData game2 = new GameData(2, "whiteUser", "blackUser", "Test Game 2", new ChessGame());
        gameDAO.addGame(game1);
        gameDAO.addGame(game2);

        Collection<GameRecord> games = gameDAO.listGames();
        assertEquals(2, games.size());

        // Convert collection to list for easier assertion
        List<GameRecord> gamesList = new ArrayList<>(games);

        // Verify first game
        assertEquals(1, gamesList.get(0).gameID());
        assertEquals("Test Game 1", gamesList.get(0).gameName());
        assertNull(gamesList.get(0).whiteUsername());
        assertNull(gamesList.get(0).blackUsername());

        // Verify second game
        assertEquals(2, gamesList.get(1).gameID());
        assertEquals("Test Game 2", gamesList.get(1).gameName());
        assertEquals("whiteUser", gamesList.get(1).whiteUsername());
        assertEquals("blackUser", gamesList.get(1).blackUsername());

        // Verify that the games are in the correct order
        assertEquals(1, gamesList.get(0).gameID());
        assertEquals(2, gamesList.get(1).gameID());

        // Verify that the chess games are not null
        GameData gameData1 = gameDAO.getGame(gamesList.get(0).gameID());
        GameData gameData2 = gameDAO.getGame(gamesList.get(1).gameID());
        assertNotNull(gameData1.getChessGame());
        assertNotNull(gameData1.getChessGame());
    }

    @Test
    void listGamesNegativeEmptyDatabase() throws DataAccessException {
        Collection<GameRecord> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void deleteGamePositive() throws DataAccessException {
        GameData game = new GameData(1, null, null, "Test Game", new ChessGame());
        gameDAO.addGame(game);

        assertDoesNotThrow(() -> gameDAO.deleteGame(1));
        assertNull(gameDAO.getGame(1));
    }

    @Test
    void deleteGameNegativeNonExistentId() throws DataAccessException {
        // Add a game to ensure the database is not empty
        GameData game = new GameData(1, null, null, "Test Game", new ChessGame());
        gameDAO.addGame(game);
        int initialCount = gameDAO.listGames().size();

        // Try to delete a non-existent game
        gameDAO.deleteGame(999);

        assertEquals(initialCount, gameDAO.listGames().size());
        assertNotNull(gameDAO.getGame(1));
    }

    @Test
    void addPlayerPositive() throws DataAccessException {
        GameData game = new GameData(1, null, null, "Test Game", new ChessGame());
        gameDAO.addGame(game);

        assertDoesNotThrow(() -> gameDAO.addPlayer(1, "testUser", PlayerColor.WHITE));
        GameData updatedGame = gameDAO.getGame(1);
        assertEquals("testUser", updatedGame.getWhiteUsername());
    }

    @Test
    void addPlayerNegativeAlreadyTaken() throws DataAccessException {
        GameData game = new GameData(1, "existingUser", null, "Test Game", new ChessGame());
        gameDAO.addGame(game);

        assertThrows(DataAccessException.class, () -> gameDAO.addPlayer(1, "testUser", PlayerColor.WHITE));
    }

    @Test
    void clearGameDataPositive() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "Test Game 1", new ChessGame());
        GameData game2 = new GameData(2, null, null, "Test Game 2", new ChessGame());
        gameDAO.addGame(game1);
        gameDAO.addGame(game2);

        assertDoesNotThrow(() -> gameDAO.clearGameData());
        assertTrue(gameDAO.listGames().isEmpty());
    }
}