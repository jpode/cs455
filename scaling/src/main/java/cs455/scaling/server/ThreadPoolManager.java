package cs455.scaling.server;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;


import cs455.scaling.tasks.Task;
import cs455.scaling.tasks.TestTask;

public class ThreadPoolManager implements Runnable{
	ConcurrentLinkedQueue<WorkerThread> thread_pool;
	ConcurrentLinkedQueue<Task> task_queue;
	
	public ThreadPoolManager(int num_threads, int batch_size, int batch_time) {
		thread_pool = new ConcurrentLinkedQueue<WorkerThread>();
		task_queue = new ConcurrentLinkedQueue<Task>();

		for(int i = 0; i < num_threads; i++) {
			WorkerThread worker = new WorkerThread();
			worker.start();
			thread_pool.add(worker);
		}
	}
	
	public void execute(Task task) {
		task_queue.add(task);
	}

	@Override
	public void run() {
		while(true) {
			if(!task_queue.isEmpty()) {
				System.out.println("Manager: tasks in queue, assigning...");
				Iterator<WorkerThread> itr = thread_pool.iterator();
				
				while(itr.hasNext()) {
					WorkerThread thread = itr.next();
					if(!thread.isActive()) {
						thread.performTask(task_queue.poll());
						break;
					}
				}
			}
		}
		
	}
}
