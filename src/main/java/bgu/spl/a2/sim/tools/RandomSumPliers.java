package bgu.spl.a2.sim.tools;

import java.util.Random;
import bgu.spl.a2.sim.Product;
import java.lang.Math;

public class RandomSumPliers implements Tool {
	/**
	 * @return tool name as string
	 */
	public String getType() {
		return "rs-pliers";
	}

	/**
	 * Tool use method
	 * 
	 * @param p
	 *            - Product to use tool on
	 * @return a long describing the result of tool use on Product package
	 */
	public long useOn(Product p) {
		long value = 0;
		for (int i = 0; i < p.getParts().size(); i++) {
			value += Math.abs(func(p.getParts().get(i).getFinalId()));
		}
		return value;
	}

	public long func(long startId) {
		long sum = 0;
		Random myRandom = new Random(startId);
		long theNumberIneed = startId % 10000;
		for (long i = 0; i < theNumberIneed; i++) {
			sum += myRandom.nextInt();
		}
		return sum;
	}
}
