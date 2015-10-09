import fr.inria.spirals.npefix.resi.strategies.Strat1A;

public class Foo {
    public String field = null;
    public String[] array = null;
    public void foo() {
        field.toString();
    }

    public void foo2() {
        for (final String element : array) {
            element.toString();
        }
    }
}