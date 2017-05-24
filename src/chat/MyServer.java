package chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    private static String serverFilesPath = System.getProperty("user.dir") + "/src/user_files";
    private static ArrayList<String> availableFiles;

    public static ArrayList<String> get_available_files() {
        final File folder = new File(serverFilesPath);
        ArrayList<String> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            files.add(fileEntry.getName());
        }
        return files;
    }

//    public static void addActiveUsers(String username){
//        if(activeUsers == null){
//            activeUsers = new ArrayList<>();
//        }
//        activeUsers.add(username);
//    }
//
//    public static ArrayList<String> getActiveUsers(){
//        return activeUsers;
//    }


    private static int sendMessage(PrintWriter socketWriter, String message) throws IOException {
        if (message.contains("upload")) {
            String[] parts = message.split(" ");
            File f = new File(parts[2]);
            System.out.println();
            String line = "BOT: Someone uploaded a file. In order to download it, type 'download " + f.getName() + " <path_where_you_want_to_save>'";
            socketWriter.println(line);
            return 0;
        }
        if (!message.contains("help") && !message.contains("download") && !message.contains("list")) {
            socketWriter.println(message);
        } else {
            if (message.length() > 10) {
                socketWriter.println(message);
            }
        }
        return 0;
    }

    private static int processCommand(String message, PrintWriter currentSocket) throws IOException {
        String[] parts = message.split(" ");

        if (parts[1].equals("download")) {
            if (parts.length < 4) {
                currentSocket.println("Invalid command; missing arguments");
                return 1;
            }
            TFTPClient cl = new TFTPClient();
            currentSocket.println(cl.download_file(parts[2], parts[3]));
            return 0;
        }

        if (parts[1].equals("upload")) {
            if (parts.length < 3) {
                currentSocket.println("Invalid command; missing arguments");
                return 1;
            }

            if (parts[2].contains(".exe")) {
                currentSocket.println("You can not upload executables on the server");
                return 1;
            }

            TFTPClient cl = new TFTPClient();
            currentSocket.println(cl.upload_file(parts[2]));
            return 0;
        }
        if (parts[1].equals("help")) {
            currentSocket.println("BOT: In order to DOWNLOAD a file write the following command: download <filename> <path_to_save_folder_filename> \n" +
                    "BOT: In order to UPLOAD a file write the following command: upload <path_to_file> \nBOT: In order to EXIT write: bye \n"
                    + "BOT: In order to LIST available files for download on the server: list\n" + "BOT: Happy chatting! :)");
            return 0;
        }
        if (parts[1].equals("list")) {
            int idx;
            availableFiles = get_available_files();
            if (availableFiles.size() == 0) {
                currentSocket.println("No files available to download on server.");
            } else currentSocket.println("Available files for download: ");
            for (idx = 0; idx < availableFiles.size(); idx++) {
                currentSocket.println(availableFiles.get(idx));
            }
        }
        return 0;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(3000);
        List<String> activeUsers = new ArrayList<>();
        List<PrintWriter> socketWriters = new ArrayList<>();
        try {
            while (true) {
                final Socket s = ss.accept();
                new Thread() {
                    public void run() {
                        try {
                            PrintWriter currentSocketWriter = new PrintWriter(s.getOutputStream(), true);
                            socketWriters.add(currentSocketWriter);
                            BufferedReader socketReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                            currentSocketWriter.println("Please choose a username: ");
                            String username = socketReader.readLine();
                            activeUsers.add(username);
                            currentSocketWriter.println("Welcome to our chat room, " + username + "! Type 'help' for more info");
                            String line;
                            while (!(line = socketReader.readLine()).contains("bye")) {
                                processCommand(line, currentSocketWriter);
                                for (PrintWriter socketWriter : socketWriters) {
                                    if (socketWriter != currentSocketWriter) {
                                        sendMessage(socketWriter, line);
                                    }
                                }
                            }
                            currentSocketWriter.println("bye");
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                s.close();
                            } catch (IOException err) {
                                err.printStackTrace();
                            }
                        }

                    }
                }.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ss.close();
            } catch (IOException err) {
                err.printStackTrace();
            }

        }

    }
}