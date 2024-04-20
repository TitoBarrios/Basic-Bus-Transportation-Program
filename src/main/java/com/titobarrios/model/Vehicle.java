package com.titobarrios.model;

import java.util.Arrays;

import com.titobarrios.constants.Value;
import com.titobarrios.db.CurrentDate;
import com.titobarrios.db.DB;

public abstract class Vehicle {

	public static enum Type {
		AIRPLANE("avión", "aviones", "Avión"), BUS("bus", "buses", "Bus"), SHIP("barco", "barcos", "Barco"),
		TRAVEL_BUS("bus de viaje", "buses de viaje", "Bus de Viaje");

		private Type(String name, String pluralName, String upperCaseName) {
			this.name = name;
			this.pluralName = pluralName;
			this.upperCaseName = upperCaseName;
		}

		private String name;
		private String pluralName;
		private String upperCaseName;

		public String getName() {
			return name;
		}

		public String getPluralName() {
			return pluralName;
		}

		public String getUpperCaseName() {
			return upperCaseName;
		}
	}

	private final static int REVENUE_Y = 2;
	private final static int STATISTICS_TYPES = 4;
	private final static int MAX_COUPONS = 20;
	private final static int MAX_TICKETS = 200;
	private final static int MAX_CAPACITY_X = 2;

	private Type type;
	private int[][] revenue;
	private Ticket[] tickets;
	private Coupon[] applicableCoupons;
	private int[] capacity;
	private Company company;
	private RouteSequence routeSeq;
	private String plate;
	private int price;
	private boolean isAvailable;

	public Vehicle(Type type, Company company, String plate, RouteSequence routeSeq, int price, int capacity) {
		this.type = type;
		revenue = new int[STATISTICS_TYPES][REVENUE_Y];
		tickets = new Ticket[MAX_TICKETS];
		applicableCoupons = new Coupon[MAX_COUPONS];
		this.capacity = new int[MAX_CAPACITY_X];
		this.capacity[Value.MAXIMUM.value()] = capacity;
		this.company = company;
		this.routeSeq = routeSeq;
		this.plate = plate;
		this.price = price;
		company.add(type, this);
		DB.store(this);
	}

	public void add(Ticket ticket) {
		for (int i = 0; i < tickets.length; i++)
			if (tickets[i] == null) {
				tickets[i] = ticket;
				break;
			}
	}

	public void add(Coupon coupon) {
		for(int i = 0; i < applicableCoupons.length; i++)
			if(applicableCoupons[i] == null){
				applicableCoupons[i] = coupon;
				break;
			}
		
	}

	public void deleteTicket(int ticketPosition) {
		tickets[ticketPosition] = null;
	}

	public void checkAvailability() {
		boolean isCapacityAvailable = false;
		refreshRouteSeq(this.getRouteSeq(), CurrentDate.get().toLocalDate());

		for (Ticket ticket : this.getTickets()) {
			if (ticket != null) {
				if (CurrentDate.get().isAfter(
						ticket.getRoutes()[Route.StopType.ENTRY.ordinal()].getStops()[Route.StopType.ENTRY.ordinal()])
						|| CurrentDate.get().isAfter(ticket.getRoutes()[Route.StopType.EXIT.ordinal()]
								.getStops()[Route.StopType.EXIT.ordinal()])) {
					setUsersTicketAvailability(ticket, false);
					this.changeCurrentCapacity(this.getCapacity()[Value.CURRENT.value()] - 1);
				} else if (!ticket.getIsAvailable() && (ticket.getRoutes()[Route.StopType.ENTRY.ordinal()]
						.getStops()[Route.StopType.ENTRY.ordinal()].isAfter(CurrentDate.get())
						|| ticket.getRoutes()[Route.StopType.ENTRY.ordinal()].getStops()[Route.StopType.ENTRY.ordinal()]
								.isAfter(CurrentDate.get()))) {
					ticket.setAvailable(true);
					setUsersTicketAvailability(ticket, true);
					this.changeCurrentCapacity(this.getCapacity()[Value.CURRENT.value()] + 1);
				}
			}
		}

		if (this.getCapacity()[Value.MAXIMUM.value()] <= this.getCapacity()[Value.CURRENT.value()]) {
			isCapacityAvailable = false;
		} else {
			isCapacityAvailable = true;
		}

		if (isCapacityAvailable && this.getRouteSeq().getIsAvailable()) {
			this.setAvailable(true);
			return;
		}
		this.setAvailable(false);

	}

