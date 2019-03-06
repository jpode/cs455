package cs455.scaling.tasks;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class BatchTask implements Task{

	private SelectionKey key;
	
	public BatchTask(SelectionKey key) {
		this.key = key;
	}
	
	
	@Override
	public void run() {
		//Read message from client to a buffer
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(256);
		try {
			int bytes_read = client.read(buffer);
			if(bytes_read == -1) { // Connection has closed
				client.close();
				System.out.println("Client disconnected");
			} else {
				System.out.println("Server: read message from buffer: " + new String(buffer.array()).trim());
				buffer.clear();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
