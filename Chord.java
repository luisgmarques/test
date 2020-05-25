import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Chord {
    private static Node node;
    private static String ip;

    public static void main(String[] args) {
        if (args.length != 1 && args.length != 2) {
            System.err.println("Wrong number of arguments");
            System.out.println("To start the chord ring:");
            System.out.println("\t java Chord <port>");
            System.out.println("To join the chord ring:");
            System.out.println("\t java Chord <port> <ip> <port>");
            return;
        }

        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }

        
            node = new Node(ip, Integer.parseInt(args[0]));
            node.executor.execute(new ClientRequestListenerThread(node));
        

        if (args.length == 2) {
            try {
                node.join(node);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}