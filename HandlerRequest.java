import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;

public class HandlerRequest implements Runnable {

    private SSLSocket socket;
    private Node node;

    HandlerRequest(Node node, SSLSocket socket) {
        this.node = node;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());
      
            String request = dis.readUTF();
            String reply = "";
      
            switch (request) {
                case "BACKUP":
                    System.out.println("I received your msg");
                    reply = node.backup(dos, dis);
                    break;
                case "RESTORE":
                    //reply = node.restore(dos, dis);
                    break;
                case "DELETE":
                    //reply = node.delete(dos, dis);
                    break;
                case "RECLAIM":
                    //reply = node.reclaim(dos, dis);
                    break;
                default:
                    break;
            }
      
            System.out.println(reply);
            dos.writeUTF(reply);
      
            dis.close();
            dos.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}