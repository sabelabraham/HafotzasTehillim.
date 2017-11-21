package org.hafotzastehillim.fx.notes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.hafotzastehillim.fx.Model;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXButton;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
		ObservableList<Note> items = FXCollections.observableArrayList(NoteManager.getInstance().dueAlarms().stream()
				.filter(n -> !n.isHidden()).sorted().collect(Collectors.toList()));
		items.forEach(n -> n.hiddenProperty().addListener((obs, ov, nv) -> {
			if(nv)
				items.remove(n);
		}));
		
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
			Util.showErrorDialog(e);
			return;
		}

		getStyleClass().add("notification-list");
		getStylesheets().add(getClass().getResource("/resources/css/notification-list.css").toExternalForm());
	}
	
	@FXML
	public void clearAllAction(ActionEvent evt) {
		new ArrayList<>(list.getItems()).forEach(n -> n.setHidden(true));
	}
}
