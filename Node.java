import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Node {
    public ScheduledExecutorService executor;
    private Node predecessor;
    private Node[] fingerTable;
    private long id;
    public String ip;
    public ConcurrentHashMap<Long, String> keys;
    public Storage storage;
    public int port;

    public Node(String ip, int port) {
        this.port = port;
        this.ip = ip;
        String id = ip + Integer.toString(port);
        this.id = Util.getSHA1(id);

        this.storage = new Storage(Long.toString(this.id));

        this.predecessor = null;

        keys = new ConcurrentHashMap<>();

        Keys.loadKeys(this);

        fingerTable = new Node[16];
        for (int i = 0; i < fingerTable.length; i++)
            fingerTable[i] = this;

        executor = Executors.newScheduledThreadPool(20);
        executor.execute(new NodeThread(this));
        executor.scheduleAtFixedRate(new Stabilize(this), 15, 15, TimeUnit.SECONDS);

    }

    public int getPort() {
        return port;
    }

    public long getId() {
        return id;
    }

    public Node getSuccessor() {
        return this.fingerTable[0];
    }

    public Node getPredecessor() {
        return predecessor;
    }

    public ConcurrentHashMap<Long, String> getKeys() {
        return keys;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setSuccessor(Node node) {
        this.fingerTable[0] = node;
    }

    public void setKeys(ConcurrentHashMap<Long, String> keys) {
        this.keys = keys;
    }

    public static boolean between(long lhs, long id, long rhs) {
        return (id > lhs && id < rhs);
    }

    public void join(Node ringNode) throws IOException {
        this.fingerTable[0] = ringNode.findSuccessor(this.id);
        this.predecessor = this.fingerTable[0];

        for (int i = 0; i < fingerTable.length; i++) {
            if (between(this.id, calculateFinger(i), this.getSuccessor().id))
                fingerTable[i] = this.getSuccessor();

            else
                fingerTable[i] = this;
        }
    }

    public Node findSuccessor(long id) {
        if (id == this.id)
            return this;

        if (between(this.id, id, this.getSuccessor().id))
            return this.getSuccessor();

        Node n0 = closestPrecedingNode(id);

        if (n0.getId() == (this.id))
            return this;

        return n0.findSuccessor(id);
    }

    public long calculateFinger(int i) {
        return (long) ((this.id + Math.pow(2, i)) % Math.pow(2, fingerTable.length));
    }

    public Node closestPrecedingNode(long id) {
        for (int i = fingerTable.length - 1; i >= 0; i--)
            if (fingerTable[i] != null && between(this.id, fingerTable[i].id, id))
                return fingerTable[i];

        return this;
    }

    public void stabilize() {
        Node node = this.getSuccessor().getPredecessor();

        if (node != null && this.id != (node.getId())
                && (this.id == this.getSuccessor().id || between(this.id, node.getId(), this.getSuccessor().id)))
            this.setSuccessor(node);

        this.getSuccessor().notify(this);
    }

    public void notify(Node node) {
        if (this.predecessor == null || this.predecessor.id != this.id
                || between(this.predecessor.id, node.id, this.id)) {
            if (node.getId() == this.id)
                return;

            if (this.predecessor == null || node.getId() != this.predecessor.id) {
                this.predecessor = node;

                Keys.giveKeys(this, this.predecessor, Keys.computeKeys(this, this.predecessor.getId()));

                if (this.getSuccessor().id == this.id) {
                    this.setSuccessor(node);
                }
            }
        }
    }

    public void fixFingers() {
        for (int i = 1; i < fingerTable.length; i++) {
            long fingerID = calculateFinger(i);
            fingerTable[i] = findSuccessor(fingerID);
        }
    }

    public void checkPredecessor() {
        if (this.predecessor != null && this.predecessor.failed(this.id))
            this.predecessor = null;
    }

    private boolean failed(long id) {
        return false;
    }

    public void deleteChunk(long key) {
        keys.remove(key);
        Keys.saveKeys(this);
        storage.delete(key);
    }

    public void storeChunk(long key, String value, byte[] chunk) {
    }

    public String getKey(long key) {
        return this.keys.get(key);
    }

    /**
     * 
     * public static void main(String[] args) { try {
     * 
     * 
     * 
     * while(true) { SSLSocket socket = (SSLSocket) serverSocket.accept();
     * 
     * DataInputStream dis = new DataInputStream(socket.getInputStream());
     * 
     * if (dis.readUTF().trim().equals("NOT VALID PROTOCOL")) {
     * System.out.println("NOT VALID PROTOCOL"); continue; }
     * 
     * String protocol = dis.readUTF().trim();
     * 
     * if (protocol.equals("BACKUP")) {
     * 
     * int num_chunks = dis.readInt();
     * 
     * int msg_size = 0; ArrayList<byte[]> file_chunks = new ArrayList<>();
     * 
     * 
     * 
     * for (int i = 1 ; i <= num_chunks; i++) {
     * 
     * msg_size = dis.readInt(); System.out.println(protocol);
     * System.out.println(msg_size);
     * 
     * byte[] msg = new byte[msg_size];
     * 
     * dis.readFully(msg, 0, msg_size);
     * 
     * String[] msg_header = Util.getHeader(msg); byte[] chunk_content =
     * Util.getChunkContent(msg, msg_size); file_chunks.add(chunk_content);
     * System.out.println(Arrays.toString(msg_header));
     * 
     * }
     * 
     * for (byte[] chunk : file_chunks) { System.out.println(chunk.length); } } }
     * 
     * 
     * } catch (NumberFormatException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated catch
     * block e.printStackTrace(); } }
     * 
     * @Override public void run() { System.setProperty("javax.net.ssl.keyStore",
     *           "server.keys");
     *           System.setProperty("javax.net.ssl.keyStorePassword", "123456");
     *           System.setProperty("javax.net.ssl.trustStore", "truststore");
     *           System.setProperty("javax.net.ssl.trustStorePassword", "123456");
     * 
     *           SSLServerSocketFactory factory = (SSLServerSocketFactory)
     *           SSLServerSocketFactory.getDefault(); SSLServerSocket serverSocket =
     *           (SSLServerSocket)
     *           factory.createServerSocket(Integer.parseInt(args[0]));
     * 
     *           serverSocket.setNeedClientAuth(true);
     * 
     *           }
     */

    public String backup(DataOutputStream out, DataInputStream in) throws IOException {
        int chunks = in.readInt();

        for (int i = 0; i < chunks; i++) {
            int length = in.readInt();
            byte[] message = new byte[length];
            in.readFully(message, 0, message.length); // read the message

            String[] header = Util.getHeader(message);
            byte[] content = Util.getChunkContent(message, length);

            System.out.println(Arrays.toString(header));
        
    
            if (i == 0) {
                System.out.println("Hello");
                String key = header[0];
                long encrypted = Util.getSHA1(key);
                System.out.println(encrypted);
                Node successor = this.findSuccessor(encrypted);
                System.out.println("Successor found");
                if (successor.id == this.id) {
                    Keys.storeKey(this, this.id, encrypted, chunks + ":" + header[1]);
                } else {
                    Keys.storeKey(successor, this.id, encrypted, chunks + ":" + header[1]);
                }
                System.out.println("End");
            }
            for (int j = 0; j < Integer.parseInt(header[2]); j++) {
                System.out.println("Saving chunk");
                String key = header[0] + "-" + header[1] + "-" + j;
                long encrypted = Util.getSHA1(key);
        
                Node successor = this.findSuccessor(encrypted);
        
                if (successor.id == this.id) {
                    System.out.println("Succ equals node");
                    storeChunk(encrypted, key, content);
                } else {
                    System.out.println("Succ not equelas node");
                    executor.execute(new ChunkSenderThread(this, successor, encrypted, key, content, false));
                }
            }
        }
        return "BACKED UP";
    }





    public void printNeighbors () {
		System.out.println("\nYou are listening on port "+this.getPort()+".");
		Node successor = fingerTable[0];
		
		// if it cannot find both predecessor and successor
		if ((predecessor == null  && (successor == null || successor.equals(this)))) {
			System.out.println("Your predecessor is yourself.");
			System.out.println("Your successor is yourself.");

		}
		
		// else, it can find either predecessor or successor
		else {
			if (predecessor != null) {
				System.out.println("Your predecessor is node "+predecessor.getId());
			}
			else {
				System.out.println("Your predecessor is updating.");
			}

			if (successor != null) {
				System.out.println("Your successor is node "+successor.getId());
			}
			else {
				System.out.println("Your successor is updating.");
			}
		}
	}
	
}