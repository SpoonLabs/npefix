package fr.inria.spirals.npefix.resi.context.instance;

public class  PrimitiveInstance<T> implements Instance<T> {

	public T value;

	public PrimitiveInstance(T value) {
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "" + value;
	}
}
