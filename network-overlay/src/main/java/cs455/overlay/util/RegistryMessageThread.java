package cs455.overlay.util;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs455.overlay.transport.TCPReceiverThread;
import cs455.overlay.wireformats.Event;

public class RegistryMessageThread implements Runnable{
	private ArrayList<TCPReceiverThread> thread_pool;
	private ConcurrentLinkedQueue<Event> queue;
	private boolean run_flag;

	//Non blocking call
	public Event get() throws InterruptedException {
		return queue.poll();
	}
	
	public RegistryMessageThread() {
		thread_pool = new ArrayList<TCPReceiverThread>();
		queue = new ConcurrentLinkedQueue<Event>();
		run_flag = true;
	}
	
	//Takes a socket and creates a new thread that will listen for messages from the socket
	public void addConnection(Socket new_socket) {
		try {
			thread_pool.add(new TCPReceiverThread(new_socket));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeConnection(TCPReceiverThread new_thread) {
		thread_pool.get(thread_pool.indexOf(new_thread)).kill();
		thread_pool.remove(thread_pool.indexOf(new_thread));
	}
	
	//The run method of this thread loops through the connection thread pool to see if any messages have been received
	public void run() {
		while(run_flag) {
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
