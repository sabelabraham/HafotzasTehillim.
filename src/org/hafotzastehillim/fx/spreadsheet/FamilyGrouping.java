package org.hafotzastehillim.fx.spreadsheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.hafotzastehillim.fx.Model;
import org.hafotzastehillim.fx.util.Util;
import org.reactfx.Subscription;

import com.jfoenix.controls.JFXButton;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

public class FamilyGrouping implements Selectable, Comparable<FamilyGrouping> {

	private ObservableList<Entry> siblings;

	private StringProperty lastName;
	private StringProperty addressNumber;
	private StringProperty addressName;
	private StringProperty apt;
	private StringProperty city;
	private StringProperty state;
	private StringProperty zip;
	private StringProperty phone;
	private StringProperty lastNameYiddish;
	private StringProperty fatherName;
	private StringProperty cityYiddish;

	private BooleanProperty selected;

	private static final Model model = Model.getInstance();

	public FamilyGrouping(List<Entry> siblings) {
		this.siblings = FXCollections.observableArrayList(siblings);

		if (siblings.size() == 0)
			return;

		List<Subscription> subscriptions = new ArrayList<>();

		Entry prev = siblings.get(0);

		bind(prev);
		for (int i = 1; i < siblings.size(); i++) {

			subscriptions.add(subscribe(prev));

			String first = siblings.get(i).getFirstName().toLowerCase().replace(" ", "");
			String firstY = siblings.get(i).getFirstNameYiddish().replace(" ", "");
			for (int sub = 0; sub < i; sub++) {
				if (!model.isIgnoreDuplicates()
						&& (siblings.get(sub).getFirstName().toLowerCase().replace(" ", "").equals(first)
								|| siblings.get(sub).getFirstNameYiddish().replace(" ", "").equals(firstY))) {
					Util.createAlert(
							AlertType.WARNING, "Duplicate", "Duplicate Detected", "Entry \"" + prev.getId()
									+ "\" and \"" + siblings.get(i).getId() + "\" seem to be a duplicate.",
							ButtonType.OK);
				}
			}

			attemptBind(prev, siblings.get(i), e -> e.lastNameProperty(), "Last Name");
			attemptBind(prev, siblings.get(i), e -> e.addressNumberProperty(), "Address Number");
			attemptBind(prev, siblings.get(i), e -> e.addressNameProperty(), "Address Name");
			attemptBind(prev, siblings.get(i), e -> e.aptProperty(), "Apt");
			attemptBind(prev, siblings.get(i), e -> e.cityProperty(), "City");
			attemptBind(prev, siblings.get(i), e -> e.stateProperty(), "State");
			attemptBind(prev, siblings.get(i), e -> e.zipProperty(), "Zip");
			attemptBind(prev, siblings.get(i), e -> e.phoneProperty(), "Phone");
			attemptBind(prev, siblings.get(i), e -> e.lastNameYiddishProperty(), "Yiddish Last Name");
			attemptBind(prev, siblings.get(i), e -> e.fatherNameProperty(), "Father's Name");

			prev = siblings.get(i);
		}

		for (Subscription s : subscriptions) {
			s.unsubscribe();
		}

	}

	public ObservableList<Entry> getSiblings() {
		return FXCollections.unmodifiableObservableList(siblings);
	}

	public final String getLastName() {
		return lastNameProperty().get();
	}

	public final String getAddressNumber() {
		return addressNumberProperty().get();
	}

	public final String getAddressName() {
		return addressNameProperty().get();
	}

	public final String getApt() {
		return aptProperty().get();
	}

	public final String getCity() {
		return cityProperty().get();
	}

	public final String getState() {
		return stateProperty().get();
	}

	public final String getZip() {
		return zipProperty().get();
	}

	public final String getPhone() {
		return phoneProperty().get();
	}

	public final String getLastNameYiddish() {
		return lastNameYiddishProperty().get();
	}

	public final String getFatherName() {
		return fatherNameProperty().get();
	}

	public final String getCityYiddish() {
		return cityYiddishProperty().get();
	}

	public final boolean isSelected() {
		return selectedProperty().get();
	}

	public final void setLastName(String str) {
		lastNameProperty().set(str);
	}

	public final void setAddressNumber(String str) {
		addressNumberProperty().set(str);
	}

	public final void setAddressName(String str) {
		addressNameProperty().set(str);
	}

	public final void setApt(String str) {
		aptProperty().set(str);
	}

	public final void setCity(String str) {
		cityProperty().set(str);
	}

	public final void setState(String str) {
		stateProperty().set(str);
	}

	public final void setZip(String str) {
		zipProperty().set(str);
	}

	public final void setPhone(String str) {
		phoneProperty().set(str);
	}

	public final void setLastNameYiddish(String str) {
		lastNameYiddishProperty().set(str);
	}

	public final void setFatherName(String str) {
		fatherNameProperty().set(str);
	}

	public final void setCityYiddish(String str) {
		cityYiddishProperty().set(str);
	}

