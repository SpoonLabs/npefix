package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.AbstractEvaluation;
import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import org.junit.Assert;
import org.junit.Test;

/**
 * Abstract selector evaluation
 */
public abstract class AbstractSelectorEvaluation extends AbstractEvaluation {

	private String rootNPEDataset = Config.CONFIG.getDatasetRoot();

	private Selector selector;

	private int nbIteration = Config.CONFIG.getNbIteration();

	public AbstractSelectorEvaluation() {
	}

	private void eval(NPEOutput  results) {
		int nbFailing = results.getFailureCount();
		String output = "The selector " + selector + " finishes with " + nbFailing + " failing tests";
		System.out.println(output);
		Assert.assertTrue(output, nbFailing != results.size());
	}

	protected NPEOutput runProject(String name, String source,
			String test, String[] deps) {
		NPEOutput results = multipleRunsProject(
				name,
				source, test, deps, true, nbIteration, selector);
		eval(results);
		return null;
	}

	public void setSelector(
			Selector selector) {
		this.selector = selector;
	}

	@Test
	public void collections331() throws Exception {
		String root = rootNPEDataset + "collections-331/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};
		runProject("collections331", source, test, deps);
	}

	@Test
	public void collections360() throws Exception {
		// svn 1076034
		String root = rootNPEDataset + "collections-360/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};
		runProject("collections360", source, test, deps);
	}

	@Test
	public void lang304() throws Exception {
		String root = rootNPEDataset + "lang-304/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};

		runProject("lang304", source, test, deps);
	}

	@Test
	public void lang587() throws Exception {
		String root = rootNPEDataset + "lang-587/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};

		runProject("lang587", source, test, deps);
	}

	@Test
	public void lang703() throws Exception {
		String root = rootNPEDataset + "lang-703/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};

		runProject("lang703", source, test, deps);
	}

	@Test
	public void math290() throws Exception {
		String root = rootNPEDataset + "math-290/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};

		runProject("math290", source, test, deps);
	}

	@Test
	public void math305() throws Exception {
		String root = rootNPEDataset + "math-305/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};

		runProject("math305", source, test, deps);
	}

	@Test
	public void math369() throws Exception {
		String root = rootNPEDataset + "math-369/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};

		runProject("math369", source, test, deps);
	}

	@Test
	public void math988a() throws Exception {
		String root = rootNPEDataset + "math-988a/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};

		runProject("math988a", source, test, deps);
	}

	@Test
	public void math988b() throws Exception {
		String root = rootNPEDataset + "math-988b/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};

		runProject("math988b", source, test, deps);
	}

	@Test
	public void math1115() throws Exception {
		String root = rootNPEDataset + "math-1115/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};

		runProject("math1115", source, test, deps);
	}

	@Test
	public void math1117() throws Exception {
		String root = rootNPEDataset + "math-1117/";
		String source = root + "src";
		String test = root + "test";
		String[] deps = new String[]{
				"junit/junit/4.7/junit-4.7.jar"
		};

		runProject("math1117", source, test, deps);
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
		runProject("pdfbox-2995", source, test, deps);
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
		runProject("pdfbox-2965", source, test, deps);
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
		runProject("pdfbox-2812", source, test, deps);
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
				"junit/junit/4.0/junit-4.0.jar",
				"org/easymock/easymock/2.4/easymock-2.4.jar"
		};
		runProject("felix-4960", source, test, deps);
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
		runProject("sling-4982", source, test, deps);
	}
}
