import java.util.ArrayList;
import java.util.Collections;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 * 
 * @author Michael K. Dennis A.
 *
 * @param <T>
 */
public class SelectionTableHeader<T> {

	/**
	 *  List of all Columns from the Table
	 */
	private ArrayList<Label> allColumnHeader = new ArrayList<>();
	
	/**
	 *  List of columnTablePosition's within the Table, which need to change
	 */
	private ArrayList<Integer> changeColumnIndex = new ArrayList<>();
	
	/**
	 *  This Column Position is focused
	 */
	private int activeColumnHeader = 0;
	
	/**
	 *  Needed for Orientation after Table Columns switched
	 */
	private int lastSelectedColumn;
	
	/**
	 *  Needed for Orientation after Table Columns switched
	 */
	private int firstSelectedColumn;
	
	/**
	 *  Start of the Navigation
	 */
	private int startColumn;
	
	/**
	 *  Is Accessibility Mode active? (After KeyCodeCombination is triggered)
	 */
	private boolean eventHandlerActive = false;
	
	/**
	 *  EventHandler, which activates the KeyCombinations and the Logic, after one of the Custom Key's is Pressed.
	 */
	private EventHandler<KeyEvent> eventHandler;
	
	/**
	 *  JavaFX TableView, which can use the Accessibility Mode
	 */
	private TableView<T> table;
	
	/**
	 *  List of all Table Columns from the table
	 */
	private ObservableList<TableColumn<T, ?>> allTableColumns = FXCollections.observableArrayList();
	
