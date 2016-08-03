import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        List<List<List<? extends Objects>>> list = new ArrayList<>();
        if(!list.get(0).isEmpty()) {
            List objects = list.get(0);
        }
    }
}