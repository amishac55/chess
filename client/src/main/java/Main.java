import chess.*;
import client.ServerFacade;
import ui.PreLogin;

public class Main {
    public static void main(String[] args) {
        System.out.println("♕ 240 Chess Client:");
        String baseUrl = "http://localhost:8080";
        ServerFacade serverFacade = ServerFacade.getInstance(baseUrl);

        PreLogin preLogin = new PreLogin(serverFacade);
        preLogin.run();
        System.out.println("Exited");
    }
}