
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;

public class TooltipTableColumn<S, T> extends TableColumn<S, T> {
	
	private int columnTablePosition;
	private String tooltipText;

	public TooltipTableColumn() {
		super();
	}

	public TooltipTableColumn(String tooltip) {
		setTooltipToColumn(tooltip);
	}
	
	public TooltipTableColumn(String text,String tooltip) {
		setTooltipToColumn(text, tooltip);
	}

	public int getColumnTablePosition() {
		return columnTablePosition;
	}

	public void setColumnTablePosition(int columnTablePosition) {
		this.columnTablePosition = columnTablePosition;
	}
	
	private void setTooltipToColumn(String text,String tooltip){
	    Label tolltipLabel = new Label(text);
	    tolltipLabel.setTooltip(new Tooltip(tooltip));
	    this.setGraphic(tolltipLabel);
	}
	
	private void setTooltipToColumn(String tooltip){
	    Label tolltipLabel = new Label(this.getText());
	    tolltipLabel.setTooltip(new Tooltip(tooltip));
	    this.setGraphic(tolltipLabel);
	}

	public String getTooltip() {
		return tooltipText;
	}

	public void setTooltip(String tooltip) {
		this.tooltipText = tooltip;
	}

}
