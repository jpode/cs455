package cs455.overlay.util;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.wireformats.Event;

public class RegistryMessageThread implements Runnable{
	private ArrayList<TCPReceiverThread> thread_pool;
	private ConcurrentLinkedQueue<Event> queue;
	private final AtomicBoolean running = new AtomicBoolean(false);
	
	//Non blocking call
	public Event get() throws InterruptedException {
		return queue.poll();
	}
	
	public RegistryMessageThread() {
		thread_pool = new ArrayList<TCPReceiverThread>();
		queue = new ConcurrentLinkedQueue<Event>();
	}
	
	//Takes a socket and creates a new thread that will listen for messages from the socket
	public void addConnection(Socket new_socket) {
		try {
			thread_pool.add(new TCPReceiverThread(new_socket));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeConnection(Socket socket) {
		for(int i = 0; i < thread_pool.size(); i++) {
			if(thread_pool.get(i).getSocket() == socket) {
				thread_pool.get(i).kill();
				thread_pool.remove(i);
			}
		}
	}
	
	public void kill() { 
		running.set(false); //Stop the thread from running
		System.out.println("RegistryMessageThread::kill: thread killed successfully");
	}
	
	//The run method of this thread loops through the connection thread pool to see if any messages have been received
	public void run() {
		running.set(true);
		while(running.get()) {
			for(TCPReceiverThread thread : thread_pool) {
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
