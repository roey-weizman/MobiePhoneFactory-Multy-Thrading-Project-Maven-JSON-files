package bgu.spl.a2.sim;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import bgu.spl.a2.Task;

public class WaveTask extends Task<Long> {

	private Object[] MyWave;
	private Warehouse w;
	int sum;
	ArrayList <ManufactureTask> listofProdocts=new ArrayList <ManufactureTask>();
	WaveTask(Object[] MyWave, Warehouse w) {
		this.MyWave = MyWave;
		this.w = w;
		 sum=0;

	}

	protected void start() {
		for (int j = 1; j < MyWave.length; j =j + 3) {
			sum+=(int)MyWave[j];
		}
		
		CountDownLatch l=new CountDownLatch(sum);
		ConcurrentLinkedQueue<Product> myAns = Simulator.getAns();
		for (int i = 0; i < MyWave.length; i = i + 3) {
			int currQty = (int) MyWave[i + 1];
			for (int j = 0; j < currQty; j++) {
				Product me=new Product((long) MyWave[i + 2]+j, (String) MyWave[i]);
				ManufactureTask m = new ManufactureTask(w.getPlan((String) MyWave[i]),me, w);
				spawn(m);
				myAns.add(m.getMyProduct());
				m.getResult().whenResolved(()->{l.countDown();
				if(l.getCount()==0)
					complete((long)0);				
				});
				

			}
			
		}
	}
}
