package solvexis.service.transport;

public class TransportMode {
    public static final TransportMode RTC_BUS = new TransportMode(10.0, 1.2, "TSRTC Bus", 5.0);
    public static final TransportMode AUTO = new TransportMode(15.0, 1.3, "Auto", 2.0);
    public static final TransportMode OLA = new TransportMode(20.0, 1.4, "Ola Cab", 3.0);
    public static final TransportMode UBER = new TransportMode(20.0, 1.4, "Uber", 3.0);
    public static final TransportMode RAPIDO = new TransportMode(12.0, 1.2, "Rapido Bike", 2.0);
    public static final TransportMode E_BIKE = new TransportMode(8.0, 1.0, "Metro E-Bike", 1.0);
    
    private static final TransportMode[] ALL_MODES = {RTC_BUS, AUTO, OLA, UBER, RAPIDO, E_BIKE};

    private final double baseFarePerKm;
    private final double peakMultiplier;
    private final String displayName;
    private final double minimumDistance;

    private TransportMode(double baseFarePerKm, double peakMultiplier, String displayName, double minimumDistance) {
        this.baseFarePerKm = baseFarePerKm;
        this.peakMultiplier = peakMultiplier;
        this.displayName = displayName;
        this.minimumDistance = minimumDistance;
    }

    public static TransportMode[] values() {
        return ALL_MODES.clone();
    }

    public double calculateFare(double distance, boolean isPeakHour) {
        double effectiveDistance = Math.max(distance, minimumDistance);
        double fare = baseFarePerKm * effectiveDistance;
        
        if (isPeakHour) {
            fare *= peakMultiplier;
        }
        
        if (this == OLA || this == UBER) {
            fare += 25.0;
        }
        
        return Math.ceil(fare / 5.0) * 5.0;
    }

    public double getBaseFarePerKm() {
        return baseFarePerKm;
    }

    public double getPeakMultiplier() {
        return peakMultiplier;
    }

    public double getMinimumDistance() {
        return minimumDistance;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getFareEstimate(double distance, boolean isPeakHour) {
        double fare = calculateFare(distance, isPeakHour);
        String peakInfo = isPeakHour ? " (Peak Hours)" : "";
        return String.format("%s - ₹%.0f for %.1f km%s", 
                           displayName, fare, Math.max(distance, minimumDistance), peakInfo);
    }
}
