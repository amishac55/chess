package dataaccess;

import chess.ChessGame;
import dataaccess.sqldao.SQLGameDAO;
import model.GameData;
import org.junit.jupiter.api.*;
import utils.PlayerColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLGameDAOTest {

    private static SQLGameDAO gameDAO;

    @BeforeAll
    static void initializeDAO() throws DataAccessException {
        gameDAO = new SQLGameDAO();
    }

    @AfterAll
    static void cleanUp() throws DataAccessException {
        gameDAO.clearGameData();
    }

    @BeforeEach
    void resetDatabase() throws DataAccessException {
        gameDAO.clearGameData();
    }

    @Test
    void addGame_ShouldAddGameSuccessfully() throws DataAccessException {
        GameData game = new GameData(1, null, null, "Test Game", new ChessGame());

        assertDoesNotThrow(() -> gameDAO.addGame(game));

        GameData retrievedGame = gameDAO.getGame(1);
        assertNotNull(retrievedGame);
        assertEquals("Test Game", retrievedGame.getGameName());
    }

    @Test
    void addGame_ShouldThrowException_WhenDuplicateId() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "Test Game 1", new ChessGame());
        GameData game2 = new GameData(1, null, null, "Test Game 2", new ChessGame());

        gameDAO.addGame(game1);
        assertThrows(DataAccessException.class, () -> gameDAO.addGame(game2));
    }

    @Test
    void getGame_ShouldReturnGame_WhenIdExists() throws DataAccessException {
        GameData game = new GameData(1, "white", "black", "Test Game", new ChessGame());
        gameDAO.addGame(game);

        GameData retrievedGame = gameDAO.getGame(1);

        assertNotNull(retrievedGame);
        assertEquals("Test Game", retrievedGame.getGameName());
        assertEquals("white", retrievedGame.getWhiteUsername());
        assertEquals("black", retrievedGame.getBlackUsername());
    }

    @Test
    void getGame_ShouldReturnNull_WhenIdDoesNotExist() throws DataAccessException {
        GameData retrievedGame = gameDAO.getGame(999);
        assertNull(retrievedGame);
    }

    @Test
    void listGames_ShouldReturnAllGames() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "Test Game 1", new ChessGame());
        GameData game2 = new GameData(2, "whiteUser", "blackUser", "Test Game 2", new ChessGame());

        gameDAO.addGame(game1);
        gameDAO.addGame(game2);

        Collection<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());

        List<GameData> gamesList = new ArrayList<>(games);

        // Verify games details
        assertEquals(1, gamesList.get(0).getGameID());
        assertEquals("Test Game 1", gamesList.get(0).getGameName());
        assertNull(gamesList.get(0).getWhiteUsername());
        assertNull(gamesList.get(0).getBlackUsername());

        assertEquals(2, gamesList.get(1).getGameID());
        assertEquals("Test Game 2", gamesList.get(1).getGameName());
        assertEquals("whiteUser", gamesList.get(1).getWhiteUsername());
        assertEquals("blackUser", gamesList.get(1).getBlackUsername());

        assertNotNull(gamesList.get(0).getChessGame());
        assertNotNull(gamesList.get(1).getChessGame());
    }

    @Test
    void listGames_ShouldReturnEmpty_WhenNoGamesExist() throws DataAccessException {
        Collection<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void deleteGame_ShouldRemoveGame_WhenIdExists() throws DataAccessException {
        GameData game = new GameData(1, null, null, "Test Game", new ChessGame());
        gameDAO.addGame(game);

        assertDoesNotThrow(() -> gameDAO.deleteGame(1));
        assertNull(gameDAO.getGame(1));
    }

    @Test
    void deleteGame_ShouldNotAffectDatabase_WhenIdDoesNotExist() throws DataAccessException {
        GameData game = new GameData(1, null, null, "Test Game", new ChessGame());
        gameDAO.addGame(game);
        int initialCount = gameDAO.listGames().size();

        gameDAO.deleteGame(999);

        assertEquals(initialCount, gameDAO.listGames().size());
        assertNotNull(gameDAO.getGame(1));
    }

    @Test
    void addPlayer_ShouldAddPlayerToGame_WhenSlotIsAvailable() throws DataAccessException {
        GameData game = new GameData(1, null, null, "Test Game", new ChessGame());
        gameDAO.addGame(game);

        assertDoesNotThrow(() -> gameDAO.addPlayer(1, "testUser", PlayerColor.WHITE));
        GameData updatedGame = gameDAO.getGame(1);

        assertEquals("testUser", updatedGame.getWhiteUsername());
    }

    @Test
    void addPlayer_ShouldThrowException_WhenSlotAlreadyTaken() throws DataAccessException {
        GameData game = new GameData(1, "existingUser", null, "Test Game", new ChessGame());
        gameDAO.addGame(game);

        assertThrows(DataAccessException.class, () -> gameDAO.addPlayer(1, "testUser", PlayerColor.WHITE));
    }

    @Test
    void clearGameData_ShouldRemoveAllGames() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "Test Game 1", new ChessGame());
        GameData game2 = new GameData(2, null, null, "Test Game 2", new ChessGame());

        gameDAO.addGame(game1);
        gameDAO.addGame(game2);

        assertDoesNotThrow(() -> gameDAO.clearGameData());
        assertTrue(gameDAO.listGames().isEmpty());
    }
}