	public final void setSelected(Boolean bool) {
		selectedProperty().set(bool);
	}

	public StringProperty lastNameProperty() {
		if (lastName == null) {
			lastName = new SimpleStringProperty(this, "lastName", "");

		}

		return lastName;
	}

	public StringProperty addressNumberProperty() {
		if (addressNumber == null) {
			addressNumber = new SimpleStringProperty(this, "addressNumber", "");

		}

		return addressNumber;
	}

	public StringProperty addressNameProperty() {
		if (addressName == null) {
			addressName = new SimpleStringProperty(this, "addressName", "");

		}

		return addressName;
	}

	public StringProperty aptProperty() {
		if (apt == null) {
			apt = new SimpleStringProperty(this, "apt", "");

			apt.addListener((obs, ov, nv) -> {
				if (!nv.isEmpty() && !nv.contains("#"))
					apt.set("#" + nv);
			});
		}

		return apt;
	}

	public StringProperty cityProperty() {
		if (city == null) {
			city = new SimpleStringProperty(this, "city", "");

		}

		return city;
	}

	public StringProperty stateProperty() {
		if (state == null) {
			state = new SimpleStringProperty(this, "state", "");

		}

		return state;
	}

	public StringProperty zipProperty() {
		if (zip == null) {
			zip = new SimpleStringProperty(this, "zip", "");

		}

		return zip;
	}

	public StringProperty phoneProperty() {
		if (phone == null) {
			phone = new SimpleStringProperty(this, "phone", "");

		}

		return phone;
	}

	public StringProperty lastNameYiddishProperty() {
		if (lastNameYiddish == null) {
			lastNameYiddish = new SimpleStringProperty(this, "lastNameYiddish", "");

		}

		return lastNameYiddish;
	}

	public StringProperty fatherNameProperty() {
		if (fatherName == null) {
			fatherName = new SimpleStringProperty(this, "fatherName", "");

		}

		return fatherName;
	}

	public StringProperty cityYiddishProperty() {
		if (cityYiddish == null) {
			cityYiddish = new SimpleStringProperty(this, "cityYiddish", "");

		}

		return cityYiddish;
	}

	public BooleanProperty selectedProperty() {
		if (selected == null) {
			selected = new SimpleBooleanProperty(this, "selected", false);
		}

		return selected;
	}

	private void bind(Entry e) {
		lastNameProperty().bindBidirectional(e.lastNameProperty());
		addressNumberProperty().bindBidirectional(e.addressNumberProperty());
		addressNameProperty().bindBidirectional(e.addressNameProperty());
		aptProperty().bindBidirectional(e.aptProperty());
		cityProperty().bindBidirectional(e.cityProperty());
		stateProperty().bindBidirectional(e.stateProperty());
		zipProperty().bindBidirectional(e.zipProperty());
		phoneProperty().bindBidirectional(e.phoneProperty());
		lastNameYiddishProperty().bindBidirectional(e.lastNameYiddishProperty());
		fatherNameProperty().bindBidirectional(e.fatherNameProperty());
		cityYiddishProperty().bindBidirectional(e.cityYiddishProperty());
	}

