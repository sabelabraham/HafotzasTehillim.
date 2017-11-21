import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class TabPaneWithAddTab extends Application {

	@Override
	public void start(Stage primaryStage) {
		final TabPane tabPane = new TabPane();
		final Tab newTab = new Tab("+");
		newTab.setClosable(false);
		tabPane.getTabs().add(newTab);
		createAndSelectNewTab(tabPane, "First tab");

		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, ov,nv) -> {
				if (nv == newTab) {
					createAndSelectNewTab(tabPane, "Tab " + (tabPane.getTabs().size()));
				}
			
		});

		final BorderPane root = new BorderPane();
		root.setCenter(tabPane);
		primaryStage.setScene(new Scene(root, 600, 400));
		primaryStage.show();
	}

	private Tab createAndSelectNewTab(final TabPane tabPane, final String title) {
		Tab tab = new Tab(title);
		final ObservableList<Tab> tabs = tabPane.getTabs();
		tab.closableProperty().bind(Bindings.size(tabs).greaterThan(2));
		tabs.add(tabs.size() - 1, tab);
		tabPane.getSelectionModel().select(tab);
		return tab;
	}

	public static void main(String[] args) {
		launch(args);
	}
}