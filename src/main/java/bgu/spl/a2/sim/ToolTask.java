package bgu.spl.a2.sim;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.Task;
import bgu.spl.a2.sim.tools.Tool;

public class ToolTask extends Task<Long> {

	Warehouse w;
	Product p;
	String s;
	
	public ToolTask(Warehouse w,Product p ,String s){
		this.w=w;
		this.p=p;
		this.s=s;
	}
	protected void start(){
		Deferred<Tool> t= w.acquireTool(s);
		t.whenResolved(()->{
			long ans= t.get().useOn(p);
			w.releaseTool(t.get());
			System.out.println(p.getParts().get(0).getFinalId()+" mmmmmmmm");
			System.out.println(ans+" my name: "+ s);
			complete(ans);
		});
	}
}
