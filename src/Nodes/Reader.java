package Nodes;

import java.util.Scanner;
import java.util.UUID;

public class Reader extends Thread {
    Node node;
    Scanner input;

    Reader(Node node){
        this.node = node;
        input = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()){
            String message = input.nextLine();
            UUID uuid = UUID.randomUUID();
            String sender = node.getName();

            node.addMsgToQueue(new Message(uuid, message, sender, false), null);

        }
    }
}
