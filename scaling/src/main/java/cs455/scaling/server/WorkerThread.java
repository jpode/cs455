package cs455.scaling.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import cs455.scaling.tasks.Task;

public class WorkerThread extends Thread{
	private AtomicBoolean active;
	private LinkedBlockingQueue<Task> tasks;
	
	public WorkerThread() {
		tasks = new LinkedBlockingQueue<Task>();
		active = new AtomicBoolean();
		active.set(false);
	}

	public boolean isActive() {
		return active.get();
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				//System.out.println("Thread " + Thread.currentThread().getName() + " waiting for task...");
				tasks.take().run();
				System.out.println("\tThread " + Thread.currentThread().getName() + " finished given task");
				active.set(false);
	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void performTask(Task task) {
		tasks.add(task);
		active.set(true);
	}

}
