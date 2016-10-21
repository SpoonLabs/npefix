package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.AbstractNPEDataset;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import org.junit.Assert;

import java.lang.reflect.Field;

/**
 * Abstract selector evaluation
 */
public abstract class AbstractSelectorEvaluation extends AbstractNPEDataset {

	private static final String rootNPEDataset = Config.CONFIG.getDatasetRoot();

	private Selector selector;

	protected int nbIteration = Config.CONFIG.getNbIteration();

	public AbstractSelectorEvaluation() {
	}


	@Override
	public void eval(NPEOutput results) {
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
		return results;
	}

	public void setSelector(
			Selector selector) {
		this.selector = selector;
	}



	public static String getClasspathProject(String projectName) {
		projectName = Character.toUpperCase(projectName.charAt(0)) + projectName.substring(1);
		Class<AbstractSelectorEvaluation> evaluationClass = AbstractSelectorEvaluation.class;
		try {
			Field field = evaluationClass.getField("classpath" + projectName.replace("-", ""));
			Object o = field.get(null);
			return (String) o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getSourcePathProject(String projectName) {
		if (AbstractNPEDataset.FELIX4960.equals(projectName)) {
			String root = rootNPEDataset + "felix-4960/";
			String source = root + "src/main/";
			String test = root + "src/test/";
			return source;
		}
		if (AbstractNPEDataset.SLING_4982.equals(projectName)) {
			String root = rootNPEDataset + "sling_4982/";
			String source = root + "src/main/";
			String test = root + "src/test/";
			return source;
		}
		if (AbstractNPEDataset.COLLECTIONS_360.equals(projectName)) {
			String root = rootNPEDataset + "collections-360/";
			String source = root + "src";
			String test = root + "test";
			return source;
		}
		if (AbstractNPEDataset.LANG_304.equals(projectName)) {
			String root = rootNPEDataset + "lang-304/";
			String source = root + "src";
			String test = root + "test";
			return source;
		}
		if (AbstractNPEDataset.LANG_587.equals(projectName)) {
			String root = rootNPEDataset + "lang-587/";
			String source = root + "src/main";
			String test = root + "src/test";
			return source;
		}
		if (AbstractNPEDataset.LANG_703.equals(projectName)) {
			String root = rootNPEDataset + "lang-703/";
			String source = root + "src";
			String test = root + "test";
			return source;
		}
		if (AbstractNPEDataset.MATH_290.equals(projectName)) {
			String root = rootNPEDataset + "math-290/";
			String source = root + "src";
			String test = root + "test";
			return source;
		}
		if (AbstractNPEDataset.MATH_305.equals(projectName)) {
			String root = rootNPEDataset + "math-305/";
			String source = root + "src";
			String test = root + "test";
			return source;
		}
		if (AbstractNPEDataset.MATH_369.equals(projectName)) {
			String root = rootNPEDataset + "math-369/";
			String source = root + "src";
			String test = root + "test";
			return source;
		}
		if (AbstractNPEDataset.MATH_988_A.equals(projectName)) {
			String root = rootNPEDataset + "math-988a/";
			String source = root + "src";
			String test = root + "test";
			return source;
		}
		if (AbstractNPEDataset.MATH_988_B.equals(projectName)) {
			String root = rootNPEDataset + "math-988b/";
			String source = root + "src";
			String test = root + "test";
			return source;
		}
		if (AbstractNPEDataset.MATH_1115.equals(projectName)) {
			String root = rootNPEDataset +  "math-1115/";
			String source = root + "src";
			String test = root + "test";
			return source;
		}
		if (AbstractNPEDataset.MATH_1117.equals(projectName)) {
			String root = rootNPEDataset +  "math-1117/";
			String source = root + "src";
			String test = root + "test";
			return source;
		}
		if (AbstractNPEDataset.PDFBOX_2995.equals(projectName)) {
			String root = rootNPEDataset + "pdfbox_2995/";
			String source = root + "src/main/";
			String test = root + "src/test/";
			return source;
		}
		if (AbstractNPEDataset.PDFBOX_2812.equals(projectName)) {
			String root = rootNPEDataset + "pdfbox_2812/";
			String source = root + "src/main/";
			String test = root + "src/test/";
			return source;
		}
		if (AbstractNPEDataset.PDFBOX_2965.equals(projectName)) {
			String root = rootNPEDataset + "pdfbox_2965/";
			String source = root + "src/main/";
			String test = root + "src/test/";
			return source;
		}
		throw new RuntimeException("Project not found: " + projectName);
	}
}
