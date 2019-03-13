package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs455.scaling.hash.Hash;

public class Client {
	
	//Server hostname
	private String server_host;
	//Server port number
	private int server_port;
	//Number of messages to be send per second, specified by user
	private long message_rate;
	//Channel connected to the server to use for reading/writing messages
	private SocketChannel client;
	//Runnable that controls sending messages to the server, requires a specified message rate
	private SenderThread sender;
	//Thread to run the sender object
	private Thread sender_thread;
	//List of the hash codes for each message that has been sent to the server, items are removed upon receiving
	// a corresponding hash code from the server
	private ConcurrentLinkedQueue<String> hash_codes;
	//Runnable that computes and displays statistics over the last 20 seconds of operatoin
	private ClientStatistics stats;
	//Thread to run the statistics object
	private Thread stats_thread;
	
	public Client(String server_host, int server_port, long message_rate) {
		this.server_host = server_host;
		this.server_port = server_port;
		this.message_rate = message_rate;
		
		hash_codes = new ConcurrentLinkedQueue<String>();
		stats = new ClientStatistics();
		stats_thread = new Thread(stats);
		stats_thread.start();
	}
	
	//Attempt to create a SocketChannel connection to the server using the given hostname and port
	public void connect() {
		try {
			System.out.println("Client: Connecting to " + server_host + " on port " + server_port); 

			client = SocketChannel.open(new InetSocketAddress(server_host, server_port));
			
			if(client.isConnected()){
				System.out.println("Client: Connection successful");
			}

		} catch (IOException e) {
			System.out.println("Client: connection to server failed");
		}
	}
	
	//Start a separate thread for sending messages to the server
	public void startMessaging() {
		sender = new SenderThread(message_rate, client, hash_codes, stats);
		sender_thread = new Thread(sender);
		sender_thread.start();
	}
	
	//Continuously listens for and processes messages coming from the server
	public void listen() {
		ByteBuffer buffer = ByteBuffer.allocate(8000);
		Hash hash = new Hash();
		while(true) {
			try {
				int bytes_read = 0;
				while(bytes_read != 40) {
					bytes_read = client.read(buffer);
				}

				if(bytes_read == -1) {
					System.out.println("Connection to server lost, client exiting");
					sender_thread.interrupt();
					return;
				} else {
					//Only take the first 40 bytes of the message, as the same buffer is used to read and write and only 40 bytes are used in SH1
					byte[] response = new byte[40];
					for(int i = 0; i < 40; i++) {
						response[i] = buffer.array()[i];
					}
					
					buffer.clear();
					
					//If the received hash code matches one in the stored list of hash codes, update the statistics to display a mesage as correctly received
					if(hash_codes.remove(new String(response))) {
						stats.updateReceiveCount();
					}
				}
			} catch (IOException e) {
				System.out.println("Connection to server lost, client exiting...");
				sender_thread.interrupt();
				stats_thread.interrupt();
				return;
			}
		}
	}
	
	public static void main(String[] args){
		if(args.length != 3) {
			System.out.println("Incorrect number of arguments - 3 required");
			return;
		}
		
		Client client = new Client(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		client.connect();
		client.startMessaging();
		client.listen();
		
	}
}
