import java.io.DataOutputStream;
import java.io.IOException;

class ChunkSenderThread implements Runnable {
    private Node node;
    private DataOutputStream dos;
    private long key;
    private String value;
    private byte[] content;
    private boolean deleteAfter;
    
    /**
     * 
     * @param node
     * @param successor
     * @param key
     * @param value
     * @param content
     * @param deleteAfter
     */
    public ChunkSenderThread(Node node, Node successor, long key, String value, byte[] content, boolean deleteAfter){
        this.node = node;
        this.key = key;
        this.content = content;
        this.value = value;
        this.deleteAfter = deleteAfter;
    }

    @Override
    public void run() {
        try {
            dos.writeUTF("BACKUP\n");
            dos.writeUTF(Long.toString(key)); 
            dos.writeUTF(value); 
            dos.writeInt(content.length);
            dos.write(content);
            dos.flush();
            if(this.deleteAfter)
                node.deleteChunk(key);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}