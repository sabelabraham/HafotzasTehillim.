package org.hafotzastehillim.pointentry.fx.notes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.hafotzastehillim.pointentry.fx.Model;
import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.fx.util.Util;

import com.jfoenix.controls.JFXButton;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;

public class NotificationList extends StackPane {
	@FXML
	private ListView<Note> list;
	@FXML
	private Label noNotes;
	@FXML
	private JFXButton clearAll;
	@FXML
	private ToggleButton silence;

	@FXML
	private void initialize() {
		ObservableList<Note> items = new FilteredList<>(
				Util.withExtractor(NoteManager.getInstance().dueAlarms().sorted(Comparator.comparing(n -> n.getCreated())),
						n -> new Observable[] { n.hiddenProperty() }),
				n -> !n.isHidden());

		list.setItems(items);
		list.setCellFactory(lv -> new NotificationCell());

		silence.selectedProperty().bindBidirectional(Model.getInstance().silenceAlarmsProperty());

		noNotes.visibleProperty().bind(Bindings.size(items).isEqualTo(0));
	}

	public NotificationList() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/NotificationList.fxml"));
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			DialogUtils.showErrorDialog(e);
			return;
		}

		getStyleClass().add("notification-list");
		getStylesheets().add(getClass().getResource("/resources/css/notification-list.css").toExternalForm());
	}

	@FXML
	public void clearAllAction(ActionEvent evt) {
		new ArrayList<>(list.getItems()).forEach(n -> n.setHidden(true));
	}

	private static NotificationList notificationList;

	public static void show() {
		if (notificationList == null) {
			notificationList = new NotificationList();
		}

		DialogUtils.createDialog(notificationList, "Notifications");
	}
}
