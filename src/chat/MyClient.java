package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MyClient {
    private static Chat clientChat;

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        try {
            Socket clientSocket = new Socket("localhost", 3000);
            Username userDialog = new Username();
            PrintWriter socketWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String username;
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.printf(socketReader.readLine());
            username = consoleReader.readLine();
            socketWriter.println(username);
            System.out.println(socketReader.readLine());
            new Thread() {
                public void run() {
                    try {

                        String clientInput;
                        do {
                            clientInput = consoleReader.readLine();
                            socketWriter.println(username + ": " + clientInput);
                        } while (!clientInput.equals("bye"));
                        System.out.println("client shutdown");
                    } catch (IOException err1) {
                        err1.printStackTrace();
                    }
                }
            }.start();
            String serverInput;
            serverInput = socketReader.readLine();
            while (serverInput != null && !serverInput.equals("bye")) {
                System.out.println(serverInput);
                serverInput = socketReader.readLine();
            }
            clientSocket.close();
        } catch (IOException err) {
            err.printStackTrace();
        } finally {
            System.exit(0);
        }

    }

}
