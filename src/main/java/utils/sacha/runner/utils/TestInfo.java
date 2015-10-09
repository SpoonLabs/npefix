package utils.sacha.runner.utils;

import org.junit.runner.Result;
import utils.sacha.interfaces.ITestResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class TestInfo extends HashMap<String, List<String>> implements ITestResult {

	private static final long serialVersionUID = 1L;

	private Result result;
	
	private String countRun;

	public TestInfo() {
		super();
	}

	public void add(String className, String methodName) {
		if (!containsKey(className))
			put(className, new ArrayList<String>());
		get(className).add(methodName);
	}

	public static void compare(TestInfo runnedTests, TestInfo importedTests) {
		System.out.println("runned : "+runnedTests.countRun+" / imported : "+importedTests.countRun);
//		int diff = runnedTests.count>importedTests.count?runnedTests.count-importedTests.count:importedTests.count-runnedTests.count;
//		if(diff>0)
//			System.err.println("number of tests diff : "+diff);
		Set<String> runnedClasses = runnedTests.keySet();
		Set<String> importedClasses = importedTests.keySet();
		int total=0;
		for (String string : importedClasses) {
			if(!runnedClasses.contains(string)){
				System.err.print("imported not runned : "+string);
				System.err.println(" number of tests : "+importedTests.get(string).size());
				total+=importedTests.get(string).size();
			}
		}
		if(total>0)
			System.err.println("\ttotal number : "+total);
		total = 0;
		for (String string : runnedClasses) {
			if(!importedClasses.contains(string)){
				System.err.print("runned not imported : "+string);
				System.err.println(" number of tests : "+runnedTests.get(string).size());
				total+=runnedTests.get(string).size();
			}
		}
		if(total>0)
			System.err.println("\ttotal number : "+total);
	}

	@Override
	public int getNbRunTests() {
		return result.getRunCount();
	}

	@Override
	public int getNbFailedTests() {
		return result.getFailureCount();
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public Result getResult() {
		return result;
	}
}