	public void checkRevenue(Value statisticsType) {
		int[][] revenue = this.getRevenue();
		int startingTicket = revenue[Value.GENERAL.value()][Value.CURRENT_TICKET.value()] + 1;
		boolean[] firstTime = new boolean[revenue.length];

		for (int i = 0; i < revenue.length; i++) {
			int currentTicketNumber = revenue[i][Value.CURRENT_TICKET.value()] + 1;
			if (currentTicketNumber < startingTicket) {
				startingTicket = currentTicketNumber;
			}
			if (revenue[i][Value.REVENUE.value()] == 0) {
				firstTime[i] = true;
				startingTicket = 0;
			}
		}

		if (this.getTickets()[startingTicket] != null) {
			for (int i = startingTicket; i < this.getTickets().length; i++) {
				if (this.getTickets()[i] != null) {
					int price = this.getTickets()[i].getPrice()[Ticket.PriceType.PAID.ordinal()];
					switch (statisticsType) {
						case GENERAL:
							int generalLastTicketNumber = revenue[Value.GENERAL.value()][Value.CURRENT_TICKET
									.value()];
							if (i > generalLastTicketNumber
									|| (firstTime[Value.GENERAL.value()] && generalLastTicketNumber == i)) {
								revenue[Value.GENERAL.value()][Value.REVENUE.value()] += price;
								revenue[Value.GENERAL.value()][Value.CURRENT_TICKET.value()] = i;
							}
						case YEARLY:
							int yearlyLastTicketNumber = revenue[Value.YEARLY.value()][Value.CURRENT_TICKET
									.value()];
							if (i > yearlyLastTicketNumber
									|| (firstTime[Value.YEARLY.value()]
											&& startingTicket == yearlyLastTicketNumber)) {
								if (this.getTickets()[yearlyLastTicketNumber].getRoutes()[Route.StopType.EXIT
										.ordinal()]
										.getStops()[Route.StopType.EXIT.ordinal()].getYear() != CurrentDate.get().getYear()) {
									revenue[Value.YEARLY.value()][Value.REVENUE.value()] = 0;
								}
								revenue[Value.YEARLY.value()][Value.REVENUE.value()] += price;
								revenue[Value.YEARLY.value()][Value.CURRENT_TICKET.value()] = i;
							}
							if (statisticsType != Value.GENERAL) break;
						case MONTHLY:
							int monthlyLastTicketNumber = revenue[Value.MONTHLY.value()][Value.CURRENT_TICKET
									.value()];
							if (i > monthlyLastTicketNumber
									|| (firstTime[Value.MONTHLY.value()] && monthlyLastTicketNumber == i)) {
								if (this.getTickets()[monthlyLastTicketNumber].getRoutes()[Route.StopType.EXIT
										.ordinal()]
										.getStops()[Route.StopType.EXIT.ordinal()]
										.getMonthValue() != CurrentDate.get().getMonthValue()
										|| this.getTickets()[monthlyLastTicketNumber].getRoutes()[Route.StopType.EXIT
												.ordinal()].getStops()[Route.StopType.EXIT.ordinal()]
												.getYear() != CurrentDate.get().getYear()) {

									revenue[Value.MONTHLY.value()][Value.REVENUE.value()] = 0;
								}
								revenue[Value.MONTHLY.value()][Value.REVENUE.value()] += price;
								revenue[Value.MONTHLY.value()][Value.CURRENT_TICKET.value()] = i;
							}
							if (statisticsType != Value.GENERAL) break;
						case DAILY:
							int dailyLastTicketNumber = revenue[Value.DAILY.value()][Value.CURRENT_TICKET
									.value()];
							if (i > dailyLastTicketNumber
									|| (firstTime[Value.DAILY.value()] && dailyLastTicketNumber == i)) {
								if (this.getTickets()[dailyLastTicketNumber].getRoutes()[Route.StopType.EXIT
										.ordinal()]
										.getStops()[Route.StopType.EXIT.ordinal()]
										.getDayOfMonth() != CurrentDate.get().getDayOfMonth()
										|| this.getTickets()[dailyLastTicketNumber].getRoutes()[Route.StopType.EXIT
												.ordinal()].getStops()[Route.StopType.EXIT.ordinal()]
												.getMonthValue() != CurrentDate.get().getMonthValue()
										|| this.getTickets()[dailyLastTicketNumber].getRoutes()[Route.StopType.EXIT
												.ordinal()].getStops()[Route.StopType.EXIT.ordinal()]
												.getYear() != CurrentDate.get().getYear()) {
									revenue[Value.DAILY.value()][Value.REVENUE.value()] = 0;
								}
								revenue[Value.DAILY.value()][Value.REVENUE.value()] += price;
								revenue[Value.DAILY.value()][Value.CURRENT_TICKET.value()] = i;
							}
							break;
						default:
							break;
					}
				} else {
					break;
				}
			}
		}
	}

	public void changeCurrentCapacity(int currentCapacity) {
		this.capacity[Value.CURRENT.value()] = currentCapacity;
	}

	public Type getType() {
		return type;
	}

	public int[][] getRevenue() {
		return revenue;
	}

	public void setRevenue(int[][] revenue) {
		this.revenue = revenue;
	}

	public Ticket[] getTickets() {
		return tickets;
	}

	public void setTickets(Ticket[] tickets) {
		this.tickets = tickets;
	}

	public Coupon[] getApplicableCoupons() {
		return applicableCoupons;
	}

	public void setApplicableCoupons(Coupon[] applicableCoupons) {
		this.applicableCoupons = applicableCoupons;
	}

	public int[] getCapacity() {
		return capacity;
	}

	public void setCapacity(int[] capacity) {
		this.capacity = capacity;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public RouteSequence getRouteSeq() {
		return routeSeq;
	}

	public void setRouteSequence(RouteSequence routeSeq) {
		this.routeSeq = routeSeq;
	}

	public String getPlate() {
		return plate;
	}

	public void setPlate(String plate) {
		this.plate = plate;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public boolean getIsAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	@Override
	public String toString() {
		return "Vehicle [type=" + type + ", revenue=" + Arrays.toString(revenue) + ", tickets="
				+ Arrays.toString(tickets) + ", applicableCoupons=" + Arrays.toString(applicableCoupons) + ", capacity="
				+ Arrays.toString(capacity) + ", company=" + company + ", routeSeq=" + routeSeq + ", plate=" + plate
				+ ", price=" + price + ", isAvailable=" + isAvailable + "]";
	}
}
