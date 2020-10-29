package Nodes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.*;

public class Sender extends Thread{
    Node node;

    Sender(Node node){
        this.node = node;
    }

    public void run(){
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Map<UUID, Set<Neighbour>> queue = node.getQueue();
                for (UUID uuid : queue.keySet()) {
                    for (Neighbour neighbour : queue.get(uuid)) {
                        if (node.getMsgByUUID(uuid).isAnswer()){
                            byte[] msg = node.getMsgByUUID(uuid).getByteMessage(true);
                            int msgLen = msg.length;
                            DatagramPacket packet = new DatagramPacket(msg, msgLen, neighbour.getAddress(), neighbour.getPort());
                            node.getSocket().send(packet);
                            neighbour.setCDforMsg(uuid);
                            node.delNeighFromSendList(uuid, neighbour);
                        }

                        else if (System.currentTimeMillis() - neighbour.getCDbyUUID(uuid) > 3000) {
                            byte[] msg = node.getMsgByUUID(uuid).getByteMessage(false);
                            int msgLen = msg.length;
                            DatagramPacket packet = new DatagramPacket(msg, msgLen, neighbour.getAddress(), neighbour.getPort());
                            node.getSocket().send(packet);
                            neighbour.setCDforMsg(uuid);
                        }
                    }
                    if (queue.get(uuid).isEmpty()){
                        node.delMsgFromQueue(uuid);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<Neighbour> getNeighbourList (){
        return node.getNeighList();
    }
}
