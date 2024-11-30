package ui;

import client.ServerFacade;
import exception.ResponseException;
import requests.LoginRequest;
import requests.RegisterRequest;

import java.util.Scanner;

import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class PreLogin {

    ServerFacade serverFacade;
    PostLogin postLogin;

    public PreLogin(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
        postLogin = new PostLogin(serverFacade);
    }

    public void run() {
        boolean loggedIn = false;
        out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
        out.println("Welcome to Chess! Enter 'help' to get started.");
        while (!loggedIn) {
            String[] input = getUserInput();
            switch (input[0]) {
                case "quit":
                    return;
                case "help":
                    printHelpMenu();
                    break;
                case "login":
                    if (input.length != 3) {
                        out.println("Please provide a username and password");
                        printLogin();
                        break;
                    }
                    try {
                        serverFacade.login(new LoginRequest(input[1], input[2]));
                        out.println("You are now logged in");
                        loggedIn = true;
                        break;
                    } catch (ResponseException e) {
                        out.println("Username or password incorrect, please try again");
                        out.println(e.getMessage());
                        printLogin();
                        break;
                    }
                case "register":
                    if (input.length != 4) {
                        out.println("Please provide a username, password, and email");
                        printRegister();
                        break;
                    }
                    try {
                        serverFacade.register(new RegisterRequest(input[1], input[2], input[3]));
                        out.println("You are now registered and logged in");
                        loggedIn = true;
                        break;
                    } catch (ResponseException e) {
                        out.println("Error registering user: " + e.getMessage());
                        printRegister();
                        break;
                    }
                default:
                    out.println("Command not recognized, please try again");
                    printHelpMenu();
                    break;
            }
        }
        postLogin.run();
    }

    private String[] getUserInput() {
        out.print("\n[LOGGED OUT] >>> ");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().split(" ");
    }

    private void printHelpMenu() {
        printRegister();
        printLogin();
        out.println("quit - stop playing");
        out.println("help - show this menu");
    }

    private void printRegister() {
        out.println("register <USERNAME> <PASSWORD> <EMAIL> - create a new user");
    }

    private void printLogin() {
        out.println("login <USERNAME> <PASSWORD> - login to an existing user");
    }
}
