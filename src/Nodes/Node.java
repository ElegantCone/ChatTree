package Nodes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Node {
    private Integer nodeLossPercent;
    private Integer nodePort;
    private String nodeName;
    private DatagramSocket nodeSocket;
    private Set<Neighbour> neighList;
    private Map<UUID, Message> sentMsg;
    private Map<UUID, Set<Neighbour>> sendList;
    private Neighbour savepoint = null;
    private Boolean haveSavepoint = false;

    Node(String name, int port, int lossPercent) throws SocketException, UnknownHostException {
        init(name, port, lossPercent);
        InetAddress laddr = InetAddress.getLocalHost();
        this.nodeSocket = new DatagramSocket(port, InetAddress.getLocalHost());
        System.out.println("Head created! Address: " + nodeSocket.getLocalSocketAddress() + ". Your name: " + nodeName);
    }

    Node(String name, int nodePort, int lossPercent, InetAddress neighAddr, int neighPort) throws IOException {
        init(name, nodePort, lossPercent);
        this.nodeSocket = new DatagramSocket(nodePort, neighAddr);
        connect(neighAddr, neighPort,false);
        System.out.println("Node created! Address: " + nodeSocket.getLocalSocketAddress() + ". Your name: " + nodeName);
    }

    private void init(String name, int port, int lossPercent){
        this.nodeName = name;
        this.nodePort = port;
        this.nodeLossPercent = lossPercent;
        this.neighList = Collections.newSetFromMap(new ConcurrentHashMap<Neighbour, Boolean>());
        sendList = new ConcurrentHashMap<UUID, Set<Neighbour>>();
        sentMsg = new ConcurrentHashMap<UUID, Message>();
    }

    public void createSavepoint(Neighbour neighbour){
        if (haveSavepoint) return;
        savepoint = neighbour;
        haveSavepoint = true;
    }

    public void addNeighbour(Neighbour neighbour){
        neighList.add(neighbour);
        if (savepoint == null){
            createSavepoint(neighbour);
        }
    }

    //if connected - 1, else - 0
    public void connect(InetAddress addr, int neighPort, boolean connected) throws IOException {
        DatagramPacket packet;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] code = {connected? (byte)1 : (byte)0};
        bout.write(code);
        byte[] name = nodeName.getBytes();
        int nameLen = name.length;
        bout.write(ByteBuffer.allocate(4).putInt(nameLen).array());
        bout.write(name);
        sendSavepoint(bout);
        byte[] message = bout.toByteArray();
        bout.close();
        packet = new DatagramPacket(message, message.length, addr, neighPort);
        nodeSocket.send(packet);
    }

    public DatagramSocket getSocket(){
        return this.nodeSocket;
    }

    public String getName(){
        return nodeName;
    }

    public int getPort(){
        return nodePort;
    }

    private void sendSavepoint(ByteArrayOutputStream bout) throws IOException {
        if (savepoint == null) return;

        InetAddress saveAddr = savepoint.getAddress();
        Integer savePort = savepoint.getPort();
        String saveName = savepoint.getName();
        byte[] address = saveAddr.toString().getBytes();
        int addressLen = address.length;
        bout.write(ByteBuffer.allocate(4).putInt(addressLen).array());
        bout.write(address);
        byte[] port = ByteBuffer.allocate(4).putInt(savePort).array();
        int portLen = port.length;
        bout.write(ByteBuffer.allocate(4).putInt(portLen).array());
        bout.write(port);
        byte[] name = saveName.getBytes();
        int nameLen = name.length;
        bout.write(ByteBuffer.allocate(4).putInt(nameLen).array());
        bout.write(name);
    }

    public Set<Neighbour> getNeighList(){
        return neighList;
    }

    public void addMsgToQueue(Message msg, Neighbour sender){
        sentMsg.put(msg.getUUID(), msg);
        Set<Neighbour> neighbours = Collections.newSetFromMap(new ConcurrentHashMap<Neighbour, Boolean>());
        for (Neighbour neighbour : neighList){
            if (neighbour.equals(sender)) continue;
            neighbours.add(neighbour);
        }
        sendList.put(msg.getUUID(), neighbours);
    }

    public void addAnswerToQueue(Message msg, Neighbour receiver){
        sentMsg.put(msg.getUUID(), msg);
        Set<Neighbour> neighbours = Collections.newSetFromMap(new ConcurrentHashMap<Neighbour, Boolean>());
        neighbours.add(receiver);
        sendList.put(msg.getUUID(), neighbours);
    }

    public Map<UUID, Set<Neighbour>> getQueue(){
        return sendList;
    }


    public Message getMsgByUUID(UUID uuid){
        return sentMsg.get(uuid);
    }

    public Neighbour getNeighbour(int port){
        for (Neighbour neighbour : neighList){
            if (neighbour.getPort() == port) return neighbour;
        }
        return null;
    }

    public void delNeighFromSendList(UUID uuid, Neighbour neighbour){
        sendList.get(uuid).remove(neighbour);
    }

    public void delMsgFromQueue(UUID uuid){
        sentMsg.remove(uuid);
        sendList.remove(uuid);
    }

    public int getLossPersent(){
        return nodeLossPercent;
    }

    public void disconnect(Neighbour neighbour){
        neighList.remove(neighbour);
        for (UUID uuid : sendList.keySet()){
            for (Neighbour neigh : sendList.get(uuid)){
                if (neigh.equals(neighbour)) sendList.get(uuid).remove(neigh);
            }
        }
    }
}
