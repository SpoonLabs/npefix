package bcornu.resi;

import java.util.ArrayList;
import java.util.List;

import bcornu.resi.context.TryContext;

public class ExceptionStack {
	
	private static List<TryContext> tryContexts = new ArrayList<>();
	
	public static void register(TryContext tc) {
		tryContexts.add(tc);
	}
	
	public static void unregister(TryContext tc){
		if(tryContexts.isEmpty())
			return;
		if(tryContexts.get(tryContexts.size()-1).equals(tc)){
			tryContexts.remove(tryContexts.size()-1);
		}else{
			System.err.println("oops?");
		}
	}

	public static boolean isStoppable(Class<? extends Exception> c){
		for (TryContext tryContext : tryContexts) {
			for (Class clazz : tryContext.getTypes()) {
				if(clazz.isAssignableFrom(c)){
					return true;
				}
			}
		}
		return false;
	}
}
