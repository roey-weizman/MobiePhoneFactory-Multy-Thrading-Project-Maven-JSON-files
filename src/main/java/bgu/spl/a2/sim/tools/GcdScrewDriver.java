package bgu.spl.a2.sim.tools;

import java.math.BigInteger;
import java.lang.Math;
import bgu.spl.a2.sim.Product;

public class GcdScrewDriver implements Tool {
	/**
	 * @return tool name as string
	 */
	public String getType() {
		return "gs-driver";
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

	public long reverse(long n) {
		long reverse = 0;
		while (n != 0) {
			reverse = reverse * 10;
			reverse = reverse + n % 10;
			n = n / 10;
		}
		return reverse;
	}

	public long func(long id) {
		BigInteger b1 = BigInteger.valueOf(id);
		BigInteger b2 = BigInteger.valueOf(reverse(id));
		long value = (b1.gcd(b2)).longValue();
		return value;
	}
}