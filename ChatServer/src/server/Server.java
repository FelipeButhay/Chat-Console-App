package server;

import java.net.*;
import java.util.HashMap;
import java.util.Set;

import ChatApp.Message;

class Server {
    public static final int FLAG_NULL = 0;
    public static final int FLAG_MESSAGE = 1;
    
    public static final int FLAG_LOGIN_REQUEST = 2;
    public static final int FLAG_LOGIN_VALID = 3;
    public static final int FLAG_LOGIN_INVALID = 4;

    public static final int FLAG_IP_SAVED_REQUEST = 5;
    public static final int FLAG_IP_SAVED = 6;
    public static final int FLAG_IP_NOT_SAVED = 7;

    public static final int FLAG_UN_SAVED_REQUEST = 8;
    public static final int FLAG_UN_SAVED_CONN = 9;
    public static final int FLAG_UN_SAVED = 10;
    public static final int FLAG_UN_NOT_SAVED = 11;
    public static final int FLAG_UN_INVALID = 12;

    private static final int PORT = 8080;

    // k = ip, v = username
    public static BiHashMap<String, String> users = new BiHashMap<>();
    public static HashMap<String, ClientHandler> clientHandlers = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server running on port: " + PORT);
            System.out.println("Server ip: " + InetAddress.getLocalHost().getHostAddress());

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    String clientIP = clientSocket.getInetAddress().getHostAddress();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandlers.put(clientIP, clientHandler);
                    clientHandlers.get(clientIP).start();

                    System.out.println("Cliente con ip: " + clientIP + " conectado");

                } catch (Exception e) {
                    System.out.println("Error conectando el cliente");
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println("Error creando en el server");
            e.printStackTrace();
        }
    }

    public static void removeClientHandler(String clientIP) {
        clientHandlers.remove(clientIP);
    }

    public static void sendMessageTo(Message message, String userName) {
        String receiverIP = users.getInverse(userName);
        clientHandlers.get(receiverIP).sendMessage(message);
    }

    public static String getUserName(String userIP) {
        return users.get(userIP);
    }

    public static String getUserIp(String userName) {
        return users.getInverse(userName);
    }

    public static boolean isUserLoggedIn(String userIP, String userName) {
        return users.containsKV(userIP, userName);
    }

    public static boolean userExists(String userName) {
        return users.containsValue(userName);
    }

    public static boolean ipExists(String clientIP) {
        return users.containsKey(clientIP);
    }

    public static boolean isUserConnected(String userName) {
        return clientHandlers.containsKey(getUserIp(userName));
    }

    public static void logInUser(String userIP, String username) {
        users.put(userIP, username);
    }
    
    public static Set<String> getUserNamesList() {
        return users.getAllValues();
    } 
}
