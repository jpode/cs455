package cs455.overlay.util;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.wireformats.Event;

public class RegistryMessageThread implements Runnable{
	private ArrayList<TCPReceiverThread> receiver_pool;
	//This is a queue that new connections are added to before being added to the main pool
	//This prevents ConcurrentModificationExceptions, as otherwise new connections could be added while the array is being iterated through
	private ArrayList<TCPReceiverThread> receiver_pool_queue;
	private ArrayList<Thread> thread_pool;
	private ConcurrentLinkedQueue<Event> queue;
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	//Non blocking call
	public Event get() throws InterruptedException {
		return queue.poll();
	}
	
	public RegistryMessageThread() {
		receiver_pool = new ArrayList<TCPReceiverThread>();
		receiver_pool_queue = new ArrayList<TCPReceiverThread>();
		thread_pool = new ArrayList<Thread>();
		queue = new ConcurrentLinkedQueue<Event>();
	}
	
	//Takes a socket and creates a new thread that will listen for messages from the socket
	//Synchronized to avoid ConcurrentModificationExceptions
	public synchronized void addConnection(Socket new_socket) {
		try {
			TCPReceiverThread new_receiver = new TCPReceiverThread(new_socket);
			receiver_pool_queue.add(new_receiver);
			
			Thread new_thread = new Thread(new_receiver);
			new_thread.start();
			
			thread_pool.add(new_thread);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeConnection(Socket socket) {
		for(int i = 0; i < receiver_pool.size(); i++) {
			if(receiver_pool.get(i).getSocket() == socket) {
				receiver_pool.get(i).kill();
				receiver_pool.remove(i);
			}
		}
	}
	
	public void kill() { 
		running.set(false); //Stop the thread from running
		
		for(TCPReceiverThread receiver : receiver_pool) {
				receiver.kill();
				receiver_pool.remove(receiver);
		}
		
		for(Thread thread : thread_pool) {
			thread.interrupt();
		}
		System.out.println("RegistryMessageThread::kill: thread killed successfully");
	}
	
	//Synchronized to avoid ConcurrentModificationExceptions
	private synchronized void loadConnections(){
		if(!receiver_pool_queue.isEmpty()) {
			receiver_pool.addAll(receiver_pool_queue);
			receiver_pool_queue.clear();
		}
	}
	
	//The run method of this thread loops through the connection thread pool to see if any messages have been received
	public void run() {
		running.set(true);
		Event new_event;
		while(running.get()) {
			//Add any new connections to the pool
			loadConnections();
			
			//Loop through pool and check for new events
			for(TCPReceiverThread thread : receiver_pool) {
				try {
					new_event = thread.get();
					if(new_event != null) {
						System.out.println("RegistryMessageThread: new event received");
						queue.add(new_event); //If there is an Event to collect from one of the listening threads, add it to the queue. 
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
