import org.junit.Test;
import org.junit.Assert;

public class ConeflowerTest {

	@Test
	public void test1() throws Exception {
		Coneflower flower = new Coneflower();
		Assert.assertEquals("Cutleaf coneflower,Brilliant coneflower,", flower.method());
	}

	@Test
	public void test2() throws Exception {
		Coneflower flower = new Coneflower();
		Assert.assertEquals("Brilliant coneflower,", flower.intermediateMethod());
	}

	@Test
	public void test3() throws Exception {
		Coneflower flower = new Coneflower();
		Assert.assertEquals("", flower.methodThrowingNPE());
	}

	@Test
	public void testThrowException() throws Exception {
		Coneflower flower = new Coneflower();
		Assert.assertEquals("failing", 0, flower.throwingException());
	}
}