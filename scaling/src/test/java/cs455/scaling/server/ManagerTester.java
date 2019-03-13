package cs455.scaling.server;

import cs455.scaling.tasks.TestTask;

public class ManagerTester {

	public static void main(String[] args) throws InterruptedException {
		ThreadPoolManager manager = new ThreadPoolManager(10, 10, 100);
		Thread manager_thread = new Thread(manager);
		manager_thread.start();
		
		int counter = 0;
		while(counter < 20) {
			Thread.currentThread().sleep(500);
			manager.execute(new TestTask((int)(Math.random() * 100)));
			counter++;
		}

	}
}
