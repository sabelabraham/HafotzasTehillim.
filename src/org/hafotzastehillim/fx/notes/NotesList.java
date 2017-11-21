package org.hafotzastehillim.fx.notes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.hafotzastehillim.fx.Model;
import org.hafotzastehillim.fx.util.Util;

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

		delete.disableProperty().bind(new BooleanBinding() {

			{
				items.forEach(i -> bind(i.selectedProperty()));

			}

			@Override
			protected boolean computeValue() {
				long selectCount = items.stream().filter(entry -> entry.isSelected()).count();
				if (selectCount == 0) {
					selectAll.setSelected(false);
					selectAll.setIndeterminate(false);
				} else if (selectCount == items.size()) {
					selectAll.setSelected(true);
					selectAll.setIndeterminate(false);
				} else {
					selectAll.setSelected(false);
					selectAll.setIndeterminate(true);
				}

				return items.stream().noneMatch(item -> item.isSelected());
			}

		});

		selectAll.setId("selectAll");
		selectAll.setOnAction(evt -> {
			boolean selected = selectAll.isSelected();
			items.forEach(i -> i.setSelected(selected));
		});

		newNote.setDisable(true);
	}

	public NotesList() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/NotesList.fxml"));
			loader.setController(this);
			loader.setRoot(this);
			loader.load();
		} catch (IOException e) {
			Util.showErrorDialog(e);
			return;
		}

		getStyleClass().add("notes-list");
		getStylesheets().add(getClass().getResource("/resources/css/notes-list.css").toExternalForm());
	}

	@FXML
	public void deleteAction(ActionEvent evt) {
		Util.createAlert(AlertType.CONFIRMATION, "Delete", "Are you sure?",
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

	private static int scrollLocation;

	@SuppressWarnings("restriction")
	public static void show() {
		NotesList noteList = new NotesList();
		noteList.list.scrollTo(scrollLocation);

		Util.createDialog(noteList, "Notes");

		ListViewSkin<?> ts = (ListViewSkin<?>) noteList.list.getSkin();
		VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
		scrollLocation = vf.getFirstVisibleCellWithinViewPort().getIndex();

	}
}
