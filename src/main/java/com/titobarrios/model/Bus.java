package com.titobarrios.model;

import com.titobarrios.constants.VType;

public class Bus extends Vehicle {
	public Bus(Company company, String plate, RouteSequence routeSeq, int price, int capacity) {
		super(VType.BUS, company, plate, routeSeq, price, capacity);
	}
}