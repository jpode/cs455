package cs455.scaling.tasks;

public class TestTask implements Task{

	private int var;
	
	public TestTask(int var) {
		this.var = var;
	}
	
	@Override
	public void run() {
		System.out.println("TestTask " + var + " run successfully");
		
	}


}