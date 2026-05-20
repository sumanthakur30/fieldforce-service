package com.shopmanagement.fieldforceservice.support;

import java.math.BigDecimal;

public final class GeoDistance {

    private static final double EARTH_RADIUS_M = 6_371_000d;

    private GeoDistance() {
    }

    /** Haversine distance in meters; returns {@link Double#MAX_VALUE} if coords missing. */
    public static double meters(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }
        double φ1 = Math.toRadians(lat1.doubleValue());
        double φ2 = Math.toRadians(lat2.doubleValue());
        double Δφ = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double Δλ = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2)
                + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }
}
