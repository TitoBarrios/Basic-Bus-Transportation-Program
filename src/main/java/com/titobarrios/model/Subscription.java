package com.titobarrios.model;

import java.time.DayOfWeek;
import java.util.Arrays;

public class Subscription {
	private Route[] routes;
	private DayOfWeek dayOfWeek;
	private Vehicle vehicle;

	public Subscription(User user, DayOfWeek dayOfWeek, Vehicle vehicle, Route[] routes) {
		this.dayOfWeek = dayOfWeek;
		this.vehicle = vehicle;
		this.routes = routes;
		user.add(this);
	}

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public Route[] getRoutes() {
		return routes;
	}

	public void setRoutes(Route[] routes) {
		this.routes = routes;
	}

	@Override
	public String toString() {
		return "Subscription [routes=" + Arrays.toString(routes) + ", dayOfWeek=" + dayOfWeek + ", vehicle=" + vehicle
				+ "]";
	}
}