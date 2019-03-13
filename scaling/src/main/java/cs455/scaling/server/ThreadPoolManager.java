package cs455.scaling.server;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Iterator;

import cs455.scaling.tasks.Task;

/*
 * The ThreadPoolManager (TPM) is responsible for creating, assigning tasks to, and pulling statistics from the thread pool.
 * The thread pool consists of WorkerThreads, with the pool size determined by the user.
 * 
 * TPM workflow:
 *  - Create the thread pool and statistics tracker 
 *  - Server will add Tasks to the queue through the execute() method
 *  - Assign new Tasks to the work queue, either by adding to an existing batch or creating a new one
 *  - Iterate through the thread pool, pulling statistics from each thread and 
 *      checking if there is a full batch or if the batch time has expired
 *  - Assign the batch to an inactive thread
 */
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
	}
	
	public void execute(Task task) {
		synchronized(work_queue){
			for(Task[] work_unit : work_queue) {
				for(int i = 0; i < work_unit.length; i++) {
					if(work_unit[i] == null) {
						work_unit[i] = task;
						return;
					}
				}
			}
			Task[] work_unit = new Task[batch_size];
			work_unit[0] = task;
			work_queue.add(work_unit);
		}
	}

	@Override
	public void run() {
		stats_thread.start();
		long start_time = System.currentTimeMillis();
		while(true) {
			Iterator<WorkerThread> itr = thread_pool.iterator();

			while(itr.hasNext()) {
				WorkerThread thread = itr.next();
			
				statistics.addClientThroughput(thread.pullStats());
				
				synchronized(work_queue) {
					if(!work_queue.isEmpty() && (work_queue.peek()[batch_size - 1] != null || System.currentTimeMillis() - start_time > batch_time * 1000)) {					
	
						if(!thread.isActive()) {
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
