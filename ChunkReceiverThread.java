import java.io.DataInputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;

class ChunkReceiverThread implements Runnable {

    private Node peer;
    private DataInputStream dis;
    private SSLSocket server;

    /**
     * 
     * @param peer
     * @param server
     */
    public ChunkReceiverThread(Node peer, SSLSocket server) {
        this.peer = peer;
        this.server = server;
    }

    @Override
    public void run() {

        try {
            
            this.dis = new DataInputStream(server.getInputStream());            

            String key = dis.readUTF();
            String value = dis.readUTF();

            int length = dis.readInt(); // read length of incoming message
            if (length > 0) {
                byte[] chunk = new byte[length];
                dis.readFully(chunk, 0, chunk.length); // read the message

                peer.storeChunk(Long.parseLong(key), value, chunk);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

}