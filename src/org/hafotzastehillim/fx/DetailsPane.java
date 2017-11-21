package org.hafotzastehillim.fx;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.hafotzastehillim.fx.cell.GiftEditorCell;
import org.hafotzastehillim.fx.cell.PointEditorCell;
import org.hafotzastehillim.fx.cell.ShavuosEditorCell;
import org.hafotzastehillim.fx.notes.Note;
import org.hafotzastehillim.fx.notes.NoteManager;
import org.hafotzastehillim.fx.cell.EditorData;
import org.hafotzastehillim.fx.spreadsheet.Entry;
import org.hafotzastehillim.fx.spreadsheet.FamilyGrouping;
import org.hafotzastehillim.fx.util.NoSelectionModel;
import org.hafotzastehillim.fx.util.Util;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DetailsPane extends VBox {

	private FormController controller;
	private ObservableList<EditorData<Integer>> pointData;
	private ObservableList<EditorData<Integer>> shavuosData;
	private ObservableList<EditorData<Boolean>> giftsData;

	public DetailsPane(Entry entry) {
		getStyleClass().add("details-pane");

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/Form.fxml"));
		GridPane form = null;
		try {
			form = loader.load();
		} catch (IOException e) {
			Util.showErrorDialog(e);
			return;
		}

		form.getStyleClass().add("form");
		form.getStylesheets().add(getClass().getResource("/resources/css/form.css").toExternalForm());

		getStyleClass().add("details");
		getStylesheets().add(getClass().getResource("/resources/css/details.css").toExternalForm());

		controller = loader.getController();
		controller.setEntry(entry);

		getChildren().add(form);

		pointData = FXCollections.observableArrayList();
		ListView<EditorData<Integer>> pointsList = new ListView<>();
		pointsList.setCellFactory(view -> new PointEditorCell());
		pointsList.setItems(pointData);
		pointsList.setSelectionModel(new NoSelectionModel<EditorData<Integer>>());
		pointsList.setPrefHeight(200);
		pointsList.setId("points-list");

		if (entry != null) {
			EditorData<Integer> last = null;
			for (int point : entry.getPoints())
				pointData.add(last = new EditorData<>(point));

			if (last != null)
				pointsList.scrollTo(last);
		}

		Label pointsLabel = new Label("Weekly Program");
		JFXButton pointsAdd = new JFXButton("Add");
		pointsAdd.setId("points-add");
		pointsAdd.setOnAction(evt -> {
			for (int i = pointData.size(); i < Model.getInstance().getCampaignIndex() - 1; i++)
				pointData.add(new EditorData<>(0));

			EditorData<Integer> last = new EditorData<>(0);
			pointData.add(last);

			pointsList.scrollTo(last);
		});

		VBox pointsPane = new VBox(10);
		HBox pointsHeader = new HBox(15);
		pointsHeader.getChildren().addAll(pointsLabel, pointsAdd);
		pointsHeader.setPadding(new Insets(10, 10, 0, 10));
		pointsHeader.setAlignment(Pos.BASELINE_CENTER);

		Label total = new Label();
		total.setPadding(new Insets(0, 0, 10, 0));
		if (entry != null)
			total.setText("Total Points: " + entry.getTotal());

		pointsPane.getChildren().addAll(pointsHeader, pointsList, new StackPane(total));
		pointsPane.setId("points-pane");

		// -----------------

		shavuosData = FXCollections.observableArrayList();
		ListView<EditorData<Integer>> shavuosList = new ListView<>();
		shavuosList.setCellFactory(view -> new ShavuosEditorCell());
		shavuosList.setItems(shavuosData);
		shavuosList.setSelectionModel(new NoSelectionModel<EditorData<Integer>>());
		shavuosList.setPrefHeight(200);
		shavuosList.setId("shavuos-list");

		if (entry != null) {
			EditorData<Integer> last = null;
			for (int data : entry.getShavuosData())
				shavuosData.add(last = new EditorData<>(data));

			if (last != null)
				shavuosList.scrollTo(last);
		}

		Label shavuosLabel = new Label("Shavuos Program");
		JFXButton shavuosAdd = new JFXButton("Add");
		shavuosAdd.setId("shavuos-add");
		shavuosAdd.setOnAction(evt -> {
			EditorData<Integer> last = new EditorData<>(0);
			shavuosData.add(last);

			shavuosList.scrollTo(last);
		});

		VBox shavuosPane = new VBox(10);
		HBox shavuosHeader = new HBox(15);
		shavuosHeader.getChildren().addAll(shavuosLabel, shavuosAdd);
		shavuosHeader.setPadding(new Insets(10, 10, 0, 10));
		shavuosHeader.setAlignment(Pos.BASELINE_CENTER);

		shavuosPane.getChildren().addAll(shavuosHeader, shavuosList);
		shavuosPane.setId("shavuos-pane");

		// -----------------

		giftsData = FXCollections.observableArrayList();
		ListView<EditorData<Boolean>> giftsList = new ListView<>();
		giftsList.setCellFactory(view -> new GiftEditorCell());
		giftsList.setItems(giftsData);
		giftsList.setSelectionModel(new NoSelectionModel<EditorData<Boolean>>());
		giftsList.setPrefHeight(200);
		giftsList.setId("gifts-list");

		if (entry != null) {
			EditorData<Boolean> last = null;
			for (boolean data : entry.getGiftsReceived())
				giftsData.add(last = new EditorData<>(data));

			int eligable = entry.getTotal() / 100;
			while (giftsData.size() < eligable) {
				giftsData.add(last = new EditorData<>(false));
			}

			if (last != null)
				giftsList.scrollTo(last);
		}

		Label giftsLabel = new Label("Gifts");
		giftsLabel.setPadding(new Insets(4));

		VBox giftsPane = new VBox(10);
		HBox giftsHeader = new HBox(15);
		giftsHeader.getChildren().addAll(giftsLabel);
		giftsHeader.setPadding(new Insets(10, 10, 0, 10));
		giftsHeader.setAlignment(Pos.BASELINE_CENTER);

		giftsPane.getChildren().addAll(giftsHeader, giftsList);
		giftsPane.setId("gifts-pane");

		// -----------------

		HBox allLists = new HBox(pointsPane, shavuosPane);
		if (entry != null) {
			allLists.getChildren().add(giftsPane);
		}
		allLists.setSpacing(10);
		allLists.setPadding(new Insets(0, 40, 20, 40));
		allLists.setAlignment(Pos.CENTER);

		getChildren().addAll(allLists);
		setAlignment(Pos.CENTER);
	}

	public FormController getController() {
		return controller;
	}

	public static void showNewDialog() {
		DetailsPane pane = new DetailsPane(null);
		boolean success = Util.createDialog(pane, "New Member",
				pane.getController().phone.textProperty().isEmpty()
						.or(pane.getController().cityYiddish.getEditor().textProperty().isEmpty()),
				ButtonType.CANCEL, ButtonType.OK).filter(b -> b == ButtonType.OK).isPresent();

		FormController form = pane.getController();

		if (success) {
			Entry e = form.getEntry();

			e.persist(row -> {
				for (int i = 0; i < pane.pointData.size(); i++) {
					if (pane.pointData.get(i).changed()) {
						e.putPoint(i, pane.pointData.get(i).getValue());
					}
				}
				for (int i = 0; i < pane.shavuosData.size(); i++) {
					if (pane.shavuosData.get(i).changed()) {
						e.putShavuosData(i, pane.shavuosData.get(i).getValue());
					}
				}
				for (int i = 0; i < pane.giftsData.size(); i++) {
					if (pane.giftsData.get(i).changed()) {
						e.putGiftReceived(i, pane.giftsData.get(i).getValue());
					}
				}
			});

			Note n = NoteManager.getInstance().getNote(e.getPhone());
			if (n != null) {
				if (n.getNote().isEmpty()) {
					n.delete();
				} else if (n.isChanged()) {
					n.save();
				}
			} else if (!form.notes.getText().isEmpty()) {
				Note.withNumberAndAlarm(e.getPhone(), form.getAlarm(), form.notes.getText());
			}
		} else {
			Note n = NoteManager.getInstance().getNote(form.phone.getText());
			if (n != null && n.isChanged()) {
				n.reload();
			}
		}
	}

	public static void showDialog(Entry entry) {
		showDialog(Arrays.asList(entry));
	}

	public static void showDialog(List<Entry> entries) {
		if (entries.size() == 0) {
			showNewDialog();
			return;
		}

		FamilyGrouping group = new FamilyGrouping(entries);
		StringProperty noteString = new SimpleStringProperty("");
		ObjectProperty<Instant> alarm = new SimpleObjectProperty<>();

		JFXTabPane family = new JFXTabPane();
		family.getStylesheets().add(DetailsPane.class.getResource("/resources/css/details-tab-pane.css").toExternalForm());
		
		DetailsPane detailsPane = null;

		for (Entry e : entries) {
			Tab t = new Tab();
			t.textProperty().bind(e.firstNameYiddishProperty());
			t.setClosable(false);
			t.setContent(detailsPane = new DetailsPane(e));
			noteString.bindBidirectional(detailsPane.getController().notes.textProperty());
			alarm.bindBidirectional(detailsPane.getController().alarmProperty());

			family.getTabs().add(t);
		}

		Tab newTab = new Tab("+");
		newTab.setTooltip(new Tooltip("Add Sibling"));
		newTab.getStyleClass().add("new-tab");
		newTab.setClosable(false);
		family.getTabs().add(newTab);

		family.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
			if (nv == newTab) {
				DetailsPane p;

				Tab tab = createAndSelectNewTab(family);
				
				Entry newEntry = group.newSibling();
				newEntry.setDetailsChanged(false); // don't persist if no changes
				tab.textProperty().bind(newEntry.firstNameYiddishProperty());
				tab.setContent(p = new DetailsPane(newEntry));

				p.getController().notes.textProperty().bindBidirectional(noteString);
				p.getController().alarmProperty().bindBidirectional(alarm);
			}

		});

		boolean success = Util.createDialog(family, "Details",
				detailsPane.getController().phone.textProperty().isEmpty()
						.or(detailsPane.getController().cityYiddish.getEditor().textProperty().isEmpty()),
				ButtonType.CANCEL, ButtonType.OK).filter(b -> b == ButtonType.OK).isPresent();

		group.setDisableConflictMerge(true);

		for (Tab t : family.getTabs()) {
			if (t == newTab)
				continue;

			DetailsPane pane = (DetailsPane) t.getContent();
			Entry e = pane.getController().getEntry();
			if (success) {
				if (e.isDetailsChanged()) {
					if (e.getRow() < 0) {
						e.persist();
					} else {
						e.saveDetails();
					}
				}

				for (int i = 0; i < pane.pointData.size(); i++) {
					if (pane.pointData.get(i).changed()) {
						e.putPoint(i, pane.pointData.get(i).getValue());
					}
				}
				for (int i = 0; i < pane.shavuosData.size(); i++) {
					if (pane.shavuosData.get(i).changed()) {
						e.putShavuosData(i, pane.shavuosData.get(i).getValue());
					}
				}
				for (int i = 0; i < pane.giftsData.size(); i++) {
					if (pane.giftsData.get(i).changed()) {
						e.putGiftReceived(i, pane.giftsData.get(i).getValue());
					}
				}
			} else {
				if (e.getTab() > 0 && e.getRow() > 0 && e.isDetailsChanged()) {
					e.reload();
				}
			}
		}

		Entry e = entries.get(0);
		Note n = NoteManager.getInstance().getNote(e.getPhone());

		if (success) {
			if (n != null) {
				if (n.getNote().isEmpty()) {
					n.delete();
				} else if (n.isChanged()) {
					n.save();
				}
			} else if (!noteString.get().isEmpty()) {
				Note.withNumberAndAlarm(e.getPhone(), alarm.get(), noteString.get());
			}
		} else {
			if (n != null && n.isChanged()) {
				n.reload();
			}

		}
	}

	private static Tab createAndSelectNewTab(TabPane tabPane) {
		Tab tab = new Tab();
		final ObservableList<Tab> tabs = tabPane.getTabs();
		tabs.add(tabs.size() - 1, tab);
		tabPane.getSelectionModel().select(tab);
		return tab;
	}
}