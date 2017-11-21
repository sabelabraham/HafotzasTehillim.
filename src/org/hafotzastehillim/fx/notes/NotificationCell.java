package org.hafotzastehillim.fx.notes;

import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXRippler;

import javafx.scene.control.ContentDisplay;
import javafx.scene.paint.Color;

public class NotificationCell extends JFXListCell<Note> {

	@Override
	public void updateItem(Note item, boolean empty) {
		super.updateItem(item, empty);
		
		if(empty)
			return;
		
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		NoteCellContent content = new NoteCellContent(getListView().getItems(), item, true, false, false);
		setGraphic(content);
	}
}
