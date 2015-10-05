package framework.bcornu.resi.context;

import framework.bcornu.resi.CallChecker;

public class MethodeContext {
	
	public MethodeContext() {
		CallChecker.methodStart();
	}

	public <T> T methodSkip() {
		// TODO Auto-generated method stub
		return null;
	}

	public void methodEnd() {
		CallChecker.methodEnd();
	}

}
