import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class NodeThread implements Runnable {
	private Node node;

	/**
	 * 
	 * @param node
	 */
	NodeThread(Node node) {
		this.node = node;
	}

	/**
	 * 
	 * @param connection
	 * @param args
	 */
	public void findSuccessor(SSLSocket connection, String[] args) {
		Node successor = node.findSuccessor(Long.parseLong(args[1]));

		String response = "SUCCESSOR " + node.getId() + " ";

		if (successor == null)
			response += "NOTFOUND\n";

		else
			response += successor.ip + " " + successor.port + " \n";

		try {
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param connection
	 * @param args
	 */
	public void getPredecessor(SSLSocket connection, String[] args) {
		Node predecessor = node.getPredecessor();

		String response = "PREDECESSOR " + node.getId() + " ";

		if (predecessor == null)
			response += "NOTFOUND \n";

		else
			response += predecessor.ip + " " + predecessor.port + " \n";

		try {
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param connection
	 * @param args
	 */
	public void notify(SSLSocket connection, String[] args) {
		node.notify(new Node(args[2], Integer.parseInt(args[3])));

		try {
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());

			out.writeBytes("OK\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param connection
	 * @param args
	 */
	public void hi(SSLSocket connection, String[] args) {
		String response = "HI " + node.getId() + " \n";

		try {
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());

			out.writeBytes(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param connection
	 * @param args
	 */
	public void storeKey(SSLSocket connection, String[] args) {
		try {
			Keys.storeKey(node, Long.parseLong(args[1]), Long.parseLong(args[2]), args[3]);

			DataOutputStream out = new DataOutputStream(connection.getOutputStream());

			out.writeBytes("OK\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param connection
	 * @param args
	 */
	public void getKey(SSLSocket connection, String[] args) {
		String value = node.getKey(Long.parseLong(args[2]));

		String response = "KEY " + node.getId() + " " + value + " \n";

		try {
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param connection
	 * @param args
	 */
	public void deleteKey(SSLSocket connection, String[] args) {
		Keys.deleteKey(node, Long.parseLong(args[1]), Long.parseLong(args[2]));

		try {
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());

			out.writeBytes("OK\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param connection
	 * @param args
	 */
	public void deleteChunk(SSLSocket connection, String[] args) {
		node.deleteChunk(Long.parseLong(args[1]));

		try {
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());

			out.writeBytes("OK\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param connection
	 * @throws IOException
	 */
	public void interpretMessage(SSLSocket connection) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		String message = in.readLine().trim();
		System.out.println("[Node " + node.getId() + "] " + message);
		String[] args = message.split(" ");

		switch (args[0]) {
            case "FINDSUCCESSOR":
                findSuccessor(connection, args);
                break;
            case "GETPREDECESSOR":
                getPredecessor(connection, args);
                break;
            case "NOTIFY":
                notify(connection, args);
                break;
            case "HI":
                hi(connection, args);
                break;
            case "STOREKEY":
                storeKey(connection, args);
                break;
            case "GETKEY":
                getKey(connection, args);
                break;
            case "DELETEKEY":
                deleteKey(connection, args);
                break;
            case "DELETECHUNK":
                deleteChunk(connection, args);
                break;
            case "BACKUP":
                node.executor.execute(new ChunkReceiverThread(node, connection));
                break;
            case "RESTORE":
                node.executor.execute(new ChunkRequestListenerThread(node, connection));
                break;
		}
	}

	public void run() {
		try {
			SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			SSLServerSocket listenSocket = (SSLServerSocket) ssf.createServerSocket(node.getPort()+1);

			while (true) {
				SSLSocket connection = (SSLSocket) listenSocket.accept();
				node.executor.execute(new Runnable() {
					public void run() {
						try {
							interpretMessage(connection);
						} catch (IOException e) {
						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}