package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.AbstractEvaluation;
import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by thomas on 13/10/15.
 */
public abstract class AbstractNPEDataset extends AbstractEvaluation {

    static final String rootNPEDataset = Config.CONFIG.getDatasetRoot();

    public static final String COLLECTIONS_360 = "collections360";
    public static final String FELIX4960 = "felix-4960";
    public static final String LANG_304 = "lang304";
    public static final String LANG_587 = "lang587";
    public static final String LANG_703 = "lang703";
    public static final String MATH_290 = "math290";
    public static final String MATH_305 = "math305";
    public static final String MATH_369 = "math369";
    public static final String MATH_988_A = "math988a";
    public static final String MATH_988_B = "math988b";
    public static final String MATH_1115 = "math1115";
    public static final String MATH_1117 = "math1117";
    public static final String PDFBOX_2812 = "pdfbox-2812";
    public static final String PDFBOX_2965 = "pdfbox-2965";
    public static final String PDFBOX_2995 = "pdfbox-2995";
    public static final String SLING_4982 = "sling-4982";

    public static final String classpathCollections360 =  depArrayToClassPath("junit/junit/4.7/junit-4.7.jar");
    public static final String classpathLang304 =  depArrayToClassPath("junit/junit/4.7/junit-4.7.jar");
    public static final String classpathLang587 =  depArrayToClassPath("junit/junit/4.7/junit-4.7.jar");
    public static final String classpathLang703 =  depArrayToClassPath("junit/junit/4.7/junit-4.7.jar");
    public static final String classpathMath290 =  depArrayToClassPath("junit/junit/4.7/junit-4.7.jar");
    public static final String classpathMath305 =  depArrayToClassPath("junit/junit/4.7/junit-4.7.jar");
    public static final String classpathMath369 =  depArrayToClassPath("junit/junit/4.7/junit-4.7.jar");
    public static final String classpathMath988a =  depArrayToClassPath("junit/junit/4.7/junit-4.7.jar");
    public static final String classpathMath988b =  depArrayToClassPath("junit/junit/4.7/junit-4.7.jar");
    public static final String classpathMath1115 =  depArrayToClassPath("junit/junit/4.7/junit-4.7.jar");
    public static final String classpathMath1117 =  depArrayToClassPath("junit/junit/4.7/junit-4.7.jar");
    public static final String classpathPdfbox2812 =  depArrayToClassPath("org/apache/pdfbox/fontbox/1.8.10/fontbox-1.8.10.jar",
            "commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar",
            "org/apache/pdfbox/jempbox/1.8.10/jempbox-1.8.10.jar",
            "org/bouncycastle/bcmail-jdk15/1.44/bcmail-jdk15-1.44.jar",
            "org/bouncycastle/bcprov-jdk15/1.44/bcprov-jdk15-1.44.jar",
            "com/ibm/icu/icu4j/3.8/icu4j-3.8.jar",
            "junit/junit/4.8.1/junit-4.8.1.jar",
            "com/levigo/jbig2/levigo-jbig2-imageio/1.6.2/levigo-jbig2-imageio-1.6.2.jar",
            "net/java/dev/jai-imageio/jai-imageio-core-standalone/1.2-pre-dr-b04-2011-07-04/jai-imageio-core-standalone-1.2-pre-dr-b04-2011-07-04.jar");
    public static final String classpathPdfbox2995 =  depArrayToClassPath("org/apache/pdfbox/fontbox/2.0.0-RC1/fontbox-2.0.0-RC1.jar",
            "commons-logging/commons-logging/1.2/commons-logging-1.2.jar",
            "org/bouncycastle/bcpkix-jdk15on/1.50/bcpkix-jdk15on-1.50.jar",
            "org/bouncycastle/bcprov-jdk15on/1.50/bcprov-jdk15on-1.50.jar",
            "junit/junit/4.12/junit-4.12.jar",
            "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar");
    public static final String classpathPdfbox2965 =  depArrayToClassPath("org/apache/pdfbox/fontbox/2.0.0-RC1/fontbox-2.0.0-RC1.jar",
            "commons-logging/commons-logging/1.2/commons-logging-1.2.jar",
            "org/bouncycastle/bcpkix-jdk15on/1.50/bcpkix-jdk15on-1.50.jar",
            "org/bouncycastle/bcprov-jdk15on/1.50/bcprov-jdk15on-1.50.jar",
            "junit/junit/4.12/junit-4.12.jar",
            "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar");
    public static final String classpathFelix4960 =  depArrayToClassPath("org/osgi/org.osgi.annotation/6.0.0/org.osgi.annotation-6.0.0.jar",
            "org/apache/felix/org.apache.felix.resolver/1.5.0-SNAPSHOT/org.apache.felix.resolver-1.5.0-SNAPSHOT.jar",
            "org/osgi/org.osgi.core/5.0.0/org.osgi.core-5.0.0.jar",
            "org/ow2/asm/asm-all/4.2/asm-all-4.2.jar",
            "org/mockito/mockito-all/1.10.19/mockito-all-1.10.19.jar",
            "junit/junit/4.7/junit-4.7.jar",
            "org/easymock/easymock/2.4/easymock-2.4.jar");
    public static final String classpathSling4982 =  depArrayToClassPath("javax/servlet/servlet-api/2.4/servlet-api-2.4.jar",
            "org/apache/sling/org.apache.sling.api/2.1.0/org.apache.sling.api-2.1.0.jar",
            "org/apache/sling/org.apache.sling.commons.osgi/2.1.0/org.apache.sling.commons.osgi-2.1.0.jar",
            "org/apache/felix/org.apache.felix.scr.annotations/1.9.12/org.apache.felix.scr.annotations-1.9.12.jar",
            "org/osgi/org.osgi.core/4.1.0/org.osgi.core-4.1.0.jar",
            "org/osgi/org.osgi.compendium/4.1.0/org.osgi.compendium-4.1.0.jar",
            "org/slf4j/slf4j-api/1.5.2/slf4j-api-1.5.2.jar",
            "org/slf4j/slf4j-simple/1.5.2/slf4j-simple-1.5.2.jar",
            "org/mockito/mockito-all/1.8.2/mockito-all-1.8.2.jar",
            "junit/junit/4.11/junit-4.11.jar",
            "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar",
            "org/jmock/jmock-junit4/2.5.1/jmock-junit4-2.5.1.jar",
            "org/jmock/jmock/2.5.1/jmock-2.5.1.jar",
            "org/hamcrest/hamcrest-library/1.1/hamcrest-library-1.1.jar",
            "junit/junit-dep/4.4/junit-dep-4.4.jar",
            "junit-addons/junit-addons/1.4/junit-addons-1.4.jar",
            "xerces/xercesImpl/2.6.2/xercesImpl-2.6.2.jar",
            "xerces/xmlParserAPIs/2.6.2/xmlParserAPIs-2.6.2.jar",
            "biz/aQute/bndlib/1.50.0/bndlib-1.50.0.jar");

