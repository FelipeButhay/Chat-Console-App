package client;

import java.io.ObjectInputStream;

import ChatApp.Message;

public class MessageListener extends Thread {
    private ObjectInputStream input;

    public MessageListener(ObjectInputStream input) {
        this.input = input;
    }

    @Override
    public void run() {
        try {
            Object receivedMessage;
            while ((receivedMessage = input.readObject()) != null) {
                if (receivedMessage instanceof Message) {
                    Message message = (Message) receivedMessage;
                    Client.incomingMessages.add((Message) message);
                    //System.out.println("{" + message.getData() + "}");
                    //System.out.println("{" + message.getFlag() + "}");

                    if (message.getFlag() == Client.FLAG_MESSAGE) {
                        System.out.println();
                        System.out.print(message.getTransmitter());
                        System.out.print(" to ");
                        System.out.print(message.getReceiver());
                        System.out.print(": ");
                        System.out.println(message.getData());

                        if (Client.myReceiver != null) {
                            System.out.print(Client.myReceiver + " ");
                        }
                        System.out.print("> ");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
