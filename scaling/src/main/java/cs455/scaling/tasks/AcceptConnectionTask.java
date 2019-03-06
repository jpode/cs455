package cs455.scaling.tasks;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class AcceptConnectionTask implements Task{
	private Selector selector;
	private ServerSocketChannel server_channel;
	private SelectionKey key;
	
	public AcceptConnectionTask(SelectionKey key, Selector selector, ServerSocketChannel server_channel) {
		this.selector = selector;
		this.server_channel = server_channel;
		this.key = key;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		//Acquire mutex locks on selector and server channel objects to avoid race conditions when registering the new connection
		synchronized(selector){
			synchronized(server_channel) {
				System.out.println("\tTask::AcceptConnection: thread running and registering new connection");
				//Accept a new client socket, configure blocking mode, and register it for read operations
				SocketChannel client;
				try {
					client = server_channel.accept();
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ, key.attachment());
					((AtomicBoolean)key.attachment()).set(false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		
	}

}
