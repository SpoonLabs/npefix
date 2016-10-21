package utils.sacha.runner.main;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import utils.sacha.interfaces.ITestResult;
import utils.sacha.runner.utils.TestInfo;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestRunner {

	private static Description currentTest;

	public TestRunner() {

	}

	private TestInfo runnedTests = new TestInfo();

	public ITestResult run(Class<?>[] classesArray) {
		JUnitCore runner = new JUnitCore();
		runner.addListener(new MethodRunListener());
		runner.run(classesArray);
		return runnedTests;
	}

	public ITestResult run(List<Method> methods) {
		JUnitCore runner = new JUnitCore();
		runner.addListener(new MethodRunListener());
		for (int i = 0; i < methods.size(); i++) {
			Method method = methods.get(i);
			Request request = Request.method(method.getDeclaringClass(), method.getName());
			Result result = runnedTests.getResult();
			runner.run(request);
			if(result != null) {
				runnedTests.getResult().getFailures().addAll(result.getFailures());
				try {
					Field fCount = result.getClass().getDeclaredField("fCount");
					fCount.setAccessible(true);
					AtomicInteger atomicInteger = (AtomicInteger) fCount.get(result);
					AtomicInteger atomicIntegerNewValue = (AtomicInteger) fCount.get(runnedTests.getResult());
					for (int j = 0; j < atomicInteger.intValue(); j++) {
						atomicIntegerNewValue.incrementAndGet();
					}
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				// runnedTests.getResult().
			}

		}
		return runnedTests;
	}

	public Result run(Request request) {
		JUnitCore runner = new JUnitCore();
		runner.addListener(new MethodRunListener());
		return runner.run(request);
	}

	public static Description getCurrentTest() {
		return currentTest;
	}

	private class MethodRunListener extends RunListener{
		public void testStarted(Description description) {
			runnedTests.add(description.getClassName(), description.getMethodName());
			currentTest = description;
		}
		public void testIgnored(Description description)
				throws Exception {
			runnedTests.add(description.getClassName(), description.getMethodName());
		}
		@Override
		public void testFailure(Failure failure) throws Exception {
			if(failure.getDescription() != null
					&& (failure.getDescription().getDisplayName().startsWith("warning") ||
					(failure.getMessage() != null && failure.getMessage().startsWith("No tests found")))) {
				return;
			}
			super.testFailure(failure);
			//System.err.println(failure.getTestHeader());
		}

		public void testRunFinished(Result result) {
			List<Failure> failures = new ArrayList<>(result.getFailures());
			for (int i = 0; i < failures.size(); i++) {
				Failure failure = failures.get(i);
				if(failure.getDescription() != null
						&& (failure.getDescription().getDisplayName().startsWith("warning") ||
						(failure.getMessage() != null && failure.getMessage().startsWith("No tests found")))) {
					result.getFailures().remove(failure);
				}
			}
			runnedTests.setResult(result);
		}
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