	/**
	 *  Deactivate Accessibility Mode with given KeyCombination
	 */
	private final KeyCombination keyCombinationTab = new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHIFT_DOWN);
	
	/**
	 *  Activate Accessibility Mode with CONTROL_DOWN + H
	 */
	private KeyCombination keyCombinationH = new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN);
	
	/**
	 *  After Accessibility Mode is active, Select multiple Columns with SHIFT + LEFT_ARROW
	 */
	private KeyCombination keyCombinationShiftLeft = new KeyCodeCombination(KeyCode.LEFT,
			KeyCombination.SHIFT_DOWN);
	
	/**
	 *  After Accessibility Mode is active, Select multiple Columns with SHIFT + RIGHT_ARROW
	 */
	private KeyCombination keyCombinationShiftRight = new KeyCodeCombination(KeyCode.RIGHT,
			KeyCombination.SHIFT_DOWN);
	
	/**
	 *  After Accessibility Mode is active, Switch selected Columns with CONTROL_DOWN + LEFT_ARROW
	 */
	private KeyCombination keyCombinationControlLeft = new KeyCodeCombination(KeyCode.LEFT,
			KeyCombination.CONTROL_DOWN);
	
	/**
	 *  After Accessibility Mode is active, Switch selected Columns with CONTROL_DOWN + RIGHT_ARROW
	 */
	private KeyCombination keyCombinationControlRight = new KeyCodeCombination(KeyCode.RIGHT,
			KeyCombination.CONTROL_DOWN);

	public SelectionTableHeader() {

	}

	/**
	 *  Activate Accessibility Mode
	 *  Be aware, that the KeyCodes: KeyCode.TAB and KeyCode.ESCAPE are disabling the Accessibility Mode
	 * @param table TableView, which need to get a better Accessibility
	 */
	public void activateAccessibility(TableView<T> table) {
		if(table != null && table.getColumns() != null && !table.getColumns().isEmpty()){
			this.table = table;
			for (Node node : table.lookupAll(".column-header > .label")) {
				allColumnHeader.add(((Label) node));
			}
			if(allColumnHeader.isEmpty()){
				for (int i = 0; i < table.getColumns().size(); i++) {
					allColumnHeader.add(new Label(""+i));
				}
			}
			renewTablePositions();
			allTableColumns.addAll(table.getColumns());
			selectionHeader();
		}else{
			System.err.println("Table or TableColumn is Null");
		}
	}

	/**
	 *  Logic, behind the Selection of the Table Column
	 */
	private void selectionHeader() {
		eventHandler = new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (keyCombinationH.match(event)) {
					changeHeader();
					eventHandlerActive = true;
				} else if (keyCombinationTab.match(event) && eventHandlerActive) {
					lostSelectionHead();
				} else if (keyCombinationShiftLeft.match(event) && eventHandlerActive) {
					activeColumnHeader--;
					markedColumn(1);
				} else if (keyCombinationShiftRight.match(event) && eventHandlerActive) {
					activeColumnHeader++;
					markedColumn(2);
				} else if (keyCombinationControlLeft.match(event) && eventHandlerActive) {
					if (changeColumnIndex.get(0) == 0) {
						return;
					}
					switchColumns(changeColumnIndex, false);
					for (int i = 0; i < changeColumnIndex.size(); i++) {
						changeColumnIndex.set(i, changeColumnIndex.get(i) - 1);
					}
					activeColumnHeader--;
				} else if (keyCombinationControlRight.match(event) && eventHandlerActive) {
					if (changeColumnIndex.get(changeColumnIndex.size() - 1) >= allColumnHeader.size() - 1) {
						return;
					}
					switchColumns(changeColumnIndex, true);
					for (int i = 0; i < changeColumnIndex.size(); i++) {
						changeColumnIndex.set(i, changeColumnIndex.get(i) + 1);
					}
					activeColumnHeader++;
				} else if (event.getCode() == KeyCode.TAB && eventHandlerActive) {
					lostSelectionHead();
				} else if (event.getCode() == KeyCode.ESCAPE && eventHandlerActive) {
					lostSelectionHead();
				} else if (event.getCode() == KeyCode.LEFT && eventHandlerActive) {
					activeColumnHeader--;
					changeHeader();
				} else if (event.getCode() == KeyCode.RIGHT && eventHandlerActive) {
					activeColumnHeader++;
					changeHeader();
				} else if (event.getCode() == KeyCode.SHIFT && eventHandlerActive) {
					markedColumn(3);
				} else if (event.getCode() == KeyCode.CONTROL && eventHandlerActive) {
					markedColumn(5);
				} else if (event.getCode() == KeyCode.SPACE && eventHandlerActive) {
					sortTableColumn();
				} else {
					lostSelectionHead();
				}
			}
		};
		this.table.setOnKeyPressed(eventHandler);
	}

	/**
	 *  Sort the Table Column (which are selected) after Space is hittet
	 */
	private void sortTableColumn() {
		TableColumn<T, ?> tempTableColumn = columnPosiEqualsAktivColumn();
		if (table.getSortOrder().contains(tempTableColumn)) {
			if (tempTableColumn.getSortType().name().equals("ASCENDING")) {
				tempTableColumn.setSortType(TableColumn.SortType.DESCENDING);
			} else {
				table.getSortOrder().remove(tempTableColumn);
			}
		} else {
			tempTableColumn.setSortType(TableColumn.SortType.ASCENDING);
			table.getSortOrder().add(tempTableColumn);
		}
	}

	/**
	 *  Change the Background Color to show, which Column is selected
	 */
	private void changeHeader() {
		for (int i = 0; i < allColumnHeader.size(); i++) {
			if (i == activeColumnHeader) {
				columnPosiEqualsAktivColumn().setStyle("-fx-background-color: white");
				startColumn = activeColumnHeader;
			} else if (activeColumnHeader < 0) {
				activeColumnHeader = 0;
				allColumnHeader.get(0).requestFocus();
			} else if (activeColumnHeader > allColumnHeader.size()) {
				activeColumnHeader = 0;
				allColumnHeader.get(0).requestFocus();
			} else {
				table.getColumns().get(i).setStyle("-fx-border-color: transparent");
				lastSelectedColumn = 0;
				firstSelectedColumn = 0;
				changeColumnIndex.clear();
			}
		}
	}

	/**
	 *  Disable / Deactivate the Accessibility Mode
	 */
	public void lostSelectionHead() {
		activeColumnHeader = 0;
		for (int i = 0; i < allColumnHeader.size(); i++) {
			table.getColumns().get(i).setStyle("-fx-border-color: transparent");
		}
		lastSelectedColumn = 0;
		firstSelectedColumn = 0;
		eventHandlerActive = false;
		changeColumnIndex.clear();
	}
	
	/**
	 *  Change the Border of the selected Table Column to red
	 * @param option Jump-Option (1 = jump backwards through selected Columns, 2 = jump forwards through selected Columns)
	 */
	private void markedColumn(int option) {
		if (changeColumnIndex.size() == 0) {
			changeColumnIndex.add(activeColumnHeader);
			columnPosiEqualsAktivColumn().setStyle("-fx-border-color: red");
		} else if (activeColumnHeader > allColumnHeader.size()) {
			activeColumnHeader = 0;
		} else if (activeColumnHeader < 0) {
			activeColumnHeader = 0;
		} else {
			checkColumns();
			jumpSelection(option);
		}
	}

	/**
	 *  After Table Columns are switched, Select the last active Table Column
	 */
	private void checkColumns() {
		for (int i = 0; i < changeColumnIndex.size(); i++) {
			if (changeColumnIndex.get(i) == activeColumnHeader) {
				return;
			}
		}
		changeColumnIndex.add(activeColumnHeader);
		columnPosiEqualsAktivColumn().setStyle("-fx-border-color: red");
	}

	/**
	 *  After navigate to left or right, set the un-selected Table Columns to normal Colors
	 * @param option Jump-Option (1 = jump backwards through selected Columns, 2 = jump forwards through selected Columns)
	 */
	private void jumpSelection(int option) {
		if (option == 1) {
			firstSelectedColumn++;
			int jump = startColumn - firstSelectedColumn;
			if (jump < 0) {
				jump = 0;
			}
			changeColumnIndex.add(jump);
			table.getColumns().get(jump).setStyle("-fx-border-color: red");
		} else if (option == 2) {
			lastSelectedColumn++;
			int jump = startColumn + lastSelectedColumn;
			if (jump > allColumnHeader.size() - 1) {
				jump = allColumnHeader.size() - 1;
			}
			changeColumnIndex.add(jump);
			table.getColumns().get(jump).setStyle("-fx-border-color: red");
		} else {

		}
	}

	/**
	 *  Is selected column equals activeColumnHeader
	 * @return Active TableColumn (Selected). If no Column is selected, than return null.
	 */
	private TableColumn<T, ?> columnPosiEqualsAktivColumn() {
		for (int i = 0; i < table.getColumns().size(); i++) {
			if (((TooltipTableColumn<T, ?>) table.getColumns().get(i))
					.getColumnTablePosition() == activeColumnHeader) {
				return table.getColumns().get(i);
			}
		}
		return null;
	}

	/**
	 *  Logic, to switch Table Columns
	 * @param startPositions The Index of all selected Columns, which need to be switched
	 * @param rightArrowKeyPressed Switch the selected Columns (true = Switch to the right, false = Switch to the left)
	 */
	private void switchColumns(ArrayList<Integer> startPositions, Boolean rightArrowKeyPressed) {
		if (startPositions == null || startPositions.size() <= 0
				|| startPositions.size() >= table.getColumns().size()) {
			return;
		}
		Collections.sort(startPositions);
		if (rightArrowKeyPressed) {
			if (startPositions.get(startPositions.size() - 1) + 1 >= table.getColumns().size())
				return;

			((TooltipTableColumn<T, ?>) table.getColumns()
					.get(startPositions.get(startPositions.size() - 1) + 1)).setColumnTablePosition(startPositions.get(0));
			for (int j = 0; j < startPositions.size(); j++) {
				((TooltipTableColumn<T, ?>) table.getColumns().get(startPositions.get(j)))
						.setColumnTablePosition(startPositions.get(j) + 1);
			}
			for (int i = 0; i < startPositions.size(); i++) {
				if (i == 0) {
					allTableColumns.set(startPositions.get(i),
							table.getColumns().get(startPositions.get(startPositions.size() - 1) + 1));

				}
				allTableColumns.set(startPositions.get(i) + 1, table.getColumns().get(startPositions.get(i)));
			}
		} else {
			if (startPositions.get(0) == 0)
				return;

			((TooltipTableColumn<T, ?>) table.getColumns().get(startPositions.get(0) - 1))
					.setColumnTablePosition(startPositions.get(startPositions.size() - 1));

			for (int j = 0; j < startPositions.size(); j++) {
				((TooltipTableColumn<T, ?>) table.getColumns().get(startPositions.get(j)))
						.setColumnTablePosition(startPositions.get(j) - 1);
			}

			for (int i = 0; i <= startPositions.size(); i++) {
				if (i == 0) {

					allTableColumns.set(startPositions.get(startPositions.size() - 1),
							table.getColumns().get(startPositions.get(0) - 1));
				} else {

					allTableColumns.set(startPositions.get(startPositions.size() - i) - 1,
							table.getColumns().get(startPositions.get(startPositions.size() - i)));
				}
			}
		}
		table.getColumns().clear();
		table.getColumns().addAll(allTableColumns);
	}

	/**
	 *  Set the Table Column positions (needed to initialize Accessibility Mode)
	 */
	private void renewTablePositions() {
		for (int i = 0; i < table.getColumns().size(); i++) {
			((TooltipTableColumn<T, ?>) table.getColumns().get(i)).setColumnTablePosition(i);
		}
	}
	
	/**
	 * 
	 * @return The Position of all Selected Columns in a List
	 */
	public ArrayList<Integer> getChangeColumnIndex() {
		return changeColumnIndex;
	}

	/**
	 * Set already Selected Columns
	 * @param changeColumnIndex Column Positions, which are visible and targetable
	 */
	public void setChangeColumnIndex(ArrayList<Integer> changeColumnIndex) {
		this.changeColumnIndex = changeColumnIndex;
	}

	/**
	 * 
	 * @return The position of the current Selected Column Header
	 */
	public int getAktivColumnHeader() {
		return activeColumnHeader;
	}

	/**
	 * Default activation KeyCombination: CONTROL_DOWN + KeyCode.H
	 * @param keyCombinationH Your Custom KeyCombination to activate the Accessibility Mode
	 */
	public void setActivateAccessibilityModeKeyCombination(KeyCombination keyCombinationH) {
		this.keyCombinationH = keyCombinationH;
	}

	/**
	 * Hold SHIFT_DOWN and Press or Hold the LEFT-Arrow-Key to select the Columns on the left side Step by Step.
	 * Default Start to Select of Multiple Columns while holding SHIFT_DOWN + KeyCode.LEFT
	 * @param keyCombinationShiftLeft Your Custom KeyCombination to select the Columns to the left
	 */
	public void setSelectMultipleColumnsKeyCombination(KeyCombination keyCombinationShiftLeft) {
		this.keyCombinationShiftLeft = keyCombinationShiftLeft;
	}

	/**
	 * Hold SHIFT_DOWN and Press or Hold the Right-Arrow-Key to select the Columns on the right side Step by Step
	 * Default Start to Select of Multiple Columns while holding SHIFT_DOWN + KeyCode.RIGHT
	 * @param keyCombinationShiftRight Your Custom KeyCombination to select the Columns to the right
	 */
	public void setKeyCombinationShiftRight(KeyCombination keyCombinationShiftRight) {
		this.keyCombinationShiftRight = keyCombinationShiftRight;
	}

	/**
	 * Default Switch selected Columns to the Left, while holding CONTROL_DOWN + KeyCode.LEFT
	 * @param keyCombinationControlLeft Your Custom KeyCombination to switch the Columns to the left
	 */
	public void setKeyCombinationControlLeft(KeyCombination keyCombinationControlLeft) {
		this.keyCombinationControlLeft = keyCombinationControlLeft;
	}

	/**
	 * Default Switch selected Columns to the Right, while holding CONTROL_DOWN + KeyCode.RIGHT
	 * @param keyCombinationControlRight Your Custom KeyCombination to switch the Columns to the right
	 */
	public void setKeyCombinationControlRight(KeyCombination keyCombinationControlRight) {
		this.keyCombinationControlRight = keyCombinationControlRight;
	}

	/**
	 * Is Accessibility Mode active?
	 * @return true = Yes, Accessibility Mode is active and false = No, Accessibility Mode is deactivated
	 */
	public boolean isAccessibilityModeActive() {
		return eventHandlerActive;
	}
	
}

