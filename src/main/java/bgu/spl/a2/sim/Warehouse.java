package bgu.spl.a2.sim;

import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.Deferred;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class representing the warehouse in your simulation
 * 
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 */
public class Warehouse {

	private MyPair rsPliers;
	private MyPair gsDriver;
	private MyPair npHammer;

	private ConcurrentHashMap<String, ManufactoringPlan> mapForPlans;
	private ConcurrentLinkedQueue<Deferred<Tool>> hammerQueue;
	private ConcurrentLinkedQueue<Deferred<Tool>> gsDriverQueue;
	private ConcurrentLinkedQueue<Deferred<Tool>> rsPliersQueue;

	private final Lock hammerLOCK;
	private final Lock gsDriverLock;
	private final Lock rsPliersLOCK;

	public Warehouse() {
		rsPliers = new MyPair(new RandomSumPliers(), 0);
		gsDriver = new MyPair(new GcdScrewDriver(), 0);
		npHammer = new MyPair(new NextPrimeHammer(), 0);
		mapForPlans = new ConcurrentHashMap<String, ManufactoringPlan>();
		hammerQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();
		gsDriverQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();
		rsPliersQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();
		hammerLOCK = new ReentrantLock();
		gsDriverLock = new ReentrantLock();
		rsPliersLOCK = new ReentrantLock();
	}

	/**
	 * Tool acquisition procedure Note that this procedure is non-blocking and
	 * should return immediately
	 * 
	 * @param type
	 *            - string describing the required tool
	 * @return a deferred promise for the requested tool
	 */
	public  Deferred<Tool> acquireTool(String type) {
		Deferred<Tool> t = new Deferred<Tool>();
		if (type.equals("rs-pliers")) {
			rsPliersLOCK.lock();
			if (rsPliers.getNumberOfTools().get() > 0) {
				rsPliers.getNumberOfTools().decrementAndGet();
				t.resolve(rsPliers.getTool());
				rsPliersLOCK.unlock();
				return t;
			} else {
				rsPliersQueue.add(t);
				rsPliersLOCK.unlock();
				return t;
			}
		} else if (type.equals("np-hammer")) {
			hammerLOCK.lock();
			if (npHammer.getNumberOfTools().get() > 0) {
				npHammer.getNumberOfTools().decrementAndGet();
				t.resolve(npHammer.getTool());
				hammerLOCK.unlock();
				return t;
			} else {
				hammerQueue.add(t);
				hammerLOCK.unlock();
				return t;
			}
		} else
			gsDriverLock.lock();
		if (gsDriver.getNumberOfTools().get() > 0) {
			gsDriver.getNumberOfTools().decrementAndGet();
			t.resolve(gsDriver.getTool());
			gsDriverLock.unlock();
			return t;
		} else {
			gsDriverQueue.add(t);
			gsDriverLock.unlock();
			return t;
		}

	}

	/**
	 * Tool return procedure - releases a tool which becomes available in the
	 * warehouse upon completion.
	 * 
	 * @param tool
	 *            - The tool to be returned
	 */
	public synchronized void releaseTool(Tool tool) {

		if (tool.getType().equals("rs-pliers")) {
			if(rsPliersQueue.size()>0){
				rsPliersQueue.remove().resolve(rsPliers.getTool());
			}else {	rsPliers.setNumberOfTools(rsPliers.getNumberOfTools().incrementAndGet());}
				
		} else if (tool.getType().equals("np-hammer")) {
			if(hammerQueue.size()>0){
				hammerQueue.remove().resolve(npHammer.getTool());
			}else {	npHammer.setNumberOfTools(npHammer.getNumberOfTools().incrementAndGet());}
				
		} else
		{
			if(gsDriverQueue.size()>0){
				gsDriverQueue.remove().resolve(gsDriver.getTool());
			}else {	gsDriver.setNumberOfTools(gsDriver.getNumberOfTools().incrementAndGet());}	
		}
	}

	/**
	 * Getter for ManufactoringPlans
	 * 
	 * @param product
	 *            - a string with the product name for which a ManufactoringPlan
	 *            is desired
	 * @return A ManufactoringPlan for product
	 */
	public ManufactoringPlan getPlan(String product) {
		return mapForPlans.get(product);
	}

	/**
	 * Store a ManufactoringPlan in the warehouse for later retrieval
	 * 
	 * @param plan
	 *            - a ManufactoringPlan to be stored
	 */
	public void addPlan(ManufactoringPlan plan) {
		mapForPlans.putIfAbsent(plan.getProductName(), plan);
	}
	

	/**
	 * Store a qty Amount of tools of type tool in the warehouse for later
	 * retrieval
	 * 
	 * @param tool
	 *            - type of tool to be stored
	 * @param qty
	 *            - amount of tools of type tool to be stored
	 */
	public void addTool(Tool tool, int qty) {
		if (tool instanceof GcdScrewDriver) {
			gsDriver.setNumberOfTools(qty);
		}
		if (tool instanceof NextPrimeHammer) {
			npHammer.setNumberOfTools(qty);
		}
		if (tool instanceof RandomSumPliers) {
			rsPliers.setNumberOfTools(qty);
		}
	}
	public AtomicInteger getRsPliersQty(){
		return rsPliers.getNumberOfTools();
	}
	public AtomicInteger getNpHammerQty(){
		return npHammer.getNumberOfTools();
	}
	public AtomicInteger getGsDriverQty(){
		return gsDriver.getNumberOfTools();
	}

}
