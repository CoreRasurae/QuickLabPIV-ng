package pt.quickLabPIV.exporter;

public enum Matlab5ArrayFlags {
	Complex(1 << 4),
	Global(1 << 5),
	Logical(1 << 6), 
	None(0);
	
	private byte flagValue;

	Matlab5ArrayFlags(int flag) {
		flagValue = (byte)flag;
	}
	
	public byte getFlagValue() {
		return flagValue;
	}
}