    public abstract void eval(NPEOutput results);

    @Ignore
    @Test
    public void collections331() throws Exception {
        String root = rootNPEDataset + "collections-331/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };
        NPEOutput output = runProject("collections331", source, test, deps);
        eval(output);
    }

    @Test
    @Ignore
    public void collections360() throws Exception {
        // svn 1076034
        String root = rootNPEDataset + "collections-360/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };
        NPEOutput output = runProject(COLLECTIONS_360, source, test, deps);
        eval(output);
    }

    @Test
    public void lang304() throws Exception {
        String root = rootNPEDataset + "lang-304/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput output = runProject(LANG_304, source, test, deps);
        eval(output);
    }

    @Test
    public void lang587() throws Exception {
        String root = rootNPEDataset + "lang-587/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput output = runProject(LANG_587, source, test, deps);
        eval(output);
    }

    @Test
    public void lang703() throws Exception {
        String root = rootNPEDataset + "lang-703/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput output = runProject(LANG_703, source, test, deps);
        eval(output);
    }

    @Test
    public void math290() throws Exception {
        String root = rootNPEDataset + "math-290/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput output = runProject(MATH_290, source, test, deps);
        eval(output);
    }

    @Test
    public void math305() throws Exception {
        String root = rootNPEDataset + "math-305/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput output = runProject(MATH_305, source, test, deps);
        eval(output);
    }

