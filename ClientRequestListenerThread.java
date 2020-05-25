import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

class ClientRequestListenerThread implements Runnable {

    private Node node;

    public ClientRequestListenerThread(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        try {
            System.setProperty("javax.net.ssl.keyStore", "server.keys");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");
            System.setProperty("javax.net.ssl.trustStore", "truststore");
            System.setProperty("javax.net.ssl.trustStorePassword", "123456");
            
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket(node.getPort());      

            while (true) {
                
                SSLSocket socket = (SSLSocket) ss.accept();

                node.executor.execute(new HandlerRequest(node, socket));
            
            }
        } catch (IOException e) {
                e.printStackTrace();
        }
    }
}