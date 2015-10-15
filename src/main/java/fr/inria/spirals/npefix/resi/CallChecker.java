package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat3;

import java.util.*;


@SuppressWarnings("all")
public class CallChecker {
		
	public static Strategy strat = new NoStrat();
	
	private static Stack<Set<Object>> stack = new Stack<>();


	private static Strategy getStrat(){
		return strat;
	}

	public static <T> T beforeCalled(T o, Class clazz) {
		if (o == null && ExceptionStack.isStoppable(NullPointerException.class))
			return null;
		return (T) getStrat().beforeCalled(o, clazz);
	}

	public static <T> T isCalled(T o, Class clazz) {
		if (o == null && ExceptionStack.isStoppable(NullPointerException.class))
				return null;
		return (T) getStrat().isCalled(o, clazz);
	}

	public static boolean beforeDeref(Object called) {
		if (called == null && ExceptionStack.isStoppable(NullPointerException.class))
			return true;
		return getStrat().beforeDeref(called);
	}

	public static <T> T init(Class clazz) {
		return (T) getStrat().init(clazz);
	}

	public static <T> T returned(Class clazz) {
		return (T) getStrat().returned(clazz);
	}

	public static <T> T varAssign(Object table) {
		if(getStrat().collectData() && !stack.isEmpty())
			stack.peek().add(table);
		return (T) table;
	}
	
	public static <T> T varInit(Object table) {
		if(getStrat().collectData() && !stack.isEmpty())
			stack.peek().add(table);
		return (T) table;
	}

	public static void methodStart() {
		if(getStrat().collectData()) {
			return;
		}
		stack.push(new HashSet<Object>());
	}

	public static void methodEnd() {
		if(getStrat().collectData()) {
			return;
		}
		if(!stack.isEmpty())
			stack.pop();
	}
	
	public static Set<Object> getCurrentVars(){
		return stack.peek();
	}

	public static Stack<Set<Object>> getStack() {
		return stack;
	}
}
