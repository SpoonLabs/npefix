import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FooArrayAccess {

    public String emtpyArray() {
        String[] array = new String[0];
        return array[0];
    }

    public String indexToSmall() {
        String[] array = new String[]{"Test"};
        return array[-1];
    }

    public String indexToBig() {
        String[] array = new String[]{"Test"};
        return array[1];
    }

}