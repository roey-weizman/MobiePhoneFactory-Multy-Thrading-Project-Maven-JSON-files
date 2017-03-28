package bgu.spl.a2.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.Task;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;

/**
 * a class that is a product trying to to get its final result(id).
 * 
 */
public class ManufactureTask extends Task<Product> {
	
	private Product myProduct;
	private ManufactoringPlan myPlan;
	private Warehouse MyWarehouse=new Warehouse();
	List<ManufactureTask> partsToBeComplete;
	AtomicInteger numberOfParts;
	AtomicInteger numberOfTools;
	AtomicLong sum;

	ManufactureTask(ManufactoringPlan Plan, Product p, Warehouse warehouse) {
		myPlan = Plan;
		myProduct = p;
		MyWarehouse = warehouse;
		partsToBeComplete = new ArrayList<ManufactureTask>();
		sum = new AtomicLong(myProduct.getStartId());
	}

	protected void start() {
		createParts();

		numberOfParts = new AtomicInteger(partsToBeComplete.size());
		numberOfTools = new AtomicInteger(myPlan.getTools().length);

		if (partsToBeComplete.isEmpty()) {//no parts for this products
			for (int i = 0; i < myPlan.getTools().length; i++) {
				String s = myPlan.getTools()[i];
				Tool t = createToolFromString(s);
				Deferred<Tool> d = MyWarehouse.acquireTool(s);
				d.whenResolved(() -> {
					myProduct.setFinalId(sum.get());
					MyWarehouse.releaseTool(t);

				});

			}
			complete(myProduct);

		} else {//there is at least 1 part
			for (int j = 0; j < partsToBeComplete.size(); j++) {
				partsToBeComplete.get(j).getResult().whenResolved(() -> {
					if (numberOfParts.decrementAndGet() == 0) {
						for (int i = 0; i < myPlan.getTools().length; i++) {
							String s = myPlan.getTools()[i];
							Tool t = createToolFromString(s);
							Deferred<Tool> d = MyWarehouse.acquireTool(s);
							d.whenResolved(() -> {
								sum.addAndGet(t.useOn(myProduct));
								MyWarehouse.releaseTool(t);
								if (numberOfTools.decrementAndGet() == 0) {
									myProduct.setFinalId(sum.get());
									complete(myProduct);
								}
							});
						}
						if (myPlan.getTools().length == 0)//the product have parts but no tools
							complete(myProduct);
					}
				});
			}
		}
	}

	Product getMyProduct() {
		return myProduct;
	}

	private static Tool createToolFromString(String s) {
		if (s.equals("rs-pliers"))
			return new RandomSumPliers();
		else if (s.equals("np-hammer"))
			return new NextPrimeHammer();
		else
			return new GcdScrewDriver();
	}

	private void createParts() {
		for (int i = 0; i < myPlan.getParts().length; i++) {
			String name = myPlan.getParts()[i];
			Product p = new Product(myProduct.getStartId() + 1, name);
			ManufactureTask pr = new ManufactureTask(MyWarehouse.getPlan(name), p, MyWarehouse);
			partsToBeComplete.add(pr);
			myProduct.addPart(p);
			spawn(pr);
		}

	}

}