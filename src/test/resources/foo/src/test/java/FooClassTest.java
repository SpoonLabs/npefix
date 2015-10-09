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
}
