package cs455.scaling.tasks;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import cs455.scaling.hash.Hash;

/*
 * BatchTask is a Task object created when a clients sends a message to the server, and is considered part of a "work unit" that will be 
 * assigned to  a worker thread by the ThreadPoolManager when a work unit is full or the batch time has expired.
 * Task workflow:
 *  - Deregister the client channel
 *  - Read the message
 *  - Compute the message hash
 *  - Respond to the message
 *  - Reregister the channel
 */
public class BatchTask implements Task{

	private SocketChannel client;
	private final int client_id;
	private Selector server_selector;
	private Hash hash;
	
	public BatchTask(SelectionKey key, Selector server_selector) {
		client = (SocketChannel)key.channel();
		client_id = (int)key.attachment(); 
		
		//Deregister the channel to prevent the server selector from continually marking the key as having activity
		key.cancel();
		
		this.server_selector = server_selector;
		this.hash = new Hash();
		
	}
	
	@Override
	public int getId() {
		return client_id;
	}
	
	@Override
	public void run() {
		//Read message from client to a buffer
		ByteBuffer buffer = ByteBuffer.allocate(8000);

		try {
			
			int bytes_read = 0;
			
			//Message from the client may not be read all at once, so attempt to read until the buffer is full
			while(buffer.hasRemaining() && bytes_read != -1) {
				bytes_read = client.read(buffer);
			}
			
			if(bytes_read == -1) { //Connection has closed
				client.close();
				//System.out.println("Client disconnected");
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
			
			//Reregister the the client with selector and set interest to read
			synchronized(server_selector) {
				client.register(server_selector, SelectionKey.OP_READ, client_id);
			}
			
			return;
		}catch(Exception e) {
			//System.out.println("Client connection failure detected");
		}
	}
}
