package Nodes;
import java.net.InetAddress;


public class Main {

    public static void main(String[] args) {
        Node node = null;
        Receiver receiver;
        Sender sender;
        Reader reader;

        InetAddress nodeAddr, neighbourAddr;
        int port, neighbourPort, lostPercent;
        String name;
        try {
            if (args.length != 3 && args.length != 5){
                throw new Exception();
            }
            name = args[0];
            port = Integer.parseInt(args[1]);
            lostPercent = Integer.parseInt(args[2]);
            if (args.length == 3) {
                node = new Node(name, port, lostPercent);
            } else {
                neighbourAddr = InetAddress.getByName(args[3]);
                neighbourPort = Integer.parseInt(args[4]);
                node = new Node(name, port, lostPercent, neighbourAddr, neighbourPort);
            }
            receiver = new Receiver(node);
            receiver.start();
            sender = new Sender(node);
            sender.start();
            reader = new Reader(node);
            reader.start();
        } catch (Exception e) {
            System.out.println("Wrong arguments");
        }
    }
}
