package cs455.scaling.tasks;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import cs455.scaling.hash.Hash;
import cs455.scaling.server.Statistics;

public class BatchTask implements Task{

	private SocketChannel client;
	private final int client_id;
	private Selector server_selector;
	private Hash hash;
	
	public BatchTask(SelectionKey key, Selector server_selector) {
		client = (SocketChannel)key.channel();
		client_id = (int)key.attachment(); 
		key.cancel();
		
		this.server_selector = server_selector;
		//Take the key and only turn on write interest, preventing duplicate reads
		//this.key = key;
		//this.key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
		
		this.hash = new Hash();
		
	}
	
	@Override
	public int getId() {
		return client_id;
	}
	
	@Override
	public void run() {
		//Read message from client to a buffer
		//SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(8000);
		System.out.println("\t\t\t\tBatchTask: starting batch task for client " + client_id);

		try {
			
			int bytes_read = 0;
			
			while(buffer.hasRemaining() && bytes_read != -1) {
				//System.out.println("\t\t\t\t\tBatchTask: reading bytes...");
				bytes_read = client.read(buffer);
			}
			
			if(bytes_read == -1) { //Connection has closed
				client.close();
				System.out.println("Client disconnected");
			} else {
				
				String hash_code = hash.SHA1FromBytes(buffer.array());
				//Clear the buffer to put new data in
				buffer.clear();
				
				//Add the computed hash code to the buffer
				buffer.put(hash_code.getBytes());
				
				//Flip the buffer so it will write. Flip must be done instead of rewind because the hash code does not fill the buffer
				buffer.flip();
				
				//Write the buffer contents and clear it
				while(buffer.hasRemaining()) {
					client.write(buffer);
				}
				
				buffer.clear();
			}
			
			//Reset the attachment and interest on the key to continue listening for reads
			//((AtomicBoolean)key.attachment()).set(false);
			//key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			
			synchronized(server_selector) {
				client.register(server_selector, SelectionKey.OP_READ, client_id);
				//client.register(server_selector, SelectionKey.OP_READ);

			}
			
			System.out.println("\t\t\t\tBatchTask: task completed successfully");
			return;
		}catch(Exception e) {
			//e.printStackTrace();
			System.out.println("Client connection failure detected, cancelling channel key.");
			//key.cancel();
		}
		System.out.println("\t\t\t\tBatchTask: task NOT completed successfully");

	}
}