	public static void attemptBind(Entry e1, Entry e2, Function<Entry, StringProperty> extractor, String name) {
		StringProperty p1 = extractor.apply(e1);
		StringProperty p2 = extractor.apply(e2);
		if (p1.get().equals(p2.get())) {
			p1.bindBidirectional(p2);
			return;
		}
		if (model.isIgnoreConflicts())
			return;

		Text lastName1 = new Text();
		Text addressNumber1 = new Text();
		Text addressName1 = new Text();
		Text apt1 = new Text();
		Text city1 = new Text();
		Text state1 = new Text();
		Text zip1 = new Text();
		Text phone1 = new Text();
		Text lastNameYiddish1 = new Text();
		Text fatherName1 = new Text();

		lastName1.textProperty().bind(e1.lastNameProperty().concat("\n"));
		addressNumber1.textProperty().bind(e1.addressNumberProperty().concat(" "));
		addressName1.textProperty().bind(e1.addressNameProperty().concat(" "));
		apt1.textProperty().bind(e1.aptProperty().concat("\n"));
		city1.textProperty().bind(e1.cityProperty().concat(", "));
		state1.textProperty().bind(e1.stateProperty().concat(" "));
		zip1.textProperty().bind(e1.zipProperty().concat("\n"));
		phone1.textProperty().bind(e1.phoneProperty().concat("\n\n"));
		lastNameYiddish1.textProperty().bind(e1.lastNameYiddishProperty());
		fatherName1.textProperty().bind(e1.fatherNameProperty().concat(" "));

		Text lastName2 = new Text();
		Text addressNumber2 = new Text();
		Text addressName2 = new Text();
		Text apt2 = new Text();
		Text city2 = new Text();
		Text state2 = new Text();
		Text zip2 = new Text();
		Text phone2 = new Text();
		Text lastNameYiddish2 = new Text();
		Text fatherName2 = new Text();

		lastName2.textProperty().bind(e2.lastNameProperty().concat("\n"));
		addressNumber2.textProperty().bind(e2.addressNumberProperty().concat(" "));
		addressName2.textProperty().bind(e2.addressNameProperty().concat(" "));
		apt2.textProperty().bind(e2.aptProperty().concat("\n"));
		city2.textProperty().bind(e2.cityProperty().concat(", "));
		state2.textProperty().bind(e2.stateProperty().concat(" "));
		zip2.textProperty().bind(e2.zipProperty().concat("\n"));
		phone2.textProperty().bind(e2.phoneProperty().concat("\n\n"));
		lastNameYiddish2.textProperty().bind(e2.lastNameYiddishProperty());
		fatherName2.textProperty().bind(e2.fatherNameProperty().concat(" "));

		TextFlow flow1 = new TextFlow(lastName1, addressNumber1, addressName1, apt1, city1, state1, zip1,
				new Text("\n\n"), fatherName1, lastNameYiddish1);
		flow1.setTextAlignment(TextAlignment.CENTER);

		TextFlow flow2 = new TextFlow(lastName2, addressNumber2, addressName2, apt2, city2, state2, zip2,
				new Text("\n\n"), fatherName2, lastNameYiddish2);
		flow2.setTextAlignment(TextAlignment.CENTER);

		Text info = new Text("Please fix the family information conflict");
		info.setFont(Font.font(14));
		Text fieldName = new Text(name);

		TextField field1 = new TextField();
		TextField field2 = new TextField();

		Button shiftLeft = new JFXButton("<");
		Button shiftRight = new JFXButton(">");

		shiftRight.setStyle("-fx-background-color: darkcyan; -fx-text-fill: white; -fx-font-weight: bold;");
		shiftLeft.styleProperty().bind(shiftRight.styleProperty());
		shiftRight.setShape(new Circle(15));
		shiftLeft.shapeProperty().bind(shiftRight.shapeProperty());

		GridPane grid = new GridPane();
		grid.add(info, 0, 0, 4, 1);
		grid.add(flow1, 0, 2, 2, 1);
		grid.add(flow2, 2, 2, 2, 1);
		grid.add(new Separator(), 0, 3, 4, 1);
		grid.add(fieldName, 1, 3, 2, 1);
		grid.add(field1, 0, 4, 2, 1);
		grid.add(field2, 2, 4, 2, 1);
		grid.add(shiftRight, 1, 5);
		grid.add(shiftLeft, 2, 5);

		GridPane.setHalignment(info, HPos.CENTER);
		GridPane.setHalignment(flow1, HPos.CENTER);
		GridPane.setHalignment(flow2, HPos.CENTER);
		GridPane.setHalignment(fieldName, HPos.CENTER);
		GridPane.setHalignment(field1, HPos.CENTER);
		GridPane.setHalignment(field2, HPos.CENTER);
		GridPane.setHalignment(shiftRight, HPos.CENTER);
		GridPane.setHalignment(shiftLeft, HPos.CENTER);

		GridPane.setValignment(info, VPos.CENTER);
		GridPane.setValignment(flow1, VPos.CENTER);
		GridPane.setValignment(flow2, VPos.CENTER);
		GridPane.setValignment(fieldName, VPos.BOTTOM);
		GridPane.setValignment(field1, VPos.CENTER);
		GridPane.setValignment(field2, VPos.CENTER);
		GridPane.setValignment(shiftRight, VPos.TOP);
		GridPane.setValignment(shiftLeft, VPos.TOP);

		GridPane.setMargin(field1, new Insets(0, 10, 0, 10));
		GridPane.setMargin(field2, new Insets(0, 10, 0, 10));

		grid.getColumnConstraints().addAll(Collections.nCopies(4, new ColumnConstraints(50, 100, 120)));
		grid.getRowConstraints().addAll(Collections.nCopies(6, new RowConstraints(30, 60, 100)));

		grid.setAlignment(Pos.CENTER);

		shiftRight.setFocusTraversable(false);
		shiftLeft.setFocusTraversable(false);

		field1.setText(p1.get());
		field2.setText(p2.get());

		shiftRight.setOnAction(evt -> field2.setText(field1.getText()));
		shiftLeft.setOnAction(evt -> field1.setText(field2.getText()));

		BooleanBinding disable = Bindings.createBooleanBinding(() -> !field1.getText().equals(field2.getText()),
				field1.textProperty(), field2.textProperty());

		Util.createDialog(new StackPane(grid), "Family Conflict", disable, ButtonType.OK)
				.filter(t -> t == ButtonType.OK).ifPresent(t -> {
					p1.set(field1.getText());
					p2.bindBidirectional(p1);
				});
	}

	private Subscription subscribe(Entry e) {
		return e.getChangeStream().subscribe(change -> e.saveDetails());
	}

	@Override
	public int compareTo(FamilyGrouping o) {
		return getLastNameYiddish().compareTo(o.getLastNameYiddish());
	}

}
