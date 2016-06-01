package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.AbstractEvaluation;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.Strategy;
import fr.inria.spirals.npefix.transformer.processors.RemoveNullCheckProcessor;
import org.junit.Ignore;
import org.junit.Test;
import spoon.Launcher;

import java.io.File;
import java.util.List;


public class PaperProjects extends AbstractEvaluation {

    @Test
    public void math() throws Exception {
        String source = "/home/thomas/git/commons-math/src/main/";
        String test = "/home/thomas/git/commons-math/src/test/";
        String[] deps = new String[]{
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar",
                "junit/junit/4.12/junit-4.12.jar"
        };

        Strategy strat = new NoStrat();
        List<Lapse> results = runProject("math", source, test, deps, true, strat);
    }

    @Test
    public void lang() throws Exception {
        String source = "/home/thomas/git/commons-lang/src/main/";
        String test = "/home/thomas/git/commons-lang/src/test/";
        String[] deps = new String[]{
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar",
                "junit/junit/4.12/junit-4.12.jar",
                "org/hamcrest/hamcrest-all/1.3/hamcrest-all-1.3.jar",
                "commons-io/commons-io/2.4/commons-io-2.4.jar",
                "org/easymock/easymock/3.3.1/easymock-3.3.1.jar",
                "cglib/cglib/3.1/cglib-3.1.jar",
                "org/ow2/asm/asm/5.0.3/asm-5.0.3.jar",
                "org/objenesis/objenesis/2.1/objenesis-2.1.jar"
        };

        Strategy strat = new NoStrat();
        List<Lapse> results = runProject("lang", source, test, deps, true, strat);
    }

    @Test
    public void spojo() throws Exception {
        String source = "/home/thomas/git/Spojo/spojo-core/src/main/java";
        String test = "/home/thomas/git/Spojo/spojo-core/src/test/java";
        String[] deps = new String[]{
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar",
                "junit/junit/4.7/junit-4.7.jar",
                "org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar",
                "org/slf4j/slf4j-log4j12/1.6.1/slf4j-log4j12-1.6.1.jar",
                "log4j/log4j/1.2.16/log4j-1.2.16.jar",
                "org/springframework/spring-beans/3.0.6.RELEASE/spring-beans-3.0.6.RELEASE.jar",
                "org/springframework/spring-core/3.0.6.RELEASE/spring-core-3.0.6.RELEASE.jar",
                "org/springframework/spring-asm/3.0.6.RELEASE/spring-asm-3.0.6.RELEASE.jar",
                "commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar",
                "com/google/code/gson/gson/1.4/gson-1.4.jar"
        };

        Strategy strat = new NoStrat();
        List<Lapse> results = runProject("spojo", source, test, deps, true, strat);
    }

    @Test
    public void spojo_withoutNPECheck() throws Exception {
        String name = "spojo_withoutNPECheck";
        String source = "/home/thomas/git/Spojo/spojo-core/src/main/java";
        String test = "/home/thomas/git/Spojo/spojo-core/src/test/java";

        String[] deps = new String[]{
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar",
                "junit/junit/4.7/junit-4.7.jar",
                "org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar",
                "org/slf4j/slf4j-log4j12/1.6.1/slf4j-log4j12-1.6.1.jar",
                "log4j/log4j/1.2.16/log4j-1.2.16.jar",
                "org/springframework/spring-beans/3.0.6.RELEASE/spring-beans-3.0.6.RELEASE.jar",
                "org/springframework/spring-core/3.0.6.RELEASE/spring-core-3.0.6.RELEASE.jar",
                "org/springframework/spring-asm/3.0.6.RELEASE/spring-asm-3.0.6.RELEASE.jar",
                "commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar",
                "com/google/code/gson/gson/1.4/gson-1.4.jar"
        };

        spoon.Launcher spoon = new Launcher();
        spoon.addInputResource(source);
        source = "/tmp/npefix/" + name + "/src";
        spoon.setSourceOutputDirectory(source);
        spoon.addProcessor(new RemoveNullCheckProcessor());
        spoon.getEnvironment().setAutoImports(true);
        spoon.getModelBuilder().setSourceClasspath(depArrayToClassPath(deps).split(File.pathSeparator));
        spoon.run();

        runProject(name, source, test, deps, false);
    }

