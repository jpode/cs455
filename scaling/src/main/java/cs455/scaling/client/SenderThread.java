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
	
	public SenderThread(long message_rate, SocketChannel client, ConcurrentLinkedQueue hash_codes) {
		this.message_rate = message_rate;
		this.client = client;
		this.hash_codes = hash_codes;
		
		hash = new Hash();
	}
	
	private byte[] generateMessage() {
		byte[] new_message = new byte[8000];
		new Random().nextBytes(new_message);
		return new_message;
	}

	@Override
	public void run() {
		long start_time = System.currentTimeMillis();
		
		while(true) {
			if((System.currentTimeMillis() - start_time) >= message_rate / 1000) {
				try {
					byte[] data = generateMessage();
					hash_codes.add(hash.SHA1FromBytes(data));
					ByteBuffer buffer = ByteBuffer.wrap(data);
					client.write(buffer);
					start_time = System.currentTimeMillis();
				} catch (IOException | NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
