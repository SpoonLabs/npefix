import java.util.ArrayList;
import java.util.List;

public class ImplicitCast {

    private int field;

    public int implicitCastReturn() {
        return new Integer(null);
    }

    public void implicitLocalVariable() {
        int i = new Integer(null);
    }

    public void implicitAssignment() {
        field =  new Integer(null);
    }

    public void implicitAssignmentVariable() {
        Integer value = new Integer(null);
        field =  value;
    }

    public void implicitInvocation() {
        List<Integer> list = new ArrayList<>();
        field =  list.get(0);
    }
}