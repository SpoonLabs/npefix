package framework.bcornu.resi;

import framework.bcornu.resi.strategies.NoStrat;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


@SuppressWarnings("all")
public class CallChecker {
		
	public static Strategy strat;
	
	private static Stack<List<Object>> stack = new Stack<List<Object>>();

	private static Strategy getStrat(){
		return strat==null?new NoStrat():strat;
	}
	
	public static <T> T isCalled(T o, Class clazz) {
		if (o == null && ExceptionStack.isStoppable(NullPointerException.class))
				return null;
		return getStrat().isCalled(o, clazz);
	}

	public static boolean beforeDeref(Object called) {
		if (called == null && ExceptionStack.isStoppable(NullPointerException.class))
			return true;
		return getStrat().beforeDeref(called);
	}

	public static <T> T init(Class clazz) {
		return getStrat().init(clazz);
	}

	public static <T> T returned(Class clazz) {
		return getStrat().returned(clazz);
	}

	public static <T> T varAssign(Object table) {
		if(! stack.isEmpty() && ! stack.peek().contains(table))
			stack.peek().add(table);
		return (T) table;
	}

	public static <T> T varInit(Object table) {
		if(! stack.isEmpty())
			stack.peek().add(table);
		return (T) table;
	}

	public static void methodStart() {
		stack.push(new ArrayList<Object>());
	}

	public static void methodEnd() {
		stack.pop();
	}
	
	public static List<Object> getCurrentVars(){
		return stack.peek();
	}

}
