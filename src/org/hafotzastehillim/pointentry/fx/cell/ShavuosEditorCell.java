package org.hafotzastehillim.pointentry.fx.cell;

import org.hafotzastehillim.pointentry.fx.Main;

import javafx.application.Platform;
import javafx.beans.value.ObservableNumberValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;

public class ShavuosEditorCell extends ListCell<EditorData<Integer>> {

	@Override
	protected void updateItem(EditorData<Integer> item, boolean empty) {
		super.updateItem(item, empty);
		if (item == null || empty) {
			setGraphic(null);
			return;
		}

		TextField field = new TextField();
		field.setPrefColumnCount(7);

		String txt = item.toString();
		field.setText(txt.equals("0") ? "" : txt);
		field.setTextFormatter(new TextFormatter<Integer>(change -> {
			if (change.isContentChange())
				change.setText(change.getText().replaceAll("[^\\d]", ""));
			return change;
		}));
		field.textProperty().addListener((obs, ov, nv) -> {
			if (field.getText().isEmpty())
				item.setValue(0);
			else
				item.setValue(Integer.parseInt(field.getText()));
		});
		field.focusedProperty().addListener((obs, ov, nv) -> {
			if (nv)
				Platform.runLater(() -> field.selectAll());
		});

		HBox content = new HBox(10);
		content.getChildren().addAll(new Label("Shavuos " + (getIndex() + Main.FIRST_SHAVUOS_YEAR)), field);
		content.setAlignment(Pos.BASELINE_CENTER);
		setAlignment(Pos.BASELINE_CENTER);
		setGraphic(content);

	}

}
