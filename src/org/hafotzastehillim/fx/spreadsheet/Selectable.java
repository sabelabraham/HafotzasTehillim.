package org.hafotzastehillim.fx.spreadsheet;

import javafx.beans.property.Property;

public interface Selectable {

	boolean isSelected();
	
	void setSelected(Boolean bool);
	
	Property<Boolean> selectedProperty();
}
