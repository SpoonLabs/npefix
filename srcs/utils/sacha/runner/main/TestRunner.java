package sacha.runner.main;

import java.io.PrintStream;
import java.text.NumberFormat;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import sacha.interfaces.ITestResult;
import sacha.runner.utils.TestInfo;


public class TestRunner {
	
	/**
	 * can be set instead of testFolder
	 */
	public Class<?>[] classesArray = null;

	public TestRunner(Class<?>[] classesArray) {
		this.classesArray = classesArray;
	}

	private TestInfo runnedTests = new TestInfo();

	public ITestResult run() {
		JUnitCore runner = new JUnitCore();
//		MyTextListener listener = new MyTextListener(System.out);
//
//		runner.addListener(listener);
		runner.addListener(new RunListener(){
			public void testStarted(Description description) {
				runnedTests.add(description.getClassName(), description.getMethodName());
			}
			public void testIgnored(Description description)
					throws Exception {
				runnedTests.add(description.getClassName(), description.getMethodName());
			}
			@Override
			public void testFailure(Failure failure) throws Exception {
				super.testFailure(failure);
					System.err.println(failure.getTestHeader()+" -> "+failure.getTrace());
			}
			
			public void testRunFinished(Result result) {
				runnedTests.setResult(result);
			}
		});
		Result result = runner.run(classesArray);
//		System.out.println("IGNORED ("+result.getIgnoreCount()+" test"+(result.getIgnoreCount()>1?"s":"")+")\n");
//		
//		System.out.println("///////////////////////////  results  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");

		return runnedTests;
	}

	private class MyTextListener extends RunListener{
		private final PrintStream fWriter;
		
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
			}
			fWriter.println();
		}
		@Override
		public void testIgnored(Description description) throws Exception {
			fWriter.println("ignored : "+description.getClassName()+"#"+description.getMethodName());
		}
		
	}
}
