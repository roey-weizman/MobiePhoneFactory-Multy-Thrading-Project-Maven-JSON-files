package bgu.spl.a2.sim;
import bgu.spl.a2.sim.tools.*;
import java.util.concurrent.atomic.AtomicInteger;
public class MyPair {
	
	private Tool tool;
	private AtomicInteger numberOfTools=new AtomicInteger(0);
	
	public MyPair(Tool tool,int numberOfTools){
		this.tool=tool;
		this.numberOfTools.set(numberOfTools);
	}
	
	public AtomicInteger getNumberOfTools(){
		return numberOfTools;
	}
	public void setNumberOfTools(int numberOfTools){
		this.numberOfTools.set(numberOfTools);
	}
	public Tool getTool(){
		return tool;
	}
}
