import org.junit.Assert;
import org.junit.Test;

public class FooClassTest {

    @Test
    public void fooTest() {
        Foo foo = new Foo();
        foo.foo();
    }

    @Test
    public void foo1Test() {
        Foo foo = new Foo();
        foo.foo2();
    }

    @Test
    public void fooLocalTest() {
        Foo foo = new Foo();
        String result = foo.fooLocal();
        Assert.assertEquals("<error>", result);
    }

    @Test
    public void fooGlobalTest() {
        Foo foo = new Foo();
        String result = foo.fooLocal();
        Assert.assertEquals("", result);
    }

    @Test
    public void fooVariableLocalTest() {
        Foo foo = new Foo();
        String result = foo.usePreviousVaribaleLocal();
        Assert.assertEquals("<error>", result);
    }

    @Test
    public void fooVariableGlobalTest() {
        Foo foo = new Foo();
        String result = foo.usePreviousVaribaleGlobal();
        Assert.assertEquals("InitexpectedOutput", result);
    }

    @Test
    public void returnVoidTest() {
        Foo foo = new Foo();
        foo.returnVoid();
    }
}
