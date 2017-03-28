package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class DeferredTest<T> {
	private Deferred<Integer> t;
	private Deferred<Integer> p;
	private Deferred<Integer> g;

	@Before
	public void setUp() throws Exception {
		t = new Deferred<Integer>();
		p = new Deferred<Integer>();
		g = new Deferred<Integer>();

	}

	@Test
	public void testGet() {
		boolean flag = false;
		try {
			t.get();
		} catch (IllegalStateException i) {
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		assertEquals("IllegalStateException thrown", flag, true);
		Integer a = new Integer(5);
		a = a + 1;
		t.resolve(a);

		try {

			assertEquals(new Integer(6), t.get());
		} catch (Exception e) {
			Assert.fail("get method not working as expected");
		}

	}

	@Test
	public void testIsResolved() {
		try {

			assertEquals(false, t.isResolved());
		} catch (Exception e) {
			Assert.fail("IsResolved method not working as expected");
		}

	}

	@Test
	public void testResolve() {
		boolean flag = false;
		int[] j = new int[5];
		Integer s = new Integer(10);
		p.whenResolved(() -> {
			j[3] = 2;

		});

		s = s - 2;
		try {
			p.resolve(s);
		}

		catch (Exception e) {
			Assert.fail("resolved method not working as expected");
		}
		assertEquals(new Integer(8), s);
		assertEquals(j[3], 2);

		try {
			p.resolve(s);
		} catch (IllegalStateException i) {
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		assertEquals("IllegalStateException thrown", flag, true);
	}

	@Test
	public void testWhenResolved() {

		String[] j = new String[5];

		try {
			g.whenResolved(() -> {
				j[2] = "run me when you finish";

			});
		} catch (Exception e) {
			Assert.fail("whenResolved method not working as expected");
		}
		assertNotSame("IllegalStateException thrown", j[2], "run me when you finish");
		g.resolve(new Integer(17));
		
		assertEquals("whenResolved is working", j[2], "run me when you finish");
	}

	@After
	public void tearDown() throws Exception {
	}

}
