import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Util {

    public final static int MAX_CHUNK_SIZE = 64000;

    public static ArrayList<byte[]> getChunks(String filename) {

        ArrayList<byte[]> chunks = new ArrayList<>();

        FileInputStream fis = null;

        try {
            fis = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FileChannel fileChannel = fis.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_CHUNK_SIZE);
        int bytesAmount = 0;

        try {
            while ((bytesAmount = fileChannel.read(byteBuffer)) > 0) {
                byte[] chunk = new byte[bytesAmount];
                byteBuffer.flip();
                byteBuffer.get(chunk);
                byteBuffer.clear();

                chunks.add(chunk);
            }

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        chunks.trimToSize();

        return chunks;
    }

    public static byte[] concatenateArrays(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);

        return c;
    }

    public static byte[] getChunkContent(byte[] message, int length) {

        byte[] chunkContent = new byte[MAX_CHUNK_SIZE];

        for (int i = 0; i < message.length; i++) {
            if ((int) message[i] == 13 && (int) message[i + 1] == 10 && (int) message[i + 2] == 13
                    && (int) message[i + 3] == 10) {
                chunkContent = new byte[length - i - 4];
                System.arraycopy(message, i + 4, chunkContent, 0, length - i - 4);
                break;
            }
        }

        return chunkContent;
    }

    public static String[] getHeader(byte[] message) {

        byte[] header = new byte[1000];

        for (int i = 0; i < message.length; i++) {
            if ((int) message[i] == 13 && (int) message[i + 1] == 10 && (int) message[i + 2] == 13
                    && (int) message[i + 3] == 10) {
                header = new byte[i];
                System.arraycopy(message, 0, header, 0, i);
                break;
            }
        }

        return new String(header, StandardCharsets.US_ASCII).split("\\s+");
    }

    public static void restoreFile(ArrayList<byte[]> file_chunks, String filename) {
        try {

            FileOutputStream fos = new FileOutputStream(filename);
            FileChannel file_channel = fos.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_CHUNK_SIZE);

            for (byte[] chunk : file_chunks) {
                byteBuffer.clear();
                byteBuffer.put(chunk);
                byteBuffer.flip();
                file_channel.write(byteBuffer);
            }

            fos.close();
            file_channel.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long getSHA1(String id) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] by = md.digest(id.getBytes());
            ByteBuffer buff = ByteBuffer.wrap(by);
            return buff.getLong();
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return 0;
        }
    }
}