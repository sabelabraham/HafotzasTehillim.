package org.hafotzastehillim.pointentry.spreadsheet;

import java.util.List;

import org.hafotzastehillim.pointentry.fx.util.Util;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;

public interface Selectable {

	default boolean isSelected() {
		return selectedProperty().getValue();
	}

	default void setSelected(boolean bool) {
		selectedProperty().setValue(bool);
	}

	Property<Boolean> selectedProperty();

	public static <T extends Selectable> void bindCheckbox(ObservableList<T> list, CheckBox check) {
		ObservableList<T> extract = Util.withExtractor(list, s -> new Observable[] { s.selectedProperty() });

		extract.addListener((ListChangeListener.Change<? extends T> change) -> {
			long selectCount = list.stream().filter(s -> s.isSelected()).count();
			if (selectCount == 0) {
				check.setSelected(false);
				check.setIndeterminate(false);
			} else if (selectCount == list.size()) {
				check.setSelected(true);
				check.setIndeterminate(false);
			} else {
				check.setSelected(false);
				check.setIndeterminate(true);
			}
		});

		check.setOnAction(evt -> {
			boolean selected = check.isSelected();
			list.forEach(entry -> entry.setSelected(selected));
		});
		
		check.disableProperty().bind(Bindings.isEmpty(list));
	}
}
