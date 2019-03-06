package cs455.scaling.server;

import cs455.scaling.tasks.TestTask;

public class ManagerTester {

	public static void main(String[] args) {
		ThreadPoolManager manager = new ThreadPoolManager(10, 5, 5);
		Thread manager_thread = new Thread(manager);
		manager_thread.start();
		
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));
		manager.execute(new TestTask((int)(Math.random() * 100)));

	}
}
