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
	
	private String server_host;
	private int server_port;
	private long message_rate;
	private SocketChannel client;
	private SenderThread sender;
	private Thread sender_thread;
	private ConcurrentLinkedQueue<String> hash_codes;
	private ClientStatistics stats;
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
	
	public void connect() {
		try {
			System.out.println("Client: Connecting to " + server_host + " on port " + server_port); 

			client = SocketChannel.open(new InetSocketAddress(server_host, server_port));
			
			if(client.isConnected()){
				System.out.println("Client: Connection successful");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startMessaging() {
		sender = new SenderThread(message_rate, client, hash_codes, stats);
		sender_thread = new Thread(sender);
		sender_thread.start();
	}
	
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
					
					//System.out.println("Server response: " + new String(response));
					if(hash_codes.remove(new String(response))) {
						stats.updateReceiveCount();
						System.out.println("Hash code removed from list");

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
	
	public int getNumRemainingHashCodes() {
		return hash_codes.size();
	}
	
	public static void main(String[] args){
		Client client = new Client("localhost", 5001, 1);
		client.connect();
		client.startMessaging();
		client.listen();
		
		System.out.println("Number of hashes that did not receive a response: " + client.getNumRemainingHashCodes());
	}
}
