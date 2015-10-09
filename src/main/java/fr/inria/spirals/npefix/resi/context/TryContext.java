package fr.inria.spirals.npefix.resi.context;

import fr.inria.spirals.npefix.resi.ExceptionStack;

public class TryContext {

	private int id = -1;
	private Class<?>[] types;
	private Class<?> context;

	public TryContext(int id, Class<?> context, String... types) {
		this.id=id;
		this.types = new Class[types.length];
		this.context = context;
		int i=0;
		ClassLoader classLoader = context.getClassLoader();
		for (String str : types) {
			try {
				this.types[i++] = classLoader.loadClass(str);
			} catch (ClassNotFoundException e) {
				System.out.println(e);
				//throw new RuntimeException(e);
			}
		}
		ExceptionStack.register(this);
	}

	public void catchStart(int i) {
		ExceptionStack.unregister(this);
	}

	public void finallyStart(int i) {
		ExceptionStack.unregister(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof TryContext))return false;
		return this.id == ((TryContext)o).id;
	}

	public Class[] getTypes() {
		return types;
	}

}
