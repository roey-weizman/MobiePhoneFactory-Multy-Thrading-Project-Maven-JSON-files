package bgu.spl.a2;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.lang.Math;

/**
 * represents a work stealing thread pool - to understand what this class does
 * please refer to your assignment.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class WorkStealingThreadPool {
	private Thread[] arrayOfThreads;
	private ConcurrentLinkedDeque<Task<?>>[] arrayOfQueue = null;
	private Processor[] arrayOfProcessor;
	private VersionMonitor VM;
	private boolean Shutdown;

	/**
	 * creates a {@link WorkStealingThreadPool} which has nthreads
	 * {@link Processor}s. Note, threads should not get started until calling to
	 * the {@link #start()} method.
	 *
	 * Implementors note: you may not add other constructors to this class nor
	 * you allowed to add any other parameter to this constructor - changing
	 * this may cause automatic tests to fail..
	 *
	 * @param nthreads
	 *            the number of threads that should be started by this thread
	 *            pool
	 */
	@SuppressWarnings("unchecked")
	public WorkStealingThreadPool(int nthreads) {
		arrayOfThreads = new Thread[nthreads];
		arrayOfProcessor = new Processor[nthreads];
		arrayOfQueue = new ConcurrentLinkedDeque[nthreads];
		VM = new VersionMonitor();
		Shutdown = false;

		for (int i = 0; i < nthreads; i++) {
			Processor p = new Processor(i, this);
			arrayOfProcessor[i] = p;
			arrayOfQueue[i] = new ConcurrentLinkedDeque<Task<?>>();
			arrayOfThreads[i] = new Thread(p);

		}
	}

	/**
	 * submits a task to be executed by a processor belongs to this thread pool
	 *
	 * @param task
	 *            the task to execute
	 */
	public void submit(Task<?> task) {
		int j = (int) (Math.random() * arrayOfThreads.length - 1);
		arrayOfQueue[j].addFirst(task);
		VM.inc();
	}

	/**
	 * closes the thread pool - this method interrupts all the threads and wait
	 * for them to stop - it is returns *only* when there are no live threads in
	 * the queue.
	 *
	 * after calling this method - one should not use the queue anymore.
	 *
	 * @throws InterruptedException
	 *             if the thread that shut down the threads is interrupted
	 * @throws UnsupportedOperationException
	 *             if the thread that attempts to shutdown the queue is itself a
	 *             processor of this queue
	 */
	public void shutdown() throws InterruptedException {
		for (int i = 0; i < arrayOfThreads.length; i++) {
			if (Thread.currentThread() == arrayOfThreads[i])
				throw new UnsupportedOperationException();
		}
		Shutdown = true;

		for (int i = 0; i < arrayOfThreads.length; i++) {
			arrayOfThreads[i].interrupt();
			arrayOfThreads[i].join();

		}

	}

	/**
	 * start the threads belongs to this thread pool
	 */
	public void start() {
		for (int i = 0; i < arrayOfThreads.length; i++) {
			arrayOfThreads[i].start();
		}

	}

	protected boolean steal(int i) {
		boolean flag = false;
		int numOfProcessors = arrayOfProcessor.length;
		for (int j = 1; j < numOfProcessors && !flag; j++) {
			int k = (i + j) % numOfProcessors;// bug
			try {
				if (!arrayOfQueue[k].isEmpty()) {
					for (int x = 0; x <= arrayOfQueue[k].size() / 2; x++) {
						arrayOfQueue[i].addFirst(arrayOfQueue[k].removeLast());
					}
					flag = true;
				}
			} catch (NoSuchElementException e) {
			}
		}

		return flag;
	}

	protected ConcurrentLinkedDeque<Task<?>> getQueueAt(int i) {
		return arrayOfQueue[i];
	}

	protected VersionMonitor getVM() {
		return VM;
	}

	protected boolean isShutdown() {
		return Shutdown;
	}
}
