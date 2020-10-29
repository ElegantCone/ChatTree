package Nodes;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Neighbour {
    private Integer nodePort;
    private String nodeName;
    private InetAddress nodeAddr;
    private Map<UUID, Long> cooldown;

    Neighbour(String name, InetAddress addr, int port){
        nodeName = name;
        nodeAddr = addr;
        nodePort = port;
        cooldown = new ConcurrentHashMap<UUID, Long>();
    }

    public Integer getPort(){
        return nodePort;
    }

    public InetAddress getAddress(){
        return nodeAddr;
    }

    public String getName() {
        return nodeName;
    }

    public long getCDbyUUID (UUID uuid){
        if (cooldown.get(uuid) == null) {
            cooldown.put(uuid, (long)0);
        }
        return cooldown.get(uuid);
    }

    public void setCDforMsg(UUID uuid){
        cooldown.put(uuid, System.currentTimeMillis());
    }
}
