package org.hafotzastehillim.pointentry.fx;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.fx.util.Util;
import org.hafotzastehillim.pointentry.spreadsheet.Column;
import org.hafotzastehillim.pointentry.spreadsheet.Entry;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;

public class GiftQueryController {
	@FXML
	private JFXSpinner spinner;
	@FXML
	private JFXButton button;

	@FXML
	public void initialize() {
		spinner.visibleProperty().bind(Model.getInstance().getSpreadsheet().searchService().runningProperty());
		DialogUtils.dialogButtonStyling(button);
	}

	@FXML
	public void run(ActionEvent event) {

		ObservableList<Entry> all = FXCollections.observableArrayList();
		Model.getInstance().getSpreadsheet().searchEntries("100", all,
				(q, v, col) -> !v.isEmpty() && Integer.parseInt(v) >= 100, Column.TOTAL_POINTS.ordinal());

		Model.getInstance().getSpreadsheet().searchService().setOnSucceeded(evt -> {
			Model.getInstance().getSpreadsheet().searchService().setOnSucceeded(null);

			int max = 0;
			for (Entry e : all) {
				max = Math.max(e.getTotal() / 100, max);
			}

			Map<Integer, ObservableList<Entry>> map = new LinkedHashMap<>();

			for (int i = 0; i < max; i++) {
				ObservableList<Entry> sub = FXCollections.observableArrayList();
				for (Entry e : all) {
					if (e.isEligibleForGift(i))
						sub.add(e);
				}

				map.put(i, sub);
			}

			GiftResultsView report = new GiftResultsView(map);
			Platform.runLater(() -> DialogUtils.createDialog(report, "Gift Reports", ButtonType.OK));
		});

	}
}