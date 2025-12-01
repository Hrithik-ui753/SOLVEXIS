package solvexis.service.transport;

import java.time.LocalTime;
import java.util.*;

public class LastMileService {
    private static final int MORNING_PEAK_START = 8;
    private static final int MORNING_PEAK_END = 11;
    private static final int EVENING_PEAK_START = 17;
    private static final int EVENING_PEAK_END = 21;

    public List<TransportOption> getAvailableOptions(String destination, double distance) {
        List<TransportOption> options = new ArrayList<>();
        boolean isPeakHour = isPeakHour();
        
        for (TransportMode mode : TransportMode.values()) {
            double fare = mode.calculateFare(distance, isPeakHour);
            int estimatedTime = calculateEstimatedTime(mode, distance);
            options.add(new TransportOption(mode, fare, estimatedTime, distance));
        }
        
        Collections.sort(options);
        return options;
    }

    private boolean isPeakHour() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        
        return (hour >= MORNING_PEAK_START && hour < MORNING_PEAK_END) ||
               (hour >= EVENING_PEAK_START && hour < EVENING_PEAK_END);
    }

    private int calculateEstimatedTime(TransportMode mode, double distance) {
        int baseSpeedKmph;
        if (mode == TransportMode.RTC_BUS) {
            baseSpeedKmph = 20;
        } else if (mode == TransportMode.AUTO) {
            baseSpeedKmph = 25;
        } else if (mode == TransportMode.OLA || mode == TransportMode.UBER) {
            baseSpeedKmph = 30;
        } else if (mode == TransportMode.RAPIDO || mode == TransportMode.E_BIKE) {
            baseSpeedKmph = 35;
        } else {
            baseSpeedKmph = 25;
        }
        
        return (int) Math.ceil((distance / baseSpeedKmph) * 60) + 5;
    }

    public static class TransportOption implements Comparable<TransportOption> {
        private final TransportMode mode;
        private final double fare;
        private final int estimatedMinutes;
        private final double distance;

        public TransportOption(TransportMode mode, double fare, int estimatedMinutes, double distance) {
            this.mode = mode;
            this.fare = fare;
            this.estimatedMinutes = estimatedMinutes;
            this.distance = distance;
        }

        public String getFormattedDetails() {
            return String.format("%s\n   Fare: ₹%.0f | Travel Time: %d minutes | Distance: %.1f km",
                               mode.getDisplayName(), fare, estimatedMinutes, distance);
        }

        public TransportMode getMode() {
            return mode;
        }

        public double getFare() {
            return fare;
        }

        public int getEstimatedMinutes() {
            return estimatedMinutes;
        }

        @Override
        public int compareTo(TransportOption other) {
            return Double.compare(this.fare, other.fare);
        }
    }
}