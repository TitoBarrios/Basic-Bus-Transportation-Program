package com.titobarrios.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;

import com.titobarrios.db.DB;
import com.titobarrios.model.Coupon;
import com.titobarrios.model.Route;
import com.titobarrios.model.RouteSequence;
import com.titobarrios.model.Vehicle;
import com.titobarrios.utils.ArraysUtil;
import com.titobarrios.utils.Maths;

public class CouponCtrl {

    public static void editObjects(Coupon coupon, Vehicle[] vehicles) {
        deleteObjects(coupon);
        coupon.setApplicable(Coupon.AppliesTo.VEHICLES);
        for (Vehicle vehicle : vehicles)
            vehicle.add(coupon);
        coupon.setApplicableVehicles(vehicles);
    }

    public static void editObjects(Coupon coupon, RouteSequence[] routeSeqs) {
        deleteObjects(coupon);
        coupon.setApplicable(Coupon.AppliesTo.ROUTE_SEQS);
        for (RouteSequence routeSeq : routeSeqs)
            routeSeq.add(coupon);
        coupon.setApplicableRouteSeqs(routeSeqs);
    }

    public static void editObjects(Coupon coupon, Route[] routes) {
        deleteObjects(coupon);
        coupon.setApplicable(Coupon.AppliesTo.ROUTES);
        for (Route route : routes)
            route.add(coupon);
        coupon.setApplicableRoutes(routes);
    }

    public static void deleteObjects(Coupon coupon) {
        deleteCouponFromObjects(coupon);
        coupon.setApplicableVehicles(null);
        coupon.setApplicableRouteSeqs(null);
        coupon.setApplicableRoutes(null);
    }

    private static void deleteCouponFromObjects(Coupon coupon) {
        switch (coupon.getApplicable()) {
            case VEHICLES:
                for (Vehicle vehicle : coupon.getApplicableVehicles())
                    ArraysUtil.deleteSlot(vehicle.getApplicableCoupons(), coupon);
                break;
            case ROUTE_SEQS:
                for (RouteSequence routeSeq : coupon.getApplicableRouteSeqs())
                    ArraysUtil.deleteSlot(routeSeq.getApplicableCoupons(), coupon);
                break;
            case ROUTES:
                for (Route route : coupon.getApplicableRoutes())
                    ArraysUtil.deleteSlot(route.getApplicableCoupons(), coupon);
                break;
            default:
                break;
        }
    }

    public static Coupon[] filterAvailable(Vehicle vehicle, RouteSequence routeSeq, Route[] routes) {
        Coupon[] coupons = ArraysUtil.combineArrays(filterAvailable(vehicle.getApplicableCoupons()),
                filterAvailable(routeSeq.getApplicableCoupons()));
        for (Route route : routes)
            ArraysUtil.combineArrays(coupons, route.getApplicableCoupons());
        return coupons;
    }

    public static Coupon[] filterAvailable(Coupon[] coupons) {
        ArrayList<Coupon> filtered = new ArrayList<Coupon>();
        for (Coupon coupon : coupons) {
            coupon.checkAvailability();
            if (coupon.isAvailable())
                filtered.add(coupon);
        }
        return filtered.toArray(Coupon[]::new);
    }

    public Coupon[] filterPublic(Coupon[] coupons) {
        ArrayList<Coupon> publics = new ArrayList<Coupon>();
        for (Coupon coupon : coupons)
            if (coupon.getType().equals(Coupon.Type.PUBLIC))
                publics.add(coupon);
        return publics.toArray(new Coupon[publics.size()]);
    }

    public Coupon findBestCoupon(Coupon[] coupons, int price) {
        Coupon bestCoupon = null;
        int bestPrice = Integer.MAX_VALUE;
        for (Coupon coupon : coupons)
            if (coupon.getType() == Coupon.Type.PUBLIC) {
                int currentCouponPrice = discountedPrice(coupon, price);
                if (currentCouponPrice < bestPrice) {
                    bestCoupon = coupon;
                    bestPrice = currentCouponPrice;
                }
            }
        return bestCoupon;
    }

    public boolean isRedeemWordAvailable(String redeemWord) {
        for (Coupon coupon : DB.getCoupons())
            if (coupon.getRedeemWord().equals(redeemWord))
                return false;
        return true;
    }

    public Coupon searchCouponByWord(String redeemWord) {
		for (Coupon coupon : DB.getCoupons())
				if (coupon.getRedeemWord().equals(redeemWord))
					return coupon;
		return null;
	}

    public static int discountedPrice(Coupon coupon, int price) {
        switch (coupon.getDiscountType()) {
            case QUANTITY:
                if (price - coupon.getDiscount() <= 0)
                    return 0;
                return price - coupon.getDiscount();
            case PERCENTAGE:
                return price - new Maths().calculatePercentage(coupon.getDiscount(), price);
            default:
                return 0;
        }
    }
}
