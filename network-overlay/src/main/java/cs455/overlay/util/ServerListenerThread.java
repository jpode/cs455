package cs455.overlay.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.wireformats.Event;

public class ServerListenerThread implements Runnable{
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
	
	public ServerListenerThread() {
		receiver_pool = new ArrayList<TCPReceiverThread>();
		receiver_pool_queue = new ArrayList<TCPReceiverThread>();
		thread_pool = new ArrayList<Thread>();
		queue = new ConcurrentLinkedQueue<Event>();
	}
	
	//Takes a receiver and a thread to listen for messages from
	//Synchronized to avoid ConcurrentModificationExceptions
	public synchronized void addConnection(TCPReceiverThread rec, Thread thd) {
		//System.out.println("RegistryMessageThread: adding new connection");
		
		receiver_pool_queue.add(rec);
		thread_pool.add(thd);
		
	}
	
	public synchronized void addConnection(Socket socket) {
		TCPReceiverThread rec;
		try {
			rec = new TCPReceiverThread(socket);
			
			Thread thd = new Thread(rec);
			thd.start();
			
			receiver_pool_queue.add(rec);
			thread_pool.add(thd);
		} catch (IOException e) {
			//e.printStackTrace
		}

	}
	
	public synchronized void closeConnection(Socket socket) {
		for(int i = 0; i < receiver_pool.size(); i++) {
			if(receiver_pool.get(i).getSocket() == socket) {
				
				thread_pool.get(i).interrupt();
				thread_pool.remove(i);
				
				receiver_pool.get(i).killAll(); //Closes the socket
				receiver_pool.remove(i);
				

			}
		}
	}
	
	public synchronized void kill() { 
		running.set(false); //Stop the thread from running
		
		for(Thread thread : thread_pool) {
			thread.interrupt();
		}
		
		for(TCPReceiverThread receiver : receiver_pool) {
				receiver.kill();
				receiver_pool.remove(receiver);
		}
		//System.out.println("RegistryMessageThread::kill: thread killed successfully");
	}
	
	public synchronized void clear() {
		
		for(Thread thd : thread_pool) {
			thd.interrupt();
		}	
		
		for(TCPReceiverThread receiver : receiver_pool) {
			receiver.kill();
			receiver_pool.remove(receiver);
		}
		

	}
	
	//Synchronized to avoid ConcurrentModificationExceptions
	private synchronized void loadConnections(){
		if(!receiver_pool_queue.isEmpty()) {
			//System.out.println("RegistryMessageThread: loaded new connections");
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
			for(TCPReceiverThread receiver : receiver_pool) {
				try {
					new_event = receiver.get();
					if(new_event != null) {
						//System.out.println("RegistryMessageThread: new event received");
						queue.add(new_event); //If there is an Event to collect from one of the listening threads, add it to the queue. 
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}



}
