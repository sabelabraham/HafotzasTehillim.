package org.hafotzastehillim.pointentry.fx;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.hafotzastehillim.pointentry.fx.cell.EditorData;
import org.hafotzastehillim.pointentry.fx.cell.GiftEditorCell;
import org.hafotzastehillim.pointentry.fx.cell.PointEditorCell;
import org.hafotzastehillim.pointentry.fx.cell.ShavuosEditorCell;
import org.hafotzastehillim.pointentry.fx.notes.Note;
import org.hafotzastehillim.pointentry.fx.notes.NoteManager;
import org.hafotzastehillim.pointentry.fx.util.NoSelectionModel;
import org.hafotzastehillim.pointentry.fx.util.DialogUtils;
import org.hafotzastehillim.pointentry.spreadsheet.Entry;
import org.hafotzastehillim.pointentry.spreadsheet.FamilyGrouping;
import org.scenicview.ScenicView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DetailsPane extends VBox {

	private FormController controller;
	private ObservableList<EditorData<Integer>> pointData;
	private ObservableList<EditorData<Integer>> shavuosData;
	private ObservableList<EditorData<Boolean>> giftsData;

	public DetailsPane(Entry entry) {
		Objects.requireNonNull(entry);

		getStyleClass().add("details-pane");

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/Form.fxml"));
		Pane form = null;
		try {
			form = loader.load();
		} catch (IOException e) {
			DialogUtils.showErrorDialog(e);
			return;
		}

		// form.getStyleClass().add("form");
		// form.getStylesheets().add(getClass().getResource("/resources/css/form.css").toExternalForm());

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

		EditorData<Integer> lastPoint = null;
		for (int point : entry.getPoints())
			pointData.add(lastPoint = new EditorData<>(point));

		if (lastPoint != null)
			pointsList.scrollTo(lastPoint);

		Label pointsLabel = new Label("Weekly Program");
		JFXButton pointsAdd = new JFXButton("Add");

		if (entry.getRow() < 0)
			pointsAdd.disableProperty().bind(entry.detailsChangedProperty().not());

		pointsAdd.setId("points-add");
		pointsAdd.setOnAction(evt -> {
			for (int i = pointData.size(); i < Model.getInstance().getCampaignIndex() - 1; i++)
				pointData.add(new EditorData<>(0));

			EditorData<Integer> lastP = new EditorData<>(0);
			pointData.add(lastP);

			pointsList.scrollTo(lastP);
		});

		VBox pointsPane = new VBox(10);
		HBox pointsHeader = new HBox(15);
		pointsHeader.getChildren().addAll(pointsLabel, pointsAdd);
		pointsHeader.setPadding(new Insets(10, 10, 0, 10));
		pointsHeader.setAlignment(Pos.BASELINE_CENTER);

		Label total = new Label();
		total.setPadding(new Insets(0, 0, 10, 0));
		if (entry.getRow() >= 0)
			total.setText("Total Points: " + entry.getTotal());

		pointsPane.getChildren().addAll(pointsHeader, pointsList, new StackPane(total));
		pointsPane.disableProperty().bind(controller.delete.selectedProperty());
		pointsPane.setId("points-pane");

		// -----------------

		shavuosData = FXCollections.observableArrayList();
		ListView<EditorData<Integer>> shavuosList = new ListView<>();
		shavuosList.setCellFactory(view -> new ShavuosEditorCell());
		shavuosList.setItems(shavuosData);
		shavuosList.setSelectionModel(new NoSelectionModel<EditorData<Integer>>());
		shavuosList.setPrefHeight(200);
		shavuosList.setId("shavuos-list");

		EditorData<Integer> lastShavuos = null;
		for (int data : entry.getShavuosData())
			shavuosData.add(lastShavuos = new EditorData<>(data));

		if (lastShavuos != null)
			shavuosList.scrollTo(lastShavuos);

		Label shavuosLabel = new Label("Shavuos Program");
		JFXButton shavuosAdd = new JFXButton("Add");

		if (entry.getRow() < 0)
			shavuosAdd.disableProperty().bind(entry.detailsChangedProperty().not());

		shavuosAdd.setId("shavuos-add");
		shavuosAdd.setOnAction(evt -> {
			EditorData<Integer> lastS = new EditorData<>(0);
			shavuosData.add(lastS);

			shavuosList.scrollTo(lastS);
		});

		VBox shavuosPane = new VBox(10);
		HBox shavuosHeader = new HBox(15);
		shavuosHeader.getChildren().addAll(shavuosLabel, shavuosAdd);
		shavuosHeader.setPadding(new Insets(10, 10, 0, 10));
		shavuosHeader.setAlignment(Pos.BASELINE_CENTER);

		shavuosPane.getChildren().addAll(shavuosHeader, shavuosList);
		shavuosPane.disableProperty().bind(controller.delete.selectedProperty());
		shavuosPane.setId("shavuos-pane");

		// -----------------

		giftsData = FXCollections.observableArrayList();
		ListView<EditorData<Boolean>> giftsList = new ListView<>();
		giftsList.setCellFactory(view -> new GiftEditorCell());
		giftsList.setItems(giftsData);
		giftsList.setSelectionModel(new NoSelectionModel<EditorData<Boolean>>());
		giftsList.setPrefHeight(200);
		giftsList.setId("gifts-list");

		EditorData<Boolean> lastGift = null;
		for (boolean data : entry.getGiftsReceived())
			giftsData.add(lastGift = new EditorData<>(data));

		int eligable = entry.getTotal() / 100;
		while (giftsData.size() < eligable) {
			giftsData.add(lastGift = new EditorData<>(false));
		}

		if (lastGift != null)
			giftsList.scrollTo(lastGift);

		Label giftsLabel = new Label("Gifts");
		giftsLabel.setPadding(new Insets(4));

		VBox giftsPane = new VBox(10);
		HBox giftsHeader = new HBox(15);
		giftsHeader.getChildren().addAll(giftsLabel);
		giftsHeader.setPadding(new Insets(10, 10, 0, 10));
		giftsHeader.setAlignment(Pos.BASELINE_CENTER);

		giftsPane.getChildren().addAll(giftsHeader, giftsList);
		giftsPane.disableProperty().bind(controller.delete.selectedProperty());
		giftsPane.setId("gifts-pane");

		// -----------------

		HBox allLists = new HBox(pointsPane, shavuosPane);
		if (entry.getRow() >= 0) {
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
		// DetailsPane pane = new DetailsPane(null);
		// boolean success = DialogUtils.createDialog(pane, "New Member",
		// pane.getController().phone.textProperty().isEmpty()
		// .or(pane.getController().cityYiddish.getEditor().textProperty().isEmpty()),
		// ButtonType.CANCEL, ButtonType.OK).filter(b -> b ==
		// ButtonType.OK).isPresent();
		//
		// FormController form = pane.getController();
		//
		// if (success) {
		// Entry e = form.getEntry();
		//
		// e.persist(row -> {
		// for (int i = 0; i < pane.pointData.size(); i++) {
		// if (pane.pointData.get(i).changed()) {
		// e.putPoint(i, pane.pointData.get(i).getValue());
		// }
		// }
		// for (int i = 0; i < pane.shavuosData.size(); i++) {
		// if (pane.shavuosData.get(i).changed()) {
		// e.putShavuosData(i, pane.shavuosData.get(i).getValue());
		// }
		// }
		// for (int i = 0; i < pane.giftsData.size(); i++) {
		// if (pane.giftsData.get(i).changed()) {
		// e.putGiftReceived(i, pane.giftsData.get(i).getValue());
		// }
		// }
		// });
		//
		// Note n = NoteManager.getInstance().getNote(e.getPhone());
		// if (n != null) {
		// if (n.getNote().isEmpty()) {
		// n.delete();
		// } else if (n.isChanged()) {
		// n.save();
		// }
		// } else if (!form.notes.getText().isEmpty()) {
		// Note.withNumberAndAlarm(e.getPhone(), form.getAlarm(), form.notes.getText());
		// }
		// } else {
		// Note n = NoteManager.getInstance().getNote(form.phone.getText());
		// if (n != null && n.isChanged()) {
		// n.reload();
		// }
		// }
		showDialog(Arrays.asList(new Entry(Model.getInstance().getSpreadsheet())));
	}

	public static void showDialog(Entry entry) {
		showDialog(Arrays.asList(entry));
	}

	private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

	public static void showDialog(List<Entry> entries) {
		if (entries.size() == 0) {
			showNewDialog();
			return;
		}

		FamilyGrouping group = new FamilyGrouping(entries);
		StringProperty noteString = new SimpleStringProperty("");
		ObjectProperty<Instant> alarm = new SimpleObjectProperty<>();

		JFXTabPane family = new JFXTabPane();
		family.setDisableAnimation(true);
		family.getStylesheets()
				.add(DetailsPane.class.getResource("/resources/css/details-tab-pane.css").toExternalForm());

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

				// don't allow adding new tabs like a maniac
				family.getTabs().remove(newTab);
				newEntry.detailsChangedProperty().addListener((obs2, ov2, nv2) -> {
					if (nv2)
						family.getTabs().add(newTab);
				});

				tab.textProperty().bind(newEntry.firstNameYiddishProperty());
				tab.setContent(p = new DetailsPane(newEntry));

				p.getController().notes.textProperty().bindBidirectional(noteString);
				p.getController().alarmProperty().bindBidirectional(alarm);
			}

		});

		DetailsPane dp = (DetailsPane) family.getTabs().get(0).getContent();
		BooleanBinding disable = Bindings.createBooleanBinding(() -> {
			String phone = dp.getController().phone.getText();

			if (dp.getController().cityYiddish.getEditor().getText().isEmpty() || phone.isEmpty())
				return true;

			if (!phone.isEmpty()) {
				try {
					PhoneNumber p = phoneUtil.parse(phone, "US");
					if (!phoneUtil.isValidNumber(p))
						return true;
				} catch (NumberParseException e) {
					return true;
				}
			}

			String cell = dp.getController().cellPhone.getText();
			if (!cell.isEmpty()) {
				try {
					PhoneNumber p = phoneUtil.parse(cell, "US");
					if (!phoneUtil.isValidNumber(p))
						return true;
				} catch (NumberParseException e) {
					return true;
				}
			}

			return false;

		}, dp.getController().phone.textProperty(), dp.getController().cellPhone.textProperty(),
				dp.getController().cityYiddish.getEditor().textProperty());

		newTab.disableProperty().bind(disable);

		ScenicView.show(new Scene(detailsPane));
		
		boolean success = DialogUtils.createDialog(family, "Member Details", disable, ButtonType.CANCEL, ButtonType.OK)
				.filter(b -> b == ButtonType.OK).isPresent();

		
		group.setDisableConflictMerge(true);

		for (Tab t : family.getTabs()) {
			if (t == newTab)
				continue;

			DetailsPane pane = (DetailsPane) t.getContent();
			Entry e = pane.getController().getEntry();
			if (success) {
				if (pane.getController().delete.isSelected()) {
					e.delete();
					continue;
				}

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