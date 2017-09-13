
public enum ColumnColor {
	WHITE("white"),
	BLACK("black"),
	YELLOW("yellow"),
	GREEN("green"),
	RED("red"),
	PINK("pink");
	
	private String color;
	
	ColumnColor(String color){
		this.color = color;
	}
	
	public String getColor(){
		return this.color;
	}
	
}
