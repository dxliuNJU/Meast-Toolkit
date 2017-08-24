package name.dxliu.associations;

public enum PruningStrategy {
	BSC,PRN,PRN_1;
	@Override
	public String toString() {
		if(this.equals(PruningStrategy.BSC))
			return "BSC";
		if(this.equals(PruningStrategy.PRN))
			return "PRN";
		if(this.equals(PruningStrategy.PRN_1))
			return "PRN_1";
		return null;
	}
	
}
