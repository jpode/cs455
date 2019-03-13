package cs455.scaling.tasks;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import cs455.scaling.server.Statistics;

public class AcceptConnectionTask implements Task{
	private Selector selector;
	private ServerSocketChannel server_channel;
	private SelectionKey key;
	private final int client_id;
	
	public AcceptConnectionTask(SelectionKey key, Selector selector, ServerSocketChannel server_channel, int client_id) {
		this.selector = selector;
		this.server_channel = server_channel;
		this.key = key;
		this.client_id = client_id;
	}
	
	@Override
	public int getId() {
		return client_id;
	}
	
	@Override
	public void run() {
		
		//Acquire mutex locks on selector and server channel objects to avoid race conditions when registering the new connection
		synchronized(selector){
			synchronized(server_channel) {
				System.out.println("\t\t\t\tTask::AcceptConnection: thread running and registering new connection with id " + client_id);
				//Accept a new client socket, configure blocking mode, and register it for read operations
				SocketChannel client;
				try {
					client = server_channel.accept();
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ, client_id);
					((AtomicBoolean)key.attachment()).set(false);
				}catch(Exception e) {
					System.out.println("Client connection failure detected, cancelling channel key.");
					key.cancel();
				}
			}
		}
	}

}
