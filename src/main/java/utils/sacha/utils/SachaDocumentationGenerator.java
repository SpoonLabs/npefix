package utils.sacha.utils;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.visitor.filter.TypeFilter;

/** Generates the documentation of sacha-infrastructure */
public class SachaDocumentationGenerator extends AbstractManualProcessor {

  @Override
  public void process() {
    System.out.println("Generated Documentation of sacha-infrastructure\n"+
                       "===============================================\n"+
    		           "(generated with sacha.utils.SachaDocumentationGenerator)\n\n"+
                       "Sacha-infrastructure supports the following use cases:\n\n");
    
    for (CtPackage pack : getFactory().Package().getAll()) {
      
      for (CtPackage pack2 : pack.getPackages()) {
      if ("sacha.mains".equals(pack2.getQualifiedName())) {
        for (CtClass c : pack2.getElements(new TypeFilter<CtClass>(CtClass.class))) {
          System.out.println("\n-------------------\n"+c.getSimpleName()+": " + c.getDocComment());
        }
      }
      }
    }
  }

  public static void main(String[] _) throws Exception {
    String[] args = {
        "-p", "sacha.utils.SachaDocumentationGenerator", "-i", "src"
    };
//    new Launcher(args).run();;
  }
  
}
