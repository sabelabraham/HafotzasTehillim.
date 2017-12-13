package org.hafotzastehillim.pointentry.fx.notes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.hafotzastehillim.pointentry.fx.Model;
import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.spreadsheet.Selectable;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;

public class NotesList extends StackPane {
	@FXML
	private ListView<Note> list;
	@FXML
	private Label noNotes;
	@FXML
	private CheckBox selectAll;
	@FXML
	private JFXButton delete;
	@FXML
	private JFXButton newNote;

	@FXML
	private void initialize() {
		ObservableList<Note> items = NoteManager.getInstance().notes().sorted();

		list.setItems(items);
		list.setCellFactory(lv -> new NoteCell());

		noNotes.visibleProperty().bind(Bindings.size(items).isEqualTo(0));

		selectAll.setId("selectAll");
		Selectable.bindCheckbox(items, selectAll);
		selectAll.indeterminateProperty().addListener(obs -> selectAll.requestFocus()); // bug fix for JFXCheckBox

		delete.disableProperty().bind(selectAll.selectedProperty().not().and(selectAll.indeterminateProperty().not()));

		newNote.setDisable(true);
	}

	public NotesList() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/NotesList.fxml"));
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			DialogUtils.showErrorDialog(e);
			return;
		}

		getStyleClass().add("notes-list");
		getStylesheets().add(getClass().getResource("/resources/css/notes-list.css").toExternalForm());
	}

	@FXML
	public void deleteAction(ActionEvent evt) {
		DialogUtils.createAlert(AlertType.CONFIRMATION, "Delete", "Are you sure?",
				"Are you sure you want to delete selected notes?", ButtonType.YES, ButtonType.NO)
				.filter(b -> b == ButtonType.YES).ifPresent(b -> new ArrayList<>(list.getItems()).forEach(n -> {
					if (n.isSelected()) {
						n.delete();
						n.setSelected(false);
					}
				}));
	}

	@FXML
	public void newNoteAction(ActionEvent evt) {
	}

	private static NotesList notesList;

	public static void show() {
		if (notesList == null) {
			notesList = new NotesList();
		}

		DialogUtils.createDialog(notesList, "Notes");
		
		// Persist selected changes
		
		notesList.list.getItems().forEach(n -> {
			if(n.isChanged())
				n.save();
		});
	}
}
