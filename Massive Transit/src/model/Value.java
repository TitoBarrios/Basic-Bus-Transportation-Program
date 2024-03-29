package model;

public enum Value {
	FRIEND(0), FAMILIAR(1),
	USER(0), COMPANY(1),
	ENTRY(0), EXIT(1),
	CURRENT(0), MAXIMUM(1),
	GENERAL(0), YEARLY(1), MONTHLY(2), DAILY(3),
	REVENUE(0), CURRENT_TICKET(1);
	
	Value (int value) {
		this.value = value;
	}
	
	private int value;
	
	public int getValue() {
		return value;
	}
}
