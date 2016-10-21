package fr.inria.spirals.npefix.patchTemplate.testClasses;

import java.util.ArrayList;

public class ChildClassSamePackage extends ParentClass {
    public int publicChildField;
    private String privateChildField;
    protected double protectedChildField;
    String defaultChildField;

    public void m(String parameter) {
        String localVariable = "";
        String[] array = new String[0];
        for(Object foreach: new ArrayList<>()) {
            new ArrayList() {
                private String innerClass;

                private void m(String parameterInner) {
                    this.get(8).getClass();
                    privateChildField.toString();
                }
            };
        }
    }
}