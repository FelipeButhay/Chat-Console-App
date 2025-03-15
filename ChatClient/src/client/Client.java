package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import ChatApp.Message;

public class Client {
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

    private static Scanner scanner;
    
    private static Socket socket;
    private static ObjectInputStream input;
    private static ObjectOutputStream output;

    private static final int PORT = 8080;
    private static String serverIP;

    public static String myReceiver = null;
    private static String myUserName;

    private static MessageListener messageListener;
    // private static MessagePrinter messagePrinter;

    public static Queue<Message> incomingMessages = new LinkedList<>();

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        // chequea que la ip sea valida
        do {
            System.out.println("Ingrese la IP del servidor: ");
            System.out.print("> ");
            serverIP = scanner.nextLine();
        } while (!successfulConection(PORT));

        System.out.println("Se conecto exitosamente al servidor");

        // Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        //     try {
        //         socket.close();
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // }));

        // conecta al server
        boolean runClient;
        try {
            runClient = true;

            output = new ObjectOutputStream(socket.getOutputStream());    
            input = new ObjectInputStream(socket.getInputStream());

        } catch (Exception e) {
            System.out.println("Error conectando al servidor");
            runClient = false;
            e.printStackTrace();
        }
        
        System.out.println("Streams creados exitosamente");

        messageListener = new MessageListener(input);
        messageListener.start();

        // se encarga del login

        Message ipExistRequest = new Message();
        ipExistRequest.setFlag(FLAG_IP_SAVED_REQUEST);
        sendMessage(ipExistRequest);
        
        while (incomingMessages.isEmpty()) { wait(10); }

        Message responseMessage = incomingMessages.poll();

        if (responseMessage.getFlag() == FLAG_IP_SAVED) {
            myUserName = responseMessage.getData();

            System.out.println("Esta ip ya se conecto con el nombre " + myUserName);

        } else if (responseMessage.getFlag() == FLAG_IP_NOT_SAVED){
            boolean logInValid = false;
            do {

                System.out.println("Ingrese un nombre de usuario: ");
                System.out.print("> ");
                myUserName = scanner.nextLine();

                Message logInRequest = new Message();
                logInRequest.setFlag(FLAG_LOGIN_REQUEST);
                logInRequest.setData(myUserName);
                sendMessage(logInRequest);

                while (incomingMessages.isEmpty()) { wait(10); }
                Message logInResponseMessage = incomingMessages.poll();
                switch (logInResponseMessage.getFlag()) {
                    case FLAG_LOGIN_VALID: logInValid = true; break;
                    case FLAG_LOGIN_INVALID: logInValid = false; break;
                }
                System.out.println(logInResponseMessage.getData());

            } while (!logInValid);
        }

        // loop

        // messagePrinter = new MessagePrinter();
        // messagePrinter.start();

        while (runClient) {
            if (myReceiver != null) {
                System.out.print(myReceiver + " ");
            }
            System.out.print("> ");

            String line = scanner.nextLine();
            String[] tokens = line.split(" ");

            if (line.charAt(0) == '/') {

                if (tokens[0].equals("/r")) {
                    if (tokens.length == 2) {
                        if (getUsernameFlag(tokens[1]) == FLAG_UN_SAVED_CONN) {
                            myReceiver = tokens[1];
                        } else {
                            System.out.println("Receptor no encontrado");
                        }
                    } else {
                        System.out.println("El comando '/r' toma 1 argumento");
                    }
                } else if (tokens[0].equals("/c")) {
                    System.out.println("Cliente cerrado");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runClient = false;
                } else if (tokens[0].equals("/h")) {
                    System.out.println("<< Comandos disponibles >>");
                    System.out.println("    - '/r' Cambia el receptor (con 'all' envias a todos)");
                    System.out.println("    - '/c' Cierra el cliente");
                    System.out.println("    - '/h' Muestra los comandos disponibles");
                } else {
                    System.out.println("Para ver los comandos disponibles use '/h'");
                }
            } else {
                Message message = new Message();

                message.setFlag(FLAG_MESSAGE);
                message.setData(line);
                //System.out.println("{" + line + "}");
                message.setTransmitter(myUserName);
                message.setReceiver(myReceiver);

                sendMessage(message);
            }
        }
    }

    private static boolean successfulConection(int puerto) {
        String lanIP = "127.0.0.1";

        try {
            InetAddress localAddress = InetAddress.getByName(lanIP);
            socket = new Socket();
            socket.bind(new java.net.InetSocketAddress(localAddress, 0));
            socket.connect(new java.net.InetSocketAddress(localAddress, puerto), 1000);
            return true;
        } catch (IOException e) {
            System.out.println("No se encontro ningun servidor en la IP: " + lanIP);
            return false;
        }
    }

    private static int getUsernameFlag(String userName) {
        userName = userName.trim();

        if (userName.toLowerCase().equals("server")) {
            return FLAG_UN_SAVED_CONN;
        }

        if (userName.toLowerCase().equals("all")) {
            return FLAG_UN_SAVED_CONN;
        }

        if (userName.contains(" ")){
            return FLAG_UN_INVALID;
        }

        Message userNameExistRequest = new Message();
        userNameExistRequest.setFlag(FLAG_UN_SAVED_REQUEST);
        userNameExistRequest.setData(userName);
        sendMessage(userNameExistRequest);
            
        while (incomingMessages.isEmpty() || incomingMessages.peek().getFlag() == FLAG_MESSAGE) { wait(10); }
        Message responseMessage = (Message) incomingMessages.poll();
        // System.out.println(responseMessage.getFlag());
        return responseMessage.getFlag();
    }

    private static void sendMessage(Message message) {
        // System.out.println("    Message data: " + message.getData());
        // System.out.println("    Message flag: " + message.getFlag());

        if (output == null) {
            System.out.println("Output stream null");
            return;
        }

        try {
            output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void wait(int millis) {
        try { 
            Thread.sleep(millis); 
        } catch (InterruptedException e) {  
            e.printStackTrace(); 
        }
    }
}