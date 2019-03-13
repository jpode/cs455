package cs455.scaling.server;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs455.scaling.tasks.Task;

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
		
		//System.out.println("\t\tWorkerThread: added " + debug_counter + " tasks to queue ");
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
				//System.out.println("Thread " + Thread.currentThread().getName() + " waiting for task...");
				Task task = tasks.take();
				active.set(true);
				//System.out.println("\t\tThread " + Thread.currentThread().getName() + " active, " + tasks.size() + " tasks remaining in queue");
				task.run();
				addClientToStats(task.getId());
				//System.out.println("\t\tThread " + Thread.currentThread().getName() + " finished given task");
				active.set(false);
	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


}
