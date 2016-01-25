public class FooTernary {

    String field;

    public String m1() {
        String local = (field == null?"": field);
        if(field == null) {
            System.out.println("test");
        }
        return (field == null?"": field);
    }

    public void m2(String parm) {
        field = (parm == null?"": parm);
    }

    public static void add(Object[] array, Object element) {
        Class type = array != null ? array.getClass() : element != null ? element.getClass() : Object.class;
    }
}