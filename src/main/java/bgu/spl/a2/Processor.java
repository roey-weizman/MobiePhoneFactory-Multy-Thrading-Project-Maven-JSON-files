package bgu.spl.a2;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;;

/**
 * this class represents a single work stealing processor, it is
 * {@link Runnable} so it is suitable to be executed by threads.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 *
 */
public class Processor implements Runnable {

	private final WorkStealingThreadPool pool;
	private final int id;

	/**
	 * constructor for this class
	 *
	 * IMPORTANT: 1) this method is package protected, i.e., only classes inside
	 * the same package can access it - you should *not* change it to
	 * public/private/protected
	 *
	 * 2) you may not add other constructors to this class nor you allowed to
	 * add any other parameter to this constructor - changing this may cause
	 * automatic tests to fail..
	 *
	 * @param id
	 *            - the processor id (every processor need to have its own
	 *            unique id inside its thread pool)
	 * @param pool
	 *            - the thread pool which owns this processor
	 */
	/* package */ Processor(int id, WorkStealingThreadPool pool) {
		this.id = id;
		this.pool = pool;
	}

	/**
	 * meir: run all the tasks in my queue.if no tasks in my queue then i try to
	 * steal. if i succeeded then i run again. if not able to steal then i wait
	 * till the VM changes.
	 * 
	 */
	@Override
	public void run() {

		ConcurrentLinkedDeque<Task<?>> currQueue = pool.getQueueAt(id);
		try {
			while (!currQueue.isEmpty() && !(pool.isShutdown())) {
				Task<?> t = ((Task<?>) currQueue.removeFirst());
				t.handle(this);
			}
		} catch (NoSuchElementException e) {
		}
		if (!(pool.isShutdown())) {
			int VMrightNow = pool.getVM().getVersion();
			if (pool.steal(id))
				run();
			else {
				try {
					pool.getVM().await(VMrightNow);
				} catch (InterruptedException e) {
				}
				this.run();

			}
		}
	}

	protected int getId() {
		return id;
	}

	protected WorkStealingThreadPool getPool() {
		return pool;
	}

}
