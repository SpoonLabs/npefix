import fr.inria.spirals.npefix.resi.strategies.Strat1A;

public class Foo {
    public String field = null;
    public String[] array = null;
    public void foo() {
        field.toString();
    }

    public Object foo2() {
        String result = "";
        for (String element : array) {
            result += element.toString();
            if(element == null) {
                return null;
            }
        }
        return result;
    }

    public String fooLocal() {
        System.out.print(field.toLowerCase());
        if(field == null) {
            return "<error>";
        }
        return field;
    }

    public String fooGlobal() {
        System.out.print(field.toLowerCase());
        if(field == null) {
            return "<error>";
        }
        return field;
    }


    public String usePreviousVaribaleLocal() {
        String empty = "Init";
        field = field.concat("expectedOutput");
        if(field == null) {
            return "<error>";
        }
        return field;
    }

    public String usePreviousVaribaleGlobal() {
        String empty = "Init";
        field = field.concat("expectedOutput");
        if(field == null) {
            return "<error>";
        }
        return field;
    }


    public void returnVoid() {
        field = field.concat("expectedOutput");
        if(field == null) {
            return;
        }
        return;
    }
}