package solvexis.service;
import solvexis.exceptions.InvalidRouteException;
import solvexis.exceptions.StationNotFoundException;
import solvexis.model.*;
import solvexis.data.TripHistory;
import java.util.Arrays;

import java.util.*;

public class MetroRoutePlanner implements RoutePlanner {

    private HashMap<String, Station> stations = new HashMap<>();
    private HashMap<String, List<String>> adj = new HashMap<>();
    private HashMap<String, Integer> edgeLine = new HashMap<>();
    private List<List<String>> lines = new ArrayList<>();

    public MetroRoutePlanner() {
        stations.put("Miyapur", new TerminalStation("Miyapur", 1));
        stations.put("JNTU College", new NormalStation("JNTU College", 1));
        stations.put("KPHB Colony", new NormalStation("KPHB Colony", 1));
        stations.put("Kukatpally", new NormalStation("Kukatpally", 1));
        stations.put("Balanagar", new NormalStation("Balanagar", 1));
        stations.put("Moosapet", new NormalStation("Moosapet", 1));
        stations.put("Bharat Nagar", new NormalStation("Bharat Nagar", 1));
        stations.put("Erragadda", new NormalStation("Erragadda", 1));
        stations.put("ESI Hospital", new NormalStation("ESI Hospital", 1));
        stations.put("SR Nagar", new NormalStation("SR Nagar", 1));
        stations.put("Ameerpet", new JunctionStation("Ameerpet", Arrays.asList(1, 2)));
        stations.put("Punjagutta", new NormalStation("Punjagutta", 1));
        stations.put("Irrum Manzil", new NormalStation("Irrum Manzil", 1));
        stations.put("Khairatabad", new NormalStation("Khairatabad", 1));
        stations.put("Lakdi-ka-pul", new NormalStation("Lakdi-ka-pul", 1));
        stations.put("Assembly", new NormalStation("Assembly", 1));
        stations.put("Nampally", new NormalStation("Nampally", 1));
        stations.put("Gandhi Bhavan", new NormalStation("Gandhi Bhavan", 1));
        stations.put("Osmania Medical College", new NormalStation("Osmania Medical College", 1));
        stations.put("MGBS", new JunctionStation("MGBS", Arrays.asList(1, 3)));
        stations.put("Malakpet", new NormalStation("Malakpet", 1));
        stations.put("New Market", new NormalStation("New Market", 1));
        stations.put("Musarambagh", new NormalStation("Musarambagh", 1));
        stations.put("Dilsukhnagar", new NormalStation("Dilsukhnagar", 1));
        stations.put("Chaitanyapuri", new NormalStation("Chaitanyapuri", 1));
        stations.put("Victoria Memorial", new NormalStation("Victoria Memorial", 1));
        stations.put("LB Nagar", new TerminalStation("LB Nagar", 1));

        // Corridor II (Blue Line) - Nagole ↔ Raidurg
        stations.put("Nagole", new TerminalStation("Nagole", 2));
        stations.put("Uppal", new NormalStation("Uppal", 2));
        stations.put("Stadium", new NormalStation("Stadium", 2));
        stations.put("NGRI", new NormalStation("NGRI", 2));
        stations.put("Habsiguda", new NormalStation("Habsiguda", 2));
        stations.put("Tarnaka", new NormalStation("Tarnaka", 2));
        stations.put("Mettuguda", new NormalStation("Mettuguda", 2));
        stations.put("Secunderabad East", new NormalStation("Secunderabad East", 2));
        stations.put("Parade Ground", new NormalStation("Parade Ground", 2));
        stations.put("Paradise", new NormalStation("Paradise", 2));
        stations.put("Rasoolpura", new NormalStation("Rasoolpura", 2));
        stations.put("Prakash Nagar", new NormalStation("Prakash Nagar", 2));
        stations.put("Begumpet", new NormalStation("Begumpet", 2));
        stations.put("Madhura Nagar", new NormalStation("Madhura Nagar", 2));
        stations.put("Yusufguda", new NormalStation("Yusufguda", 2));
        stations.put("Road No. 5 Jubilee Hills", new NormalStation("Road No. 5 Jubilee Hills", 2));
        stations.put("Jubilee Hills Check Post", new NormalStation("Jubilee Hills Check Post", 2));
        stations.put("Peddamma Gudi", new NormalStation("Peddamma Gudi", 2));
        stations.put("Madhapur", new NormalStation("Madhapur", 2));
        stations.put("Durgam Cheruvu", new NormalStation("Durgam Cheruvu", 2));
        stations.put("Hitec City", new NormalStation("Hitec City", 2));
        stations.put("Raidurg", new TerminalStation("Raidurg", 2));

        // Corridor III (Green Line) - JBS Parade Ground ↔ MGBS
        stations.put("JBS Parade Ground", new TerminalStation("JBS Parade Ground", 3));
        stations.put("Secunderabad West", new NormalStation("Secunderabad West", 3));
        stations.put("Gandhi Hospital", new NormalStation("Gandhi Hospital", 3));
        stations.put("Musheerabad", new NormalStation("Musheerabad", 3));
        stations.put("RTC Cross Roads", new NormalStation("RTC Cross Roads", 3));
        stations.put("Chikkadpally", new NormalStation("Chikkadpally", 3));
        stations.put("Narayanaguda", new NormalStation("Narayanaguda", 3));
        stations.put("Sultan Bazaar", new NormalStation("Sultan Bazaar", 3));
    }

    {
        List<String> red = Arrays.asList(
            "Miyapur", "JNTU College", "KPHB Colony", "Kukatpally", "Balanagar", 
            "Moosapet", "Bharat Nagar", "Erragadda", "ESI Hospital", "SR Nagar", 
            "Ameerpet", "Punjagutta", "Irrum Manzil", "Khairatabad", "Lakdi-ka-pul", 
            "Assembly", "Nampally", "Gandhi Bhavan", "Osmania Medical College", 
            "MGBS", "Malakpet", "New Market", "Musarambagh", "Dilsukhnagar", 
            "Chaitanyapuri", "Victoria Memorial", "LB Nagar"
        );
        
        List<String> blue = Arrays.asList(
            "Nagole", "Uppal", "Stadium", "NGRI", "Habsiguda", "Tarnaka", 
            "Mettuguda", "Secunderabad East", "Parade Ground", "Paradise", "Rasoolpura", 
            "Prakash Nagar", "Begumpet", "Ameerpet", "Madhura Nagar", "Yusufguda", 
            "Road No. 5 Jubilee Hills", "Jubilee Hills Check Post", "Peddamma Gudi", 
            "Madhapur", "Durgam Cheruvu", "Hitec City", "Raidurg"
        );
        
        List<String> green = Arrays.asList(
            "JBS Parade Ground", "Secunderabad West", "Gandhi Hospital", "Musheerabad", 
            "RTC Cross Roads", "Chikkadpally", "Narayanaguda", "Sultan Bazaar", "MGBS"
        );

        lines.add(red);
        lines.add(blue);
        lines.add(green);

        for (int li = 0; li < lines.size(); li++) {
            List<String> line = lines.get(li);
            for (int i = 0; i < line.size(); i++) {
                String s = line.get(i);
                adj.putIfAbsent(s, new ArrayList<>());
                if (i + 1 < line.size()) {
                    String t = line.get(i + 1);
                    adj.get(s).add(t);
                    adj.putIfAbsent(t, new ArrayList<>());
                    adj.get(t).add(s);
                    String key = makeEdgeKey(s, t);
                    edgeLine.put(key, li);
                }
            }
        }
    }

    public Set<String> getStationNames() {
        return stations.keySet();
    }

    public void printStations() {
        System.out.println("\nCorridor I (Red Line) - Miyapur ↔ LB Nagar:");
        List<String> redLine = lines.get(0);
        for (String name : redLine) {
            System.out.println("  • " + name);
        }
        
        System.out.println("\nCorridor II (Blue Line) - Nagole ↔ Raidurg:");
        List<String> blueLine = lines.get(1);
        for (String name : blueLine) {
            System.out.println("  • " + name);
        }
        
        System.out.println("\nCorridor III (Green Line) - JBS Parade Ground ↔ MGBS:");
        List<String> greenLine = lines.get(2);
        for (String name : greenLine) {
            System.out.println("  • " + name);
        }
    }

