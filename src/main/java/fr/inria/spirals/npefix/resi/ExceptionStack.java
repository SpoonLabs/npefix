package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.resi.context.TryContext;

import java.util.ArrayList;
import java.util.List;

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
			//System.err.println("oops?");
		}
	}

	public static boolean isStoppable(Class<? extends Exception> c){
		for (TryContext tryContext : tryContexts) {
			if (tryContext == null) {
				continue;
			}
			for (Class clazz : tryContext.getTypes()) {
				if(clazz.isAssignableFrom(c)){
					return true;
				}
			}
		}
		return false;
	}
}
