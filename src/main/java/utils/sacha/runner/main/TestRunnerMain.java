package utils.sacha.runner.main;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.sacha.finder.main.TestMain;
import utils.sacha.runner.utils.TestInfo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintStream;
import java.text.NumberFormat;


public abstract class TestRunnerMain {
	/**
	 * have to be set in static{testFolder = "%path%";} on instances
	 */
	public static String testFolder = null;
	
	/**
	 * can be set instead of testFolder
	 */
	public static Class<?>[] classesArray = null;

	public static String eclipseTestReport = null;
	private static TestInfo runnedTests = new TestInfo();
	private static TestInfo importedTests = new TestInfo();

	public static void main(String[] args) {
		if(classesArray==null){
			if(testFolder==null)
				throw new IllegalArgumentException("must set the testFolder in static block");
			classesArray = TestMain.findTest(testFolder);
		}
		JUnitCore runner = new JUnitCore();
		
		MyTextListener listener = new MyTextListener(System.out);

		runner.addListener(listener);
		if(eclipseTestReport!=null)
			runner.addListener(new RunListener(){
				public void testStarted(Description description) {
					runnedTests.add(description.getClassName(), description.getMethodName());
				}
				public void testIgnored(Description description)
						throws Exception {
					runnedTests.add(description.getClassName(), description.getMethodName());
				}
			});
		Result result = runner.run(classesArray);
		System.out.println("IGNORED ("+result.getIgnoreCount()+" test"+(result.getIgnoreCount()>1?"s":"")+")\n");
		
		System.out.println("///////////////////////////  results  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
		if(eclipseTestReport!=null)
			compareTests(eclipseTestReport);

		System.exit(0);
	}

	private static void compareTests(String eclipseTestReport) {
		try {
			File fXmlFile = new File(eclipseTestReport);
			if (!fXmlFile.exists() || fXmlFile.isDirectory() || !fXmlFile.canRead())
				throw new IllegalArgumentException("cannot found " + eclipseTestReport + " or is not a file or is not readable");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("testcase");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					importedTests.add(eElement.getAttribute("classname"),eElement.getAttribute("name"));
				}
			}
			TestInfo.compare(runnedTests,importedTests);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static class MyTextListener extends RunListener{
		private final PrintStream fWriter;
		private int errors=0;
		private int failures=0;
		
		public MyTextListener(PrintStream writer) {
			this.fWriter= writer;
		}
		public void testRunStarted(Description description) throws Exception {
			fWriter.println("start tests\n/////////////////////////// tests out \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
		}
		public void testRunFinished(Result result) {
			fWriter.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\ tests out ///////////////////////////\nend tests");
			fWriter.println("Time: " + NumberFormat.getInstance().format((double) result.getRunTime() / 1000));
			if (result.wasSuccessful()) {
				fWriter.println();
				fWriter.print("OK");
				fWriter.println(" (" + result.getRunCount() + " test" + (result.getRunCount() == 1 ? "" : "s") + ")");
			} else {
				fWriter.println();
				fWriter.println("FAILURES!!!");
				fWriter.println("Tests run: " + result.getRunCount() + ",  Test failed: " + result.getFailureCount());
				fWriter.println("erros: " + errors + ",  failures: " + failures);
			}
			fWriter.println();
		}
		@Override
		public void testIgnored(Description description) throws Exception {
			fWriter.println("ignored : "+description.getClassName()+"#"+description.getMethodName());
		}
		
	}
}
