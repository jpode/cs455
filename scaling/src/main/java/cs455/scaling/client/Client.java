package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
	
	private String server_host;
	private int server_port;
	private long message_rate;
	private SocketChannel client;
	private SenderThread sender;
	private Thread thread;
	private ConcurrentLinkedQueue<String> hash_codes;
	
	public Client(String server_host, int server_port, long message_rate) {
		this.server_host = server_host;
		this.server_port = server_port;
		this.message_rate = message_rate;
		
		hash_codes = new ConcurrentLinkedQueue<String>();
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
		sender = new SenderThread(message_rate, client, hash_codes);
		thread = new Thread(sender);
		thread.start();
	}
	
	public void listen() {
		ByteBuffer buffer = ByteBuffer.allocate(256);
		while(true) {
			try {
				client.read(buffer);
				System.out.println("Server response: " + new String(buffer.array()).trim());
				buffer.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		 Client client = new Client("localhost", 5011, 5);
	}
}
