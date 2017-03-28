package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class VersionMonitorTest {
	private VersionMonitor t;

	@Before
	public void setUp() throws Exception {
		t = new VersionMonitor();
	}



	@Test
	public void getVersion() {
		try{
		assertEquals(0, t.getVersion());
	}		
		catch (Exception e){
		Assert.fail("getVersion method not working as expected");
	}
	}
	@Test
	public void inc() {
		try{
			int a=t.getVersion();
			t.inc();
			t.inc();
			
			assertEquals(a+2, t.getVersion());
			}
			catch (Exception e){
				Assert.fail("inc method not working as expected");
			}
	}

	@Test
	public void await() {
		int a=t.getVersion();
		Thread t1=new Thread(() -> { 
			try{
				t.await(a);		
				}

			catch(Exception e){
				Assert.fail("await method not working as expected");
			}
		});
		t1.start();
		t.inc();
		try{
			t1.join();
		}
		catch(InterruptedException i){
		
		}
		
		assertNotSame(a, t.getVersion());

	}

	@After
	public void tearDown() throws Exception {
	}

}
