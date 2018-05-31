# TableUsability
Mit TableUsability, können Sie jede einzelne JavaFX Tabellenspalten ("TableColoumns") einer JavaFX Tabelle ("TableView") durchnavigieren.

Damit ermöglichen Sie Ihren Benutzern, bequem mit der Tastatur durch jede JavaFX Tabelle ("TableView") zu navigieren, die Sortierungsfunktion auszulösen (von ein oder mehreren Tabellenspalten) oder die Tabellenspalten ("TableColoumns") zu verschieben.

Um diese Features nutzen zu können, müssen Sie lediglich eine Instanz von der "SelectionTableHeader" Klasse erzeugen, die Methode "activateAccessibility" aufrufen und die Tabelle ("TableView") als Parameter angeben.

Beispiel:

	public class SampleController implements Initializable {

		@FXML
		TableView<String> tableView;

		private SelectionTableHeader<String> sth = new SelectionTableHeader<String>();

		public SampleController() {
		}

		@Override
		public void initialize(URL location, ResourceBundle resources) {
			test();
		}

		private void test() {
			for (int i = 1; i <= 4; i++) {
				tableView.getColumns()
						.add(new TooltipTableColumn<String, String>("Spalte" + i, "Tooltip fuer die Spalte" 						         + i));
			}
			sth.activateAccessibility(tableView);
		}
	}
