package Nodes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class Receiver extends Thread {
    Node node;
    Random random = new Random();

    Receiver(Node node){
        this.node = node;
    }

    public void run(){
        try {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] message = new byte[4096];
                DatagramPacket packet = new DatagramPacket(message, message.length);
                node.getSocket().receive(packet);
                byte[] code = new byte[1];
                ByteArrayInputStream bin = new ByteArrayInputStream(message);
                bin.read(code);

                if (code[0] == 0 || code[0] == 1){
                    receiveGreetingMessage(bin, packet, code[0]);
                    bin.close();
                }
                //2 - message, 3 - answer
                if (code[0] == 2 || code[0] == 3){
                    if (code[0] == 2 && node.getLossPersent() < random.nextInt(100)) {
                        Message recvMsg = receiveMessage(bin, packet, code[0]);
                        Neighbour neighbour = node.getNeighbour(packet.getPort());
                        if (neighbour == null) throw new Exception();
                        Message msg = new Message(recvMsg.getUUID(), "Got it!", node.getName(), true);
                        Message shareMsg = new Message(UUID.randomUUID(), recvMsg.getMessage(), recvMsg.getName(), false);
                        if (node.getNeighList().size() != 1) {
                            node.addMsgToQueue(shareMsg, neighbour);
                        }
                        node.addAnswerToQueue(msg, neighbour);

                    }
                    else {
                        System.out.println("Message was lost! Trying to receive again...");
                    }
                    if (code[0] == 3) {
                        Message recvMsg = receiveMessage(bin, packet, code[0]);
                        Neighbour neighbour = node.getNeighbour(packet.getPort());
                        if (neighbour == null) throw new Exception();
                        node.delNeighFromSendList(recvMsg.getUUID(), neighbour);
                    }
                    bin.close();
                }

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Can't find node to send answer");
        }
    }

    //receive node's name, addr, port and savepoint(name, addr, port)
    private void receiveGreetingMessage(ByteArrayInputStream bin, DatagramPacket packet, byte code) throws IOException {
        int length = readLengthFromInput(bin);
        byte[] name_b = new byte[length];
        bin.read(name_b);
        String name = new String(name_b); //got node's name
        System.out.println("Node " + name + " connected! From port: " + packet.getPort());
        Neighbour neighbour = new Neighbour(name, packet.getAddress(), packet.getPort());
        node.addNeighbour(neighbour);
        int spAddrLen = readLengthFromInput(bin);
        if (spAddrLen == 0) {
            if (code == 0){
                Neighbour spNeighbour = new Neighbour(name, packet.getAddress(), packet.getPort());
                node.createSavepoint(spNeighbour);
                node.connect(packet.getAddress(), packet.getPort(), true);
            }
            return;
        }
        //else
        byte[] spAddr_b = new byte[spAddrLen];
        bin.read(spAddr_b);
        byte[] spAddr_bTemp = Arrays.copyOfRange(spAddr_b, 1, spAddrLen);
        String spAddrName = new String(spAddr_bTemp);
        InetAddress spAddr = InetAddress.getByName(spAddrName);
        int spPortLen = readLengthFromInput(bin);
        byte[] spPort_b = new byte[spPortLen];
        bin.read(spPort_b);
        int spPort = ByteBuffer.wrap(spPort_b).getInt();
        int spNameLen = readLengthFromInput(bin);
        byte[] spName_b = new byte[spNameLen];
        bin.read(spName_b);
        String spName = new String(spName_b);
        Neighbour spNeighbour;
        if (node.getName().equals(spName) && node.getPort() == spPort){
            spNeighbour = new Neighbour(name, packet.getAddress(), packet.getPort());
        }
        else {
            spNeighbour = new Neighbour(spName, spAddr, spPort);
        }
        node.createSavepoint(spNeighbour);
        if (code == 0){
            node.connect(packet.getAddress(), packet.getPort(), true);
        }
    }

    private Message receiveMessage(ByteArrayInputStream bin, DatagramPacket packet, byte code) throws IOException {
        byte[] msbUUID_b = new byte[8];
        bin.read(msbUUID_b);
        byte[] lsbUUID_b = new byte[8];
        bin.read(lsbUUID_b);
        long msbUUID = ByteBuffer.wrap(msbUUID_b).getLong();
        long lsbUUID = ByteBuffer.wrap(lsbUUID_b).getLong();
        UUID uuid = new UUID(msbUUID, lsbUUID);
        int nameLen = readLengthFromInput(bin);
        byte[] name_b = new byte[nameLen];
        bin.read(name_b);
        String name = new String(name_b);
        int msgLen = readLengthFromInput(bin);
        byte[] msg_b = new byte[msgLen];
        bin.read(msg_b);
        String message = new String(msg_b);
        System.out.println(name + ": " + message);
        Message recvMsg = new Message(uuid, message, name, false);
        return recvMsg;
    }



    int readLengthFromInput(ByteArrayInputStream bin) throws IOException {
        byte[] length_b = new byte[4];
        bin.read(length_b);
        return ByteBuffer.wrap(length_b).getInt();
    }


}
