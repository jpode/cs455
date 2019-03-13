package cs455.scaling.tasks;

import cs455.scaling.server.Statistics;

public interface Task extends Runnable{

	public void run();

	public int getId();
}