    public List<Station> getStationsOnRoute(String start, String end) throws StationNotFoundException, InvalidRouteException {
        List<String> pathNames = findRoute(start, end);
        List<Station> stationsList = new ArrayList<>();
        for (String name : pathNames) {
            stationsList.add(stations.get(name));
        }
        return stationsList;
    }

    public List<String> findRoute(String start, String end) throws StationNotFoundException, InvalidRouteException {
        if (!stations.containsKey(start))
            throw new StationNotFoundException("Start station not found: " + start);

        if (!stations.containsKey(end))
            throw new StationNotFoundException("End station not found: " + end);

        if (start.equalsIgnoreCase(end))
            throw new InvalidRouteException("Start and end stations cannot be the same!");

        List<String> q = new ArrayList<>();
        Map<String, String> prev = new HashMap<>();
        q.add(start);
        prev.put(start, null);

        int index = 0;
        while (index < q.size()) {
            String cur = q.get(index);
            index++;
            if (cur.equals(end)) break;
            List<String> neighbors = adj.getOrDefault(cur, Collections.emptyList());
            for (String nb : neighbors) {
                if (!prev.containsKey(nb)) {
                    prev.put(nb, cur);
                    q.add(nb);
                }
            }
        }

        if (!prev.containsKey(end)) {
            throw new InvalidRouteException("No route found between " + start + " and " + end);
        }

        List<String> path = new ArrayList<>();
        String cur = end;
        while (cur != null) {
            path.add(cur);
            cur = prev.get(cur);
        }
        Collections.reverse(path);
        return path;
    }

    public int estimateTimeMinutes(List<String> path) {
        if (path == null || path.size() <= 1) return 0;
        int hops = path.size() - 1;
        int timePerStop = 2;
        int transferPenalty = 3;

        int transfers = 0;
        Integer prevLine = null;
        for (int i = 0; i < path.size() - 1; i++) {
            String a = path.get(i);
            String b = path.get(i + 1);
            String key = makeEdgeKey(a, b);
            Integer lineIdx = edgeLine.get(key);
            if (lineIdx == null) continue;
            if (prevLine != null && !prevLine.equals(lineIdx)) transfers++; 
            prevLine = lineIdx;
        }

        return hops * timePerStop + transfers * transferPenalty;
    }

    private String makeEdgeKey(String a, String b) {
        if (a.compareTo(b) <= 0) return a + "|" + b;
        return b + "|" + a;
    }

    public double calculateFareBetween(String start, String end) throws StationNotFoundException {
        if (!stations.containsKey(start))
            throw new StationNotFoundException("Start station not found: " + start);
        if (!stations.containsKey(end))
            throw new StationNotFoundException("End station not found: " + end);
        
        try {
            List<String> path = findRoute(start, end);
            int numStations = path.size();
            
            if (numStations <= 2) {
                return 10.0;
            } else if (numStations <= 5) {
                return 15.0;
            } else if (numStations <= 9) {
                return 20.0;
            } else if (numStations <= 16) {
                return 30.0;
            } else {
                return 40.0;
            }
        } catch (InvalidRouteException e) {
            return 20.0;
        }
    }

    @Override
    public void planRoute(String start, String end) {
        try {
            if (!stations.containsKey(start))
                throw new StationNotFoundException("Start station not found: " + start);

            if (!stations.containsKey(end))
                throw new StationNotFoundException("End station not found: " + end);

            if (start.equalsIgnoreCase(end))
                throw new InvalidRouteException("Start and end stations cannot be the same!");

            Station s1 = stations.get(start);
            Station s2 = stations.get(end);

            double fare = s1.calculateFare() + s2.calculateFare();

            System.out.println("\nRoute Found!");
            System.out.println("From: " + start + " To: " + end);
            System.out.println("Fare: ₹" + fare);

            List<Station> route = new ArrayList<>();
            route.add(s1);
            route.add(s2);
            TripHistory.saveCompleteTrip(route, null, null, fare);

        } catch (StationNotFoundException | InvalidRouteException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
