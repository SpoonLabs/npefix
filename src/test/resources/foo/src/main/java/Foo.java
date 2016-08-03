import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Foo {
    public String field = null;
    public String[] array = null;

    public Foo() {
        super();
        field = null;
        array = null;
    }

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

    public void notnull() {
        if(field == null) {
            return;
        } else {
            field = field.concat("expectedOutput");
        }
        return;
    }

    public void multiCatch() {
        try {

        } catch (IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void genericInMethodType() {
        List<? extends String> var = new ArrayList<>();
        if(var.get(0).toString().equals("")) {

        }
    }

    public static <T> void  genericWithoutExtendsInMethodType() {
        List<T> var = new ArrayList<>();
        if(var.get(0).toString().equals("")) {

        }
    }

    public void  multiDecisionLine() {
        Arrays.asList(field.toString(), field.toString());
    }

    public void  elseIf() {
        if (field == array[0]) {

        } else if (array[0].isEmpty()) {

        }
    }
}