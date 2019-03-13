package cs455.scaling.server;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;


import cs455.scaling.tasks.Task;
import cs455.scaling.tasks.TestTask;

public class ThreadPoolManager implements Runnable{
	private ConcurrentLinkedQueue<WorkerThread> thread_pool;
	private ConcurrentLinkedQueue<Task[]> work_queue;
	private int batch_size;
	private int batch_time;
	private ServerStatistics statistics;
	private Thread stats_thread;
	
	public ThreadPoolManager(int num_threads, int batch_size, int batch_time) {
		thread_pool = new ConcurrentLinkedQueue<WorkerThread>();
		work_queue = new ConcurrentLinkedQueue<Task[]>();
		
		this.batch_size = batch_size;
		this.batch_time = batch_time;
		
		for(int i = 0; i < num_threads; i++) {
			WorkerThread worker = new WorkerThread();
			worker.start();
			thread_pool.add(worker);
		}
		
		statistics = new ServerStatistics();
		stats_thread = new Thread(statistics);
		stats_thread.start();
	}
	
	public void execute(Task task) {
		synchronized(work_queue){
			for(Task[] work_unit : work_queue) {
				for(int i = 0; i < work_unit.length; i++) {
					if(work_unit[i] == null) {
						//System.out.println("\tThreadPoolManager: adding task to work unit " + i);
						work_unit[i] = task;
						return;
					}
				}
			}
			//System.out.println("\tThreadPoolManager: creating new work unit for task");
			Task[] work_unit = new Task[batch_size];
			work_unit[0] = task;
			work_queue.add(work_unit);
		}
	}

	@Override
	public void run() {
		long start_time = System.currentTimeMillis();
		while(true) {
			Iterator<WorkerThread> itr = thread_pool.iterator();

			while(itr.hasNext()) {
				WorkerThread thread = itr.next();
			
				statistics.addClientThroughput(thread.pullStats());
				
				synchronized(work_queue) {
					if(!work_queue.isEmpty() && (work_queue.peek()[batch_size - 1] != null || System.currentTimeMillis() - start_time > batch_time * 1000)) {					
	
						if(!thread.isActive()) {
							System.out.println("\tManager: tasks in queue, assigning to inactive thread");
							thread.performTask(work_queue.poll());
							start_time = System.currentTimeMillis();
							break;
						}
					}
				}
			}
		}
		
	}
}
