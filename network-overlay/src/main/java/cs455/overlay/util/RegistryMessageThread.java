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
	private ArrayList<Thread> thread_pool;
	private ConcurrentLinkedQueue<Event> queue;
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	//Non blocking call
	public Event get() throws InterruptedException {
		return queue.poll();
	}
	
	public RegistryMessageThread() {
		receiver_pool = new ArrayList<TCPReceiverThread>();
		thread_pool = new ArrayList<Thread>();
		queue = new ConcurrentLinkedQueue<Event>();
	}
	
	//Takes a socket and creates a new thread that will listen for messages from the socket
	public void addConnection(Socket new_socket) {
		try {
			TCPReceiverThread new_receiver = new TCPReceiverThread(new_socket);
			receiver_pool.add(new_receiver);
			
			Thread new_thread = new Thread(new_receiver);
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
	
	//The run method of this thread loops through the connection thread pool to see if any messages have been received
	public void run() {
		System.out.println("Message listener thread started");
		running.set(true);
		while(running.get()) {
			for(TCPReceiverThread thread : receiver_pool) {
				try {
					if(thread.get() != null) {
						queue.add(thread.get()); //If there is an Event to collect from one of the listening threads, add it to the queue. 
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
