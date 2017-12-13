package org.hafotzastehillim.pointentry.fx.cell;

import org.hafotzastehillim.pointentry.spreadsheet.Entry;

import javafx.beans.value.ObservableNumberValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class EntryCell extends RoundEdgedListCell<Entry> {

	private Label name;
	private StackPane pane;

	public EntryCell(double radii, ObservableNumberValue itemSize) {
		super(itemSize);

		getStyleClass().add("entry-cell");

		name = new Label();
		name.setAlignment(Pos.BASELINE_RIGHT);
		name.getStyleClass().add("name-label");

		pane = new StackPane(name);
		pane.setMouseTransparent(true);

	}

	@Override
	public void updateItem(Entry entry, boolean selected) {
		super.updateItem(entry, selected);

		if (entry != null) {
			name.setText(entry.toString());
		} else {
			name.setText("");
		}

		setGraphic(pane);
	}

}
