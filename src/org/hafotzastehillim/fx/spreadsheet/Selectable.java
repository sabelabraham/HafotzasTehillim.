package org.hafotzastehillim.fx.spreadsheet;

import javafx.beans.property.Property;

public interface Selectable {

	default boolean isSelected() {
		return selectedProperty().getValue();
	}
	
	default void setSelected(boolean bool) {
		selectedProperty().setValue(bool);
	}
	
	Property<Boolean> selectedProperty();
}
