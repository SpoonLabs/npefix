package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.MethodContext;
import fr.inria.spirals.npefix.resi.context.instance.Instance;
import fr.inria.spirals.npefix.resi.exception.ForceReturn;
import fr.inria.spirals.npefix.resi.exception.NoMoreDecision;
import fr.inria.spirals.npefix.resi.selector.DomSelector;
import fr.inria.spirals.npefix.resi.selector.GreedySelector;
import fr.inria.spirals.npefix.resi.selector.Selector;
import fr.inria.spirals.npefix.resi.strategies.AbstractStrategy;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

@SuppressWarnings("all")
public class CallChecker {

	public static Selector strategySelector;

	static {
		try {
			System.out.print(String.format("RMI %s (Host: %s, Port: %d): ",
					Config.CONFIG.getServerName(),
					Config.CONFIG.getServerHost(),
					Config.CONFIG.getServerPort()));

			Registry registry = LocateRegistry.getRegistry(Config.CONFIG.getServerHost(), Config.CONFIG.getServerPort());

			strategySelector =  (Selector) registry.lookup(Config.CONFIG.getServerName());
			System.out.println("OK");
		} catch (Exception e) {
			strategySelector = new GreedySelector();
			System.out.println("KO");
		}
	}

	private static Stack<MethodContext> stack = new Stack<>();

	private static boolean isEnable = true;
	private static Strategy strategyBackup;
	private static Selector selectorBackup;

	private static Lapse lastLapse;

	public static Location currentLocation;
	public static ClassLoader currentClassLoader = CallChecker.class.getClassLoader();

	public static void clear() {
		strategyBackup = null;
		selectorBackup = null;
		stack = new Stack<>();
		isEnable = true;
		decisions.clear();
		cache.clear();
	}

