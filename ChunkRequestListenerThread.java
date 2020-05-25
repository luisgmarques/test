import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;

class ChunkRequestListenerThread implements Runnable {

    private DataOutputStream dos;
    private DataInputStream dis;
    private Node node;

    /**
     * 
     * @param node
     * @param socket
     */
    public ChunkRequestListenerThread(Node node, SSLSocket socket) {
        this.node = node;

        try {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            String chunkID = dis.readUTF();
            byte[] chunk = node.getStorage().readChunk(Long.parseLong(chunkID));

            dos.writeInt(chunk.length);
            dos.write(chunk);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}