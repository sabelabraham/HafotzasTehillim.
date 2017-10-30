package org.hafotzastehillim.fx.cell;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;

public class GiftEditorCell extends ListCell<EditorData<Boolean>> {

	@Override
	protected void updateItem(EditorData<Boolean> item, boolean empty) {
		super.updateItem(item, empty);
		if (item == null || empty) {
			setGraphic(null);
			return;
		}

		CheckBox box = new CheckBox(" Gift for " + ((getIndex() + 1) * 100) + " points");
		box.setSelected(item.getValue());
		box.selectedProperty().addListener((obs, ov, nv) -> item.setValue(nv));

		setAlignment(Pos.BASELINE_CENTER);
		setGraphic(box);

	}

}
