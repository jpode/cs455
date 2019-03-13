package cs455.scaling.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs455.scaling.hash.Hash;

//SenderThread is a separate execution thread that continuously sends messages to the server at the specified rate, and 
// keeps track of the sent messages by adding their hash to a linked list
public class SenderThread implements Runnable {
	private long message_rate;
	private SocketChannel client;
	private ConcurrentLinkedQueue<String> hash_codes;
	private Hash hash;
	private ClientStatistics stats;
	
	public SenderThread(long message_rate, SocketChannel client, ConcurrentLinkedQueue hash_codes, ClientStatistics stats) {
		this.message_rate = message_rate;
		this.client = client;
		this.hash_codes = hash_codes;
		this.stats = stats;
		
		hash = new Hash();
	}
	
	private byte[] generateMessage() {
		byte[] new_message = new byte[8000];
		new Random().nextBytes(new_message);
		return new_message;
	}

	@Override
	public void run() {
		//Continuously send messages to the server at the specified rate 
		System.out.println("Messsage sender thread started");
		while(true) {
			try {
				Thread.currentThread().sleep(1000 / message_rate);
				
				//Generate 8KB of random data
				byte[] data = generateMessage();

				//Compute the hash of the data
				hash_codes.add(hash.SHA1FromBytes(data));
				
				//Attempt to write, which may take multiple attempts
				ByteBuffer buffer = ByteBuffer.wrap(data);
				
				while(buffer.hasRemaining()) {
					client.write(buffer);
				}
				
				//Update the number of messages sent
				stats.updateSendCount();
				
			} catch (IOException | NoSuchAlgorithmException e) {
				return;
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}
