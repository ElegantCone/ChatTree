package Nodes;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Set;
import java.util.UUID;

public class Pinger extends Thread{
    Node node;
    long lastPingTime;

    Pinger(Node node){
        this.node = node;
        lastPingTime = 0;
    }

    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            for (Neighbour neighbour : node.getNeighList()){
                if (System.currentTimeMillis() - neighbour.getLastSentPingTime() < 2000) continue;
                try{
                    byte[] code = {(byte)4};
                    DatagramPacket packet =
                            new DatagramPacket(code, code.length, neighbour.getAddress(), neighbour.getPort());
                    node.getSocket().send(packet);
                    neighbour.setLastSentPingTime();
                    //System.out.println("Sending ping to " + neighbour.getName());
                } catch (IOException e) {
                    System.out.println("Can't send packet");
                }

                if (System.currentTimeMillis() - neighbour.getPingRecvTime() > 5000){
                    System.out.println(neighbour.getName() + " disconnected");
                    node.disconnect(neighbour);
                }
            }
        }
    }



}
