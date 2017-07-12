import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConeflowerTest {

	@Test
	public void test1() throws Exception {
		Coneflower flower = new Coneflower();
		assertEquals("Cutleaf coneflower,Brilliant coneflower,", flower.method());
	}

	@Test
	public void test2() throws Exception {
		Coneflower flower = new Coneflower();
		assertEquals("Brilliant coneflower,", flower.intermediateMethod());
	}

	@Test
	public void test3() throws Exception {
		Coneflower flower = new Coneflower();
		assertEquals("", flower.methodThrowingNPE());
	}
}