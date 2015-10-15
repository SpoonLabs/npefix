package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.AbnormalExecutionError;
import fr.inria.spirals.npefix.resi.ExceptionStack;
import fr.inria.spirals.npefix.resi.ForceReturn;
import fr.inria.spirals.npefix.resi.Strategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * a=new A
 * @author bcornu
 *
 */
public class Strat2B extends Strategy{

	@Override
	public <T> T beforeCalled(T o, Class<?> clazz) {
		if (o == null) {
			if (ExceptionStack.isStoppable(NullPointerException.class)) {
				return null;
			}
			if(clazz.equals(Class.class)) {
				return null;
			}

			T instance = initNotNull(clazz);
			if(instance != null) {
				o = instance;
			}
		}
		return o;
	}

	public <T> T isCalled(T o, Class<?> clazz) {
		return o;
	}

	public boolean beforeDeref(Object called) {
		return true;
	}


	@Override
	public <T> T returned(Class<?> clazz) {
		throw new AbnormalExecutionError("should not call return");
	}
}
