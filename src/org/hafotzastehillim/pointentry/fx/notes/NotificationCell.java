package org.hafotzastehillim.pointentry.fx.notes;

import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXRippler;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;

public class NotificationCell extends ListCell<Note> {

	@Override
	public void updateItem(Note item, boolean empty) {
		super.updateItem(item, empty);
		
		if(empty)
			return;
		
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		NoteCellContent content = new NoteCellContent(getListView().getItems(), item, true, false, true);
		setGraphic(content);
	}
}
