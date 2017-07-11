import org.junit.Assert;
import org.junit.Test;

public class FooArrayAccessTest {

    @Test
    public void fooTest() {
        FooArrayAccess foo = new FooArrayAccess();
        foo.emtpyArray();
    }

    @Test
    public void fooTest1() {
        FooArrayAccess foo = new FooArrayAccess();
        foo.indexToSmall();
    }


    @Test
    public void fooTest2() {
        FooArrayAccess foo = new FooArrayAccess();
        foo.indexToBig();
    }
}
