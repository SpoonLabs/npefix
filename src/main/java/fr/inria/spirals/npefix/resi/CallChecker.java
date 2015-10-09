package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.resi.strategies.NoStrat;

import java.util.*;


@SuppressWarnings("all")
public class CallChecker {
		
	public static Strategy strat;
	
	private static Stack<Map<String, Object>> stack = new Stack<>();

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

	public static <T> T varAssign(String varName, Object table) {
		if(! stack.isEmpty())
			stack.peek().put(varName, table);
		return (T) table;
	}
	
	public static <T> T varInit(String varName, Object table) {
		if(! stack.isEmpty())
			stack.peek().put(varName, table);
		return (T) table;
	}

	public static void methodStart() {
		if(getStrat() instanceof NoStrat) {
			return;
		}
		stack.push(new HashMap<String, Object>());
	}

	public static void methodEnd() {
		if(getStrat() instanceof NoStrat) {
			return;
		}
		stack.pop();
	}
	
	public static Map<String, Object> getCurrentVars(){
		return stack.peek();
	}

}
