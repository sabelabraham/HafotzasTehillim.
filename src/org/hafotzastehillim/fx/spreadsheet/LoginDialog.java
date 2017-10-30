package org.hafotzastehillim.fx.spreadsheet;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import org.hafotzastehillim.fx.Main;
import org.hafotzastehillim.fx.Model;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginDialog extends Stage {

	private LoginController controller;
	private Pane view;
	private Rectangle clip;


	public LoginDialog(boolean cached) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/Login.fxml"));

		try {
			view = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		controller = loader.getController();
		if (cached)
			controller.setMessageByLoggedIn();

		initModality(Modality.APPLICATION_MODAL);
		getIcons().add(Main.ICON);

		view.getStyleClass().add("login-dialog");
		view.getStylesheets().add(getClass().getResource("/resources/css/login-dialog.css").toExternalForm());

		clip = new Rectangle();
		clip.widthProperty().bind(view.widthProperty().divide(2));
		clip.setHeight(430);
		clip.setY(30);

		view.setClip(clip);
		view.translateXProperty().bind(clip.translateXProperty().negate());

		controller.staySignedInProperty().bindBidirectional(Model.getInstance().staySignedInProperty());
		controller.setShowStaySignedIn(cached);

		setOnCloseRequest(evt -> {
			SheetsAPI.stop();
		});

		setScene(new Scene(new Group(view)));

		setTitle("Google Login");
		setResizable(false);
	}

	public void setSignInAction(Supplier<Task<?>> supplier) {
		controller.setSignInAction(evt -> {
			Task<?> task = supplier.get();

			task.stateProperty().addListener((obs, ov, nv) -> {
				if (ov == State.RUNNING) {
					close();
				}
			});

			Thread th = new Thread(task);
			th.setDaemon(true);
			th.start();

			moveClipRight();
		});
	}

	public void setCustomMessage(String msg) {
		controller.setCustomMessage(msg);

		if(msg == null || msg.isEmpty())
			setTitle("Google Login");
		else
			setTitle("Google Login | " + msg);

	}

	public void setWebAddress(String url) {
		Platform.runLater(() -> controller.getWebView().getEngine().load(url));
	}

	public void moveClipRight() {
		TranslateTransition animation = new TranslateTransition(Duration.seconds(0.25), clip);
		animation.setToX(view.getWidth() / 2);

		PauseTransition pause = new PauseTransition(Duration.seconds(1));
		SequentialTransition both = new SequentialTransition(pause, animation);

		both.play();
	}

	public void moveClipLeft() {
		TranslateTransition animation = new TranslateTransition(Duration.ONE, clip);
		animation.setToX(0);

		animation.play();
	}
//
//	private static final Preferences pref = Preferences.userNodeForPackage(LoginDialog.class);
//	private static final String STAY_SIGNED_IN_KEY = "StaySignedIn";
//
//	private static BooleanProperty staySignedIn;
//
//	public static BooleanProperty staySignedInProperty() {
//		if(staySignedIn == null) {
//			boolean value = pref.getBoolean(STAY_SIGNED_IN_KEY, true);
//			staySignedIn = new SimpleBooleanProperty(value);
//			staySignedIn.addListener((obs, ov, nv) -> {
//				pref.putBoolean(STAY_SIGNED_IN_KEY, staySignedIn.get());
//			});
//		}
//
//		return staySignedIn;
//	}
//
//	public static final boolean isStaySignedIn() {
//		return staySignedInProperty().get();
//	}
//
//	public static final void setStaySignedIn(boolean bool) {
//		staySignedInProperty().set(bool);
//	}


}
