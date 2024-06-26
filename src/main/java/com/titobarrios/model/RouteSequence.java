package com.titobarrios.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

import com.titobarrios.constants.VType;
import com.titobarrios.db.CurrentDate;
import com.titobarrios.db.DB;
import com.titobarrios.model.interfaces.Id;
import com.titobarrios.utils.LaboralDays;

public class RouteSequence implements Id {
	private VType type;
	private DayOfWeek[] laboralDays;
	private Route[] routes;
	private ArrayList<Coupon> coupons;
	private Company owner;
	private ArrayList<Vehicle> vehicles;
	private String id;
	private boolean isAvailable;

	private int[] revenue;
	private LocalDateTime lastCheck;

	public RouteSequence(Company owner, VType type, String id, LocalTime initialTime, DayOfWeek[] laboralDays,
			int stopsNumber, int[] timeLapse) {
		this.owner = owner;
		this.type = type;
		this.id = id;
		this.laboralDays = laboralDays;
		coupons = new ArrayList<Coupon>();
		vehicles = new ArrayList<Vehicle>();
		revenue = new int[4];
		lastCheck = CurrentDate.get();
		initialize(initialTime, stopsNumber, timeLapse);
		refresh();
	}

	private void initialize(LocalTime initialTime, int stopsNumber, int[] timeLapse) {
		routes = new Route[stopsNumber - 1];
		LocalDateTime[][] stops = new LocalDateTime[stopsNumber][2];
		stops[0][Route.StopType.ENTRY.ordinal()] = LaboralDays.adjustTime(laboralDays,
				LocalDateTime.of(CurrentDate.get().toLocalDate(), initialTime));
		stops[0][Route.StopType.EXIT.ordinal()] = LaboralDays.adjustTime(laboralDays,
				stops[0][Route.StopType.ENTRY.ordinal()].plusMinutes(timeLapse[0]));
		routes[0] = new Route("0 a 1", new LocalDateTime[] { stops[0][Route.StopType.ENTRY.ordinal()],
				stops[0][Route.StopType.EXIT.ordinal()] }, new String[] { "0", "1" });
		for (int i = 1; i < routes.length; i++) {
			stops[i][Route.StopType.ENTRY.ordinal()] = stops[i - 1][Route.StopType.EXIT.ordinal()];
			stops[i][Route.StopType.EXIT.ordinal()] = LaboralDays.adjustTime(laboralDays,
					stops[i][Route.StopType.ENTRY.ordinal()].plusMinutes(timeLapse[i]));
			routes[i] = new Route(i + " a " + (i + 1), new LocalDateTime[] { stops[i][Route.StopType.ENTRY.ordinal()],
					stops[i][Route.StopType.EXIT.ordinal()] }, new String[] { i + "", (i + 1) + "" });
		}
		DB.store(this);
		owner.add(this);
	}

	public void refresh() {
		CurrentDate.refresh();
		LocalDate currentDate = CurrentDate.get().toLocalDate();
		if (this.getRoutes()[0].getStops()[Route.StopType.ENTRY.ordinal()].getDayOfMonth() != currentDate
				.getDayOfMonth()) {
			for (Route route : this.getRoutes()) {
				route.setStops(new LocalDateTime[] { LocalDateTime.of(currentDate,
						route.getStops()[Route.StopType.ENTRY.ordinal()].toLocalTime()),
						LocalDateTime.of(currentDate,
								route.getStops()[Route.StopType.EXIT.ordinal()].toLocalTime()) });
			}
		}
		checkAvailability();
	}

	public void checkAvailability() {
		this.setAvailable(isLaboralDay() && isAnyRouteAvailable() ? true : false);
	}

	private boolean isLaboralDay() {
		for (DayOfWeek laboralDay : this.getLaboralDays())
			if (laboralDay.equals(CurrentDate.get().getDayOfWeek()))
				return true;
		return false;
	}

	private boolean isAnyRouteAvailable() {
		for (Route route : this.getRoutes())
			if (route.getIsAvailable())
				return true;
		return false;
	}

	public void add(Coupon coupon) {
		coupons.add(coupon);
	}

	public void add(Vehicle vehicle) {
		vehicles.add(vehicle);
	}

	public void remove(Coupon coupon) {
		coupons.remove(coupon);
	}

	public void remove(Vehicle vehicle) {
		vehicles.remove(vehicle);
	}

	public VType getType() {
		return type;
	}

	public void setType(VType type) {
		this.type = type;
	}

	public DayOfWeek[] getLaboralDays() {
		return laboralDays;
	}

	public void setLaboralDays(DayOfWeek[] laboralDays) {
		this.laboralDays = laboralDays;
	}

	public Route[] getRoutes() {
		return routes;
	}

	public void setRoutes(Route[] routes) {
		this.routes = routes;
	}

	public Coupon[] getCoupons() {
		return coupons.toArray(Coupon[]::new);
	}

	public Company getOwner() {
		return owner;
	}

	public void setOwner(Company owner) {
		this.owner = owner;
	}

	public Vehicle[] getVehicles() {
		return vehicles.toArray(Vehicle[]::new);
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public int[] getRevenue() {
		return revenue;
	}

	public LocalDateTime getLastCheck() {
		return lastCheck;
	}

	public void delete() {
		DB.remove(this);
		owner.remove(this);
		for (Coupon coupon : coupons)
			coupon.remove(this);
	}

	public String info() {
		StringBuilder builder = new StringBuilder();
		builder.append(id).append("	").append(owner.getId()).append("\nDías activo: ");
		for (int i = 0; i < laboralDays.length; i++)
			builder.append(laboralDays[i]).append(i == laboralDays.length - 1 ? "." : ", ");
		builder.append("\n").append(isAvailable ? "Disponible" : "No disponible");
		builder.append("\n\nRutas:\n");
		for (Route route : routes)
			builder.append(route.info()).append("\n");
		builder.append("\nVehículos:");
		for (Vehicle vehicle : vehicles)
			builder.append("	").append(vehicle.getPlate()).append(" ").append(vehicle.getCompany().getId())
					.append("\n	").append("Precio: ").append(vehicle.getPrice()).append("	. ")
					.append(vehicle.isAvailable() ? "Disponible" : "No disponible").append("\n");
		return builder.toString();
	}
}
