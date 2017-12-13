package org.hafotzastehillim.pointentry.fx.cell;

public class EditorData<T> {

	private T value;
	private boolean changed;

	public EditorData(T value) {
		this.value = value;
	}

	public void setValue(T newValue) {
		if(value == newValue)
			return;

		value = newValue;
		changed = true;
	}

	public T getValue() {
		return value;
	}

	public boolean changed() {
		return changed;
	}

	public String toString() {
		return value == null ? "" : "" + value;
	}
}