	public static <T> Decision<T> getDecision(List<Decision<T>> decisions) {
		try {
			return strategySelector.select(decisions);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static <T> List<Decision<T>> getSearchSpace(Strategy.ACTION action, Object value, Class clazz, Location location, MethodContext context) {
		List<Decision<T>> output = new ArrayList<>();
		List<Strategy> strategies = null;
		try {
			strategies = strategySelector.getStrategies();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		disable();
		for (int i = 0; i < strategies.size(); i++) {
			Strategy strategy = strategies.get(i);
			try {
				output.addAll(strategy.getSearchSpace(value, clazz, location, context));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		enable();
		return output;
	}

	public static  Map<Location, List<Decision>> cache = new HashMap<>();
	public static  Map<Location, Decision> decisions = new HashMap<>();

	private static <T> T called(Strategy.ACTION action, T o, Class clazz, int line, int sourceStart, int sourceEnd) {
		Location location = getLocation(line, sourceStart, sourceEnd);
		if (o != null) {
			return action == Strategy.ACTION.beforeDeref? (T) Boolean.TRUE : o;
		}

		Lapse currentLapse;
		try {
			currentLapse = getCurrentLapse();
			if (currentLapse == null) {
				return action == Strategy.ACTION.beforeDeref? (T) Boolean.TRUE : o;
			}
			if (!currentLapse.equals(lastLapse)) {
				decisions.clear();
				enable();
			}
			lastLapse = currentLapse;
		} catch (RemoteException e) {
			e.printStackTrace();
			return o;
		}
		if(decisions.containsKey(location)) {
			Decision decision = decisions.get(location);
			if(!decision.getStrategy().isCompatibleAction(action)) {
				return o;
			}
			//System.out.println("Stack size: " + stack.size());
			//System.out.println("Nb method calls" + MethodContext.idCount);
			decision.increaseNbUse();
			decision.setUsed(true);
			enable();
			try {
				currentLapse.addApplication(decision);
				strategySelector.updateCurrentLapse(currentLapse);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(decision.getStrategy() instanceof Strat4) {
				throw new ForceReturn(decision);
			}
			return (T) decision.getValue();
		}
		if (!isEnable()) {
			return action == Strategy.ACTION.beforeDeref? (T) Boolean.TRUE : o;
		}
		if (!Config.CONFIG.isMultiPoints()) {
			Collection<Decision> usedDecisions = decisions.values();
			for (Iterator<Decision> iterator = usedDecisions.iterator(); iterator.hasNext(); ) {
				Decision decision = iterator.next();
				if (decision.isUsed()) {
					return action == Strategy.ACTION.beforeDeref? (T) Boolean.TRUE : o;
				}
			}
		}

		List<Decision<T>> searchSpace = new ArrayList<>();
		if(false && cache.containsKey(location) && !Config.CONFIG.getServerName().equals("Regression")) {
			List<Decision> decisions = cache.get(location);
			for (int i = 0; i < decisions.size(); i++) {
				Decision<T> decision =  decisions.get(i);
				searchSpace.add(decision);
			}
		} else {
			searchSpace = getSearchSpace(action, o, clazz, location, getCurrentMethodContext());
			cache.put(location, new ArrayList<Decision>(searchSpace));
		}

		if(searchSpace.isEmpty()) {
			return o;
		}

		Decision<?> decision = getDecision(searchSpace);

		try {
			currentLapse.addDecision(decision);
			strategySelector.updateCurrentLapse(currentLapse);
		} catch (Exception e) {
			e.printStackTrace();
		}

		decisions.put(location, decision);

		if(!decision.getStrategy().isCompatibleAction(action)) {
			disable();
			return o;
		}
		//System.out.println("Stack size: " + stack.size());
		//System.out.println("Nb method calls: " + MethodContext.idCount);
		try {
			currentLapse.addApplication(decision);
			strategySelector.updateCurrentLapse(currentLapse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		decision.increaseNbUse();
		decision.setUsed(true);

		if(decision.getStrategy() instanceof Strat4) {
			throw new ForceReturn(decision);
		}

		return (T) decision.getValue();
	}


	public static <T, E extends RuntimeException> T isToCatch(E throwable, Class<T> methodType){
		Lapse currentLapse;
		try {
			currentLapse = getCurrentLapse();
			if (currentLapse == null) {
				throw throwable;
			}
			if (!currentLapse.equals(lastLapse)) {
				decisions.clear();
				enable();
			}
			lastLapse = currentLapse;
		} catch (RemoteException e) {
			e.printStackTrace();
			throw throwable;
		}
		if(decisions.containsKey(getCurrentMethodContext().getLocation())) {
			Decision decision = decisions.get(getCurrentMethodContext().getLocation());
			//System.out.println("Stack size: " + stack.size());
			//System.out.println("Nb method calls" + MethodContext.idCount);
			decision.increaseNbUse();
			decision.setUsed(true);
			enable();
			try {
				currentLapse.addApplication(decision);
				strategySelector.updateCurrentLapse(currentLapse);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return (T) decision.getValue();
		}

		if (!isEnable()) {
			throw throwable;
		}




		if (!Config.CONFIG.isMultiPoints()) {
			Collection<Decision> usedDecisions = decisions.values();
			for (Iterator<Decision> iterator = usedDecisions.iterator(); iterator.hasNext(); ) {
				Decision decision = iterator.next();
				if (decision.isUsed()) {
					throw throwable;
				}
			}
		}

		List<Decision<T>> searchSpace = new ArrayList<>();
		if(false && cache.containsKey(getCurrentMethodContext().getLocation()) && !Config.CONFIG.getServerName().equals("Regression")) {
			List<Decision> decisions = cache.get(getCurrentMethodContext().getLocation());
			for (int i = 0; i < decisions.size(); i++) {
				Decision<T> decision =  decisions.get(i);
				searchSpace.add(decision);
			}
		} else {
			for (MethodContext context : stack) {
				List<Decision<T>> space = getSearchSpace(Strategy.ACTION.tryRepair, null, context.getMethodType(), context.getLocation(), context);
				searchSpace.addAll(space);
			}
			cache.put(getCurrentMethodContext().getLocation(), new ArrayList<Decision>(searchSpace));
		}

		if(searchSpace.isEmpty()) {
			throw new NoMoreDecision();
		}

		Decision<?> decision = getDecision(searchSpace);

		decisions.put(decision.getLocation(), decision);


		if (!decision.getLocation().equals(getCurrentMethodContext().getLocation())) {
			throw throwable;
		}

		//System.out.println("Stack size: " + stack.size());
		//System.out.println("Nb method calls: " + MethodContext.idCount);
		try {
			currentLapse.addApplication(decision);
			strategySelector.updateCurrentLapse(currentLapse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		decision.increaseNbUse();
		decision.setUsed(true);

		return (T) decision.getValue();
	}

	private static Lapse getCurrentLapse() throws RemoteException {
		if (isEnable) {
			return strategySelector.getCurrentLapse();
		}
		return selectorBackup.getCurrentLapse();
	}

	public static <T> T beforeCalled(T o, Class clazz, int line, int sourceStart, int sourceEnd) {
		return called(Strategy.ACTION.beforeCalled, o, clazz, line, sourceStart, sourceEnd);
	}

	public static <T> T isCalled(T o, Class clazz, int line, int sourceStart, int sourceEnd) {
		return called(Strategy.ACTION.isCalled, o, clazz, line, sourceStart, sourceEnd);
	}

	public static boolean beforeDeref(Object o, Class clazz, int line, int sourceStart, int sourceEnd) {
		Object called = called(Strategy.ACTION.beforeDeref, o, clazz, line,
				sourceStart, sourceEnd);
		if(called == null) {
			return true;
		}
		if(called instanceof Boolean) {
			return called.equals(Boolean.TRUE);
		}
		return true;
	}

	public static <T> T init(Class clazz) {
		if(!isEnable()) {
			if(clazz.isPrimitive()) {
				List<Instance<T>> instances = AbstractStrategy.<T>initPrimitive(clazz);
				return (T) instances.get(0).getValue();
			}
			return null;
		}
		disable();
		try {
			return (T) AbstractStrategy.initClass(clazz).getValue();
		} finally {
			enable();
		}
	}

	public static <T> T arrayAccess(Object array, int index, Class<T> type, int line, int sourceStart, int sourceEnd) {
		Location location = getLocation(line, sourceStart, sourceEnd);
		System.out.println(location);
		int size = Array.getLength(array);
		if (index >= 0 && size > index) {
			return (T) Array.get(array, index);
		}

		Lapse currentLapse;
		try {
			currentLapse = getCurrentLapse();
			if (currentLapse == null) {
				return (T) Array.get(array, index);
			}
			if (!currentLapse.equals(lastLapse)) {
				decisions.clear();
				enable();
			}
			lastLapse = currentLapse;
		} catch (RemoteException e) {
			e.printStackTrace();
			return (T) Array.get(array, index);
		}
		if(decisions.containsKey(location)) {
			Decision decision = decisions.get(location);
			decision.increaseNbUse();
			decision.setUsed(true);
			enable();
			try {
				currentLapse.addApplication(decision);
				strategySelector.updateCurrentLapse(currentLapse);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(decision.getStrategy() instanceof Strat4) {
				throw new ForceReturn(decision);
			}
			return (T) decision.getValue();
		}
		if (!isEnable()) {
			return (T) Array.get(array, index);
		}
		if (!Config.CONFIG.isMultiPoints()) {
			Collection<Decision> usedDecisions = decisions.values();
			for (Iterator<Decision> iterator = usedDecisions.iterator(); iterator.hasNext(); ) {
				Decision decision = iterator.next();
				if (decision.isUsed()) {
					return (T) Array.get(array, index);
				}
			}
		}

		List<Decision<T>> searchSpace = new ArrayList<>();
		if(false && cache.containsKey(location) && !Config.CONFIG.getServerName().equals("Regression")) {
			List<Decision> decisions = cache.get(location);
			for (int i = 0; i < decisions.size(); i++) {
				Decision<T> decision =  decisions.get(i);
				searchSpace.add(decision);
			}
		} else {
			searchSpace = getSearchSpace(Strategy.ACTION.arrayAccess, array, type, location, getCurrentMethodContext());
			cache.put(location, new ArrayList<Decision>(searchSpace));
		}

		if(searchSpace.isEmpty()) {
			return (T) Array.get(array, index);
		}

		Decision<?> decision = getDecision(searchSpace);

		try {
			currentLapse.addDecision(decision);
			strategySelector.updateCurrentLapse(currentLapse);
		} catch (Exception e) {
			e.printStackTrace();
		}

		decisions.put(location, decision);

		if(!decision.getStrategy().isCompatibleAction(Strategy.ACTION.arrayAccess)) {
			disable();
			return (T) Array.get(array, index);
		}
		try {
			currentLapse.addApplication(decision);
			strategySelector.updateCurrentLapse(currentLapse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		decision.increaseNbUse();
		decision.setUsed(true);

		if(decision.getStrategy() instanceof Strat4) {
			throw new ForceReturn(decision);
		}

		return (T) decision.getValue();
	}
	/*public static <T> T returned(Class clazz) {
		Strategy strat = currentLaps.getMainDecision();
		if(strat == null) {
			strat = getStrat();
			currentLaps.setMainDecision(strat);
		}
		disable();
		try {
			return (T) strat.returned(clazz);
		} catch (Throwable e) {
			throw e;
		} finally {
			enable();
		}
	}*/

	public static <T> T varAssign(Object table, String variableName, int line, int sourceStart, int sourceEnd) {
		addObjectInStack(variableName, table);
		return (T) table;
	}

	public static <T> T varInit(Object table, String variableName, int line, int sourceStart, int sourceEnd) {
		addObjectInStack(variableName, table);
		return (T) table;
	}

	public synchronized static void enable() {
		if(isEnable()) {
			return;
		}
		CallChecker.strategySelector = selectorBackup;
		DomSelector.strategy = strategyBackup;
		isEnable = true;
	}

	public synchronized static void disable() {
		if(!isEnable()) {
			return;
		}
		selectorBackup = CallChecker.strategySelector;
		strategyBackup = DomSelector.strategy;
		CallChecker.strategySelector = new DomSelector();
		DomSelector.strategy = new NoStrat();
		isEnable = false;
	}

	public static boolean isEnable() {
		return isEnable;
	}

	private static void addObjectInStack(String variableName, Object table) {
		if (isEnable()
				&& !(CallChecker.strategySelector instanceof DomSelector
				&& DomSelector.strategy instanceof NoStrat) && !stack.isEmpty()) {
			disable();
			try {
				stack.peek().addVariable(variableName, table);
			} catch (Throwable e) {
				// ignore
			} finally {
				enable();
			}
		}
	}

	public static void methodStart(MethodContext methodType) {
		stack.push(methodType);
	}

	public static void methodEnd(MethodContext methodContext) {
		stack.remove(methodContext);
	}

	public static MethodContext getCurrentMethodContext() {
		return stack.peek();
	}

	public static Stack<MethodContext> getStack() {
		return stack;
	}

	private static Location getLocation(int line, int sourceStart, int sourceEnd) {
		Thread thread = Thread.currentThread();
		StackTraceElement[] stackTraces = thread.getStackTrace();
		StackTraceElement stackTrace = stackTraces[4];
		return new Location(stackTrace.getClassName(), line, sourceStart, sourceEnd);
	}

	public static Map<Location, Decision> getDecisions() {
		return decisions;
	}
}
