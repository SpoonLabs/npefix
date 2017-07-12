
public class Coneflower {

	private String nullString = null;

	public String methodThrowingNPE() {
		return nullString.toString();
	}

	public String intermediateMethod() {
		return "Brilliant coneflower," + methodThrowingNPE();
	}

	public String method() {
		return "Cutleaf coneflower," + intermediateMethod();
	}

}