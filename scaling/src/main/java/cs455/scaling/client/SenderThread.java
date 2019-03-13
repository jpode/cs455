package cs455.scaling.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs455.scaling.hash.Hash;

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
		System.out.println("Messsage sender thread started");
		int debug_counter = 0;
		while(debug_counter < 10) {
			try {
				Thread.currentThread().sleep(1000 / message_rate);
				//Thread.currentThread().sleep(3000);
				byte[] data = generateMessage();
				//System.out.println("Attempting to send new message with hash code: " + hash.SHA1FromBytes(data));

				hash_codes.add(hash.SHA1FromBytes(data));
				ByteBuffer buffer = ByteBuffer.wrap(data);
				
				while(buffer.hasRemaining()) {
					client.write(buffer);
				}
				
				stats.updateSendCount();
				
				debug_counter++;
			} catch (IOException | NoSuchAlgorithmException e) {
				return;
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}