    @Test
    public void commonsCodec() throws Exception {
        String name = "commonsCodec";
        String source = "/home/thomas/git/commons-codec/src/main/";
        String test = "/home/thomas/git/commons-codec/src/test/";
        String[] deps = new String[]{
                "junit/junit/4.12/junit-4.12.jar",
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar",
                "org/apache/commons/commons-lang3/3.4/commons-lang3-3.4.jar"
        };

        Strategy strat = new NoStrat();
        List<Lapse> results = runProject(name, source, test, deps, true, strat);
    }

    @Test
    public void commonsCodec_withoutNPECheck() throws Exception {
        String name = "commonsCodec_withoutNPECheck";
        String source = "/home/thomas/git/commons-codec/src/main/";
        String test = "/home/thomas/git/commons-codec/src/test/";
        String[] deps = new String[]{
                "junit/junit/4.12/junit-4.12.jar",
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar",
                "org/apache/commons/commons-lang3/3.4/commons-lang3-3.4.jar"
        };
        if(isInstrumentCode()) {
            spoon.Launcher spoon = new Launcher();
            spoon.addInputResource(source);
            source = "/tmp/npefix/" + name + "/src";
            spoon.setSourceOutputDirectory(source);
            spoon.addProcessor(new RemoveNullCheckProcessor());
            spoon.getEnvironment().setAutoImports(true);
            spoon.getModelBuilder().setSourceClasspath(
                    depArrayToClassPath(deps).split(File.pathSeparator));
            spoon.run();
        } else {
            source = "/tmp/npefix/" + name + "/src";
        }

        runProject(name, source, test, deps, false);
    }

    @Test
    public void commonsOkio() throws Exception {
        String name = "commonsOkio";
        String source = "/home/thomas/git/okio/okio/src/main/";
        String test = "/home/thomas/git/okio/okio/src/test/";
        String[] deps = new String[]{
                "junit/junit/4.12/junit-4.12.jar",
                "org/codehaus/mojo/animal-sniffer-annotations/1.10/animal-sniffer-annotations-1.10.jar",
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
        };

        Strategy strat = new NoStrat();
        List<Lapse> results = runProject(name, source, test, deps, true, strat);
    }

    @Test
    public void commonsOkio_withoutNPECheck() throws Exception {
        String name = "commonsOkio_withoutNPECheck";
        String source = "/home/thomas/git/okio/okio/src/main/";
        String test = "/home/thomas/git/okio/okio/src/test/";

        String[] deps = new String[]{
                "junit/junit/4.12/junit-4.12.jar",
                "org/codehaus/mojo/animal-sniffer-annotations/1.10/animal-sniffer-annotations-1.10.jar",
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
        };

        spoon.Launcher spoon = new Launcher();
        spoon.addInputResource(source);
        source = "/tmp/npefix/" + name + "/src";
        spoon.setSourceOutputDirectory(source);
        spoon.addProcessor(new RemoveNullCheckProcessor());
        spoon.getEnvironment().setAutoImports(true);
        spoon.getModelBuilder().setSourceClasspath(depArrayToClassPath(deps).split(File.pathSeparator));
        spoon.run();

        runProject(name, source, test, deps, false);
    }


    @Test
    @Ignore
    public void junit() throws Exception {
        String source = "/home/thomas/git/junit/src/main/java";
        String test = "/home/thomas/git/junit/src/test/java";
        String[] deps = new String[]{
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
        };

        Strategy strat = new NoStrat();
        List<Lapse> results = runProject("junit", source, test, deps, false, strat);
    }
}
