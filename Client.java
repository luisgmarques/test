import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client {

    private static DataOutputStream dos;
    private static DataInputStream dis;

    public static void main(String[] args) {
        try {
            
            if (args.length != 5 && args.length != 6) {
                System.err.println("Wrong number of arguments");
                System.out.println("Usage: Client <ip> <port> <protocol> <filename> <rd>");
                return;
            }

            System.setProperty("javax.net.ssl.keyStore", "client.keys");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");
            System.setProperty("javax.net.ssl.trustStore", "truststore");
            System.setProperty("javax.net.ssl.trustStorePassword", "123456");

            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(args[0], Integer.parseInt(args[1]));
            
            socket.startHandshake();

            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());            

            switch (args[2]) {
                case "BACKUP":
                    dos.writeUTF(args[2]);
                    backup(args[3], Integer.parseInt(args[4]));
                    break;
                case "RESTORE":
                    dos.writeUTF(args[2]);
                    restore(args[3]);
                    break;
                case "DELETE":
                    dos.writeUTF(args[2]);
                    delete(args[3]);
                    break;
                case "RECLAIM":
                    dos.writeUTF(args[2]);
                    reclaim(Integer.parseInt(args[3]));
                    break;
                default:
                    dos.writeUTF("NOT VALID PROTOCOL");
                    break;
            }

        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void backup(String filename, int rd) {

        try {

            ArrayList<byte[]> chunks = Util.getChunks(filename);
            
            dos.writeInt(chunks.size());
            
            int i = 1; // chunk number;
            
            for (byte[] chunk : chunks) {
                String header_msg = filename + " " + (i++) + " " + rd + " \r\n\r\n";
                
                byte[] msg = Util.concatenateArrays(header_msg.getBytes(), chunk);
                
                dos.writeInt(msg.length);
                
                dos.write(msg);
                
                dos.flush();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private static void restore(String filename) {
        try {
            ArrayList<byte[]> file_chunks = new ArrayList<>();

            dos.writeUTF(filename);

            int num_chunks = dis.readInt();

            for (int i = 1; i <= num_chunks; i++) {
                int chunk_size = dis.readInt();
                if (chunk_size > 0) {
                    byte[] chunk = new byte[chunk_size];
                    dis.readFully(chunk, 0, chunk_size);
                    file_chunks.add(chunk);
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void reclaim(int spaceToReclaim) {
        try {

            dos.writeInt(spaceToReclaim);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void delete(String filename) {
        try {

            dos.writeUTF(filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}