package Nodes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class Message {
    private UUID uuid;
    private String message;
    private String senderName;
    private Boolean isAnswer;


    Message(UUID uuid, String message, String senderName, Boolean isAnswer){
        this.message = message;
        this.uuid = uuid;
        this.senderName = senderName;
        this.isAnswer = isAnswer;
    }

    public UUID getUUID(){
        return uuid;
    }

    public byte[] getByteMessage(Boolean received) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] code = {received? (byte)3 : (byte)2};
        bout.write(code);
        byte[] msbUUID = ByteBuffer.allocate(8).putLong(uuid.getMostSignificantBits()).array();
        byte[] lsbUUID = ByteBuffer.allocate(8).putLong(uuid.getLeastSignificantBits()).array();
        bout.write(msbUUID);
        bout.write(lsbUUID);
        int nameLen = senderName.length();
        bout.write(ByteBuffer.allocate(4).putInt(nameLen).array());
        bout.write(senderName.getBytes());
        int msgLen = message.length();
        bout.write(ByteBuffer.allocate(4).putInt(msgLen).array());
        bout.write(message.getBytes());
        bout.close();
        return bout.toByteArray();
    }

    public boolean isAnswer(){
        return isAnswer;
    }

    public String getName(){
        return senderName;
    }

    public String getMessage(){
        return message;
    }
}
