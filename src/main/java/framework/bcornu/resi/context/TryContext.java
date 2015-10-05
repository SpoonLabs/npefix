package framework.bcornu.resi.context;

import framework.bcornu.resi.ExceptionStack;

public class TryContext {

	private int id = -1;
	private Class[] types;

	public TryContext(int id, String... types) {
		this.id=id;
		this.types = new Class[types.length];
		int i=0;
		for (String str : types) {
			try {
				this.types[i++]=Class.forName(str);
			} catch (ClassNotFoundException e) {
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
