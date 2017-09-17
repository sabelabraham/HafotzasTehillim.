package org.hafotzastehillim.fx;

class PointEditorData {

	private int value;
	private boolean changed;

	public PointEditorData(int value) {
		this.value = value;
	}

	public void setValue(int newValue) {
		if(value == newValue)
			return;

		value = newValue;
		changed = true;
	}

	public int getValue() {
		return value;
	}

	public boolean changed() {
		return changed;
	}

	public String toString() {
		return value == 0 ? "" : "" + value;
	}
}