    @Test
    public void math369() throws Exception {
        String root = rootNPEDataset + "math-369/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput output = runProject(MATH_369, source, test, deps);
        eval(output);
    }

    @Test
    public void math988a() throws Exception {
        String root = rootNPEDataset + "math-988a/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput output = runProject(MATH_988_A, source, test, deps);
        eval(output);
    }

    @Test
    public void math988b() throws Exception {
        String root = rootNPEDataset + "math-988b/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput output = runProject(MATH_988_B, source, test, deps);
        eval(output);
    }

    @Test
    public void math1115() throws Exception {
        String root = rootNPEDataset + "math-1115/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput output = runProject(MATH_1115, source, test, deps);
        eval(output);
    }

    @Test
    public void math1117() throws Exception {
        String root = rootNPEDataset + "math-1117/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput output = runProject(MATH_1117, source, test, deps);
        eval(output);
    }


    @Test
    public void pdfbox2995() throws Exception {
        // commit 1705415
        String root = rootNPEDataset + "pdfbox_2995/";
        String source = root + "src/main/";
        String test = root + "src/test/";
        String[] deps = new String[]{
                "org/apache/pdfbox/fontbox/2.0.0-RC1/fontbox-2.0.0-RC1.jar",
                "commons-logging/commons-logging/1.2/commons-logging-1.2.jar",
                "org/bouncycastle/bcpkix-jdk15on/1.50/bcpkix-jdk15on-1.50.jar",
                "org/bouncycastle/bcprov-jdk15on/1.50/bcprov-jdk15on-1.50.jar",
                "junit/junit/4.12/junit-4.12.jar",
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
        };
        NPEOutput output = runProject(PDFBOX_2995, source, test, deps);
        eval(output);
    }

    @Test
    public void pdfbox2965() throws Exception {
        // commit 1701905
        String root = rootNPEDataset + "pdfbox_2965/";
        String source = root + "src/main/";
        String test = root + "src/test/";
        String[] deps = new String[]{
                "org/apache/pdfbox/fontbox/2.0.0-RC1/fontbox-2.0.0-RC1.jar",
                "commons-logging/commons-logging/1.2/commons-logging-1.2.jar",
                "org/bouncycastle/bcpkix-jdk15on/1.50/bcpkix-jdk15on-1.50.jar",
                "org/bouncycastle/bcprov-jdk15on/1.50/bcprov-jdk15on-1.50.jar",
                "junit/junit/4.12/junit-4.12.jar",
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
        };
        NPEOutput output = runProject(PDFBOX_2965, source, test, deps);
        eval(output);
    }

    @Test
    public void pdfbox2812() throws Exception {
        // commit 1681643
        String root = rootNPEDataset + "pdfbox_2812/";
        String source = root + "src/main/";
        String test = root + "src/test/";
        String[] deps = new String[]{
                "org/apache/pdfbox/fontbox/1.8.10/fontbox-1.8.10.jar",
                "commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar",
                "org/apache/pdfbox/jempbox/1.8.10/jempbox-1.8.10.jar",
                "org/bouncycastle/bcmail-jdk15/1.44/bcmail-jdk15-1.44.jar",
                "org/bouncycastle/bcprov-jdk15/1.44/bcprov-jdk15-1.44.jar",
                "com/ibm/icu/icu4j/3.8/icu4j-3.8.jar",
                "junit/junit/4.8.1/junit-4.8.1.jar",
                "com/levigo/jbig2/levigo-jbig2-imageio/1.6.2/levigo-jbig2-imageio-1.6.2.jar",
                "net/java/dev/jai-imageio/jai-imageio-core-standalone/1.2-pre-dr-b04-2011-07-04/jai-imageio-core-standalone-1.2-pre-dr-b04-2011-07-04.jar"
        };
        NPEOutput output = runProject(PDFBOX_2812, source, test, deps);
        eval(output);
    }

