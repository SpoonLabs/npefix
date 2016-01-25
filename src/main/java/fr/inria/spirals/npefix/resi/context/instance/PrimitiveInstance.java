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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		PrimitiveInstance<?> that = (PrimitiveInstance<?>) o;

		if (value != null ? !value.equals(that.value) : that.value != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}
}
