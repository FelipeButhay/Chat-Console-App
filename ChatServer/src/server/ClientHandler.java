package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import ChatApp.Message;

class ClientHandler extends Thread {
    private Socket socket;
    private String clientIP;

    private ObjectInputStream input;
    private ObjectOutputStream output;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientIP = socket.getInetAddress().getHostAddress();
    }
    
    @Override
    public void run() {
        try {
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());

            Object receivedMessage;
            while ((receivedMessage = input.readObject()) != null) {
                if (receivedMessage instanceof Message) {

                    Message message = (Message) receivedMessage;
                    switch (message.getFlag()) {
                        case Server.FLAG_NULL:
                            System.out.println("Error en el mensaje: flag no recibida");
                            break;
                        case Server.FLAG_MESSAGE:
                            System.out.println("FLAG_MESSAGE");
                            processMessage(message);
                            break;
                        case Server.FLAG_LOGIN_REQUEST:
                            System.out.println("FLAG_LOGIN");
                            processLogIn(message);
                            break;
                        case Server.FLAG_IP_SAVED_REQUEST:
                            System.out.println("FLAG_IP_SAVED_REQUEST");
                            processIPSaved(message);
                            break;
                        case Server.FLAG_UN_SAVED_REQUEST:
                            System.out.println("FLAG_UN_SAVED_REQUEST");
                            processUNSaved(message);
                            break;
                        default: 
                            System.out.println("Flag invalida: " + message.getFlag());
                            break;
                    }

                } else {
                    System.out.println("Mensaje de tipo no esperado");
                }
            }

        } catch (IOException | ClassNotFoundException e1) {
            e1.printStackTrace();

            try {
                socket.close();
                Server.removeClientHandler(clientIP);
                System.out.println("Cliente con ip: " + clientIP + " desconectado");
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            System.out.println(message.getFlag());
            output.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void processLogIn(Message message) {
        String userName = message.getData();
        Message response = new Message();

        response.setTransmitter("Server");
        response.setReceiver("you");

        if (!Server.userExists(userName)) {
            Server.logInUser(clientIP, userName);

            response.setFlag(Server.FLAG_LOGIN_VALID);
            response.setData("Nombre de usuario aceptado: " + userName);

        } else {
            //System.out.println("Ya existe un usuario con este nombre o ip: " + clientIP + " - " + userName);

            response.setFlag(Server.FLAG_LOGIN_INVALID);
            response.setData("Nombre de usuario rechazado");

        }
        sendMessage(response);
    }

    private void processMessage(Message message) {
        System.out.println(Server.users.getAllKeys());
        System.out.println(Server.users.getAllValues());

        Message response = new Message();
        
        response.setFlag(Server.FLAG_MESSAGE);
        response.setTransmitter("Server");
        response.setReceiver("you");

        if (!Server.userExists(message.getReceiver())) {
            response.setData("El usuario receptor no existe");
            sendMessage(response);
            return;
        }

        if (!Server.isUserConnected(message.getReceiver())) {
            response.setData("El usuario receptor no esta conectado");
            sendMessage(response);
            return;
        }

        if (message.getReceiver().toLowerCase().equals("all")) {
            for (String iterUserName : Server.getUserNamesList()) {
                if (Server.getUserIp(iterUserName).equals(clientIP)) {
                    continue;
                }

                Server.sendMessageTo(message, iterUserName);
            }
            
            return;
        }

        Server.sendMessageTo(message, message.getReceiver());
    }

    private void processIPSaved(Message message) {
        Message response = new Message();

        response.setTransmitter("Server");
        response.setReceiver("you");

        if (Server.ipExists(clientIP)){
            response.setFlag(Server.FLAG_IP_SAVED);
            response.setData(Server.getUserName(clientIP));
        } else {
            response.setFlag(Server.FLAG_IP_NOT_SAVED);
        }

        sendMessage(response);
    }

    private void processUNSaved(Message message) {
        Message response = new Message();

        response.setTransmitter("Server");
        response.setReceiver("you");

        System.out.println(message.getData());

        if (Server.isUserConnected(message.getData())){
            response.setFlag(Server.FLAG_UN_SAVED_CONN);
            // System.out.println("CONNECTED");

        } else if (Server.userExists(message.getData())){
            response.setFlag(Server.FLAG_UN_SAVED);
            // System.out.println("EXISTS BUT NOT CONNECTED");

        } else {
            response.setFlag(Server.FLAG_UN_NOT_SAVED);
            // System.out.println("DOESNT EXISTS");
        }

        sendMessage(response);
    }
}
