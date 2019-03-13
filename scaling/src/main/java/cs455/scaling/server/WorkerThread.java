package cs455.scaling.server;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs455.scaling.tasks.Task;
/*
 * WorkerThread is a Thread object that exists in a thread pool managed by the ThreadPoolManager (TPM) class.
 * WorkerThread workflow:
 *  - Once started, block and wait for new tasks to be added to the queue by the TPM 
 *  - Run the Task by invoking its run() method
 *  - If completed successfully, add the client id associated with the task to the list of processed clients
 *  - When requested, provide the list of clients with processed messages and then clear the list
 */
public class WorkerThread extends Thread{
	private AtomicBoolean active;
	private LinkedBlockingQueue<Task> tasks;
	private ArrayList<Integer> processed_clients;
	public WorkerThread() {
		active = new AtomicBoolean();
		active.set(false);
		tasks = new LinkedBlockingQueue<Task>();
		processed_clients = new ArrayList<Integer>();
	}

	public boolean isActive() {
		return active.get();
	}
	
	public void performTask(Task[] work_unit) {
		int debug_counter = 0;
		for(Task task : work_unit) {
			if(task != null) {
				tasks.add(task);
				debug_counter++;
			}
		}
	}

	public synchronized Integer[] pullStats() {
		Integer[] result = processed_clients.toArray(new Integer[0]);
		processed_clients.clear();
		return result;
	}
	
	private synchronized void addClientToStats(int id) {
		processed_clients.add(id);
	}

	
	@Override
	public void run() {
		while(true) {
			try {
				Task task = tasks.take();
				active.set(true);
				task.run();
				addClientToStats(task.getId());
				active.set(false);
	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


}