    @Test
    public void felix4960() throws Exception {
        // commit 1691137
        String root = rootNPEDataset + "felix-4960/";
        String source = root + "src/main/";
        String test = root + "src/test/";
        String[] deps = new String[]{
                "org/osgi/org.osgi.annotation/6.0.0/org.osgi.annotation-6.0.0.jar",
                "org/apache/felix/org.apache.felix.resolver/1.5.0-SNAPSHOT/org.apache.felix.resolver-1.5.0-SNAPSHOT.jar",
                "org/osgi/org.osgi.core/5.0.0/org.osgi.core-5.0.0.jar",
                "org/ow2/asm/asm-all/4.2/asm-all-4.2.jar",
                "org/mockito/mockito-all/1.10.19/mockito-all-1.10.19.jar",
                "junit/junit/4.7/junit-4.7.jar",
                "org/easymock/easymock/2.4/easymock-2.4.jar"
        };
        NPEOutput output = runProject(FELIX4960, source, test, deps);
        eval(output);
    }

    @Test
    public void sling4982() throws Exception {
        // commit 1700424
        String root = rootNPEDataset + "sling_4982/";
        String source = root + "src/main/";
        String test = root + "src/test/";
        String[] deps = new String[]{
                "javax/servlet/servlet-api/2.4/servlet-api-2.4.jar",
                "org/apache/sling/org.apache.sling.api/2.1.0/org.apache.sling.api-2.1.0.jar",
                "org/apache/sling/org.apache.sling.commons.osgi/2.1.0/org.apache.sling.commons.osgi-2.1.0.jar",
                "org/apache/felix/org.apache.felix.scr.annotations/1.9.12/org.apache.felix.scr.annotations-1.9.12.jar",
                "org/osgi/org.osgi.core/4.1.0/org.osgi.core-4.1.0.jar",
                "org/osgi/org.osgi.compendium/4.1.0/org.osgi.compendium-4.1.0.jar",
                "org/slf4j/slf4j-api/1.5.2/slf4j-api-1.5.2.jar",
                "org/slf4j/slf4j-simple/1.5.2/slf4j-simple-1.5.2.jar",
                "org/mockito/mockito-all/1.8.2/mockito-all-1.8.2.jar",
                "junit/junit/4.11/junit-4.11.jar",
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar",
                "org/jmock/jmock-junit4/2.5.1/jmock-junit4-2.5.1.jar",
                "org/jmock/jmock/2.5.1/jmock-2.5.1.jar",
                "org/hamcrest/hamcrest-library/1.1/hamcrest-library-1.1.jar",
                "junit/junit-dep/4.4/junit-dep-4.4.jar",
                "junit-addons/junit-addons/1.4/junit-addons-1.4.jar",
                "xerces/xercesImpl/2.6.2/xercesImpl-2.6.2.jar",
                "xerces/xmlParserAPIs/2.6.2/xmlParserAPIs-2.6.2.jar",
                "biz/aQute/bndlib/1.50.0/bndlib-1.50.0.jar"
        };
        NPEOutput output = runProject(SLING_4982, source, test, deps);
        eval(output);
    }

    @Test
    @Ignore
    public void mckoi01() throws Exception {
        String root = rootNPEDataset + "Mckoi-01/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("mckoi01", source, test, deps);
        eval(results);
    }
}
