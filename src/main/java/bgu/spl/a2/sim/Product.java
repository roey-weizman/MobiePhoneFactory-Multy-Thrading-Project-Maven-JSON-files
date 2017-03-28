package bgu.spl.a2.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


/**
 * A class that represents a product produced during the simulation.
 */
public class Product implements java.io.Serializable {
	/**
	 * 
	 */
	/**
	 * Constructor
	 * 
	 * @param startId
	 *            - Product start id
	 * @param name
	 *            - Product name
	 */

	private String name;
	private long startId;
	private AtomicLong finalId=new AtomicLong();
	private List<Product> partsList;
	private static final long serialVersionUID = 1L;

	public Product(long startId, String name) {
		this.startId = startId;
		this.finalId.set(startId);
		this.name = name;
		partsList =new ArrayList<Product>();

	}

	/**
	 * @return The product name as a string
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The product start ID as a long. start ID should never be changed.
	 */
	public long getStartId() {
		return startId;
	}

	/**
	 * @return The product final ID as a long. final ID is the ID the product
	 *         received as the sum of all UseOn();
	 */
	public long getFinalId() {
		return finalId.get();
	}

	public void setFinalId(long finaly) {
		finalId.set(finaly);
	}

	/**
	 * @return Returns all parts of this product as a List of Products
	 */
	public List<Product> getParts() {
		return partsList;
	}

	/**
	 * Add a new part to the product
	 * 
	 * @param p
	 *            - part to be added as a Product object
	 */
	public void addPart(Product p) {
		partsList.add(p);
			
	}

}
