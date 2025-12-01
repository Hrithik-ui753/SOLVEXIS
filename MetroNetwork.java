package solvexis.service;

import solvexis.model.Station;
import solvexis.model.JunctionStation;
import solvexis.model.TerminalStation;
import solvexis.model.NormalStation;
import java.util.*;

public class MetroNetwork {
    private Map<String, Station> stations;
    private List<List<Station>> corridors;

    public MetroNetwork() {
        stations = new HashMap<>();
        corridors = new ArrayList<>();
        initializeCorridors();
    }

    private void initializeCorridors() {
        // Corridor I (Miyapur ↔ LB Nagar)
        List<Station> corridorI = new ArrayList<>();
        addStation(new TerminalStation("Miyapur", 1), corridorI);
        addStation(new NormalStation("JNTU College", 1), corridorI);
        addStation(new NormalStation("KPHB Colony", 1), corridorI);
        addStation(new NormalStation("Kukatpally", 1), corridorI);
        addStation(new NormalStation("Balanagar", 1), corridorI);
        addStation(new NormalStation("Moosapet", 1), corridorI);
        addStation(new NormalStation("Bharat Nagar", 1), corridorI);
        addStation(new NormalStation("Erragadda", 1), corridorI);
        addStation(new NormalStation("ESI Hospital", 1), corridorI);
        addStation(new NormalStation("SR Nagar", 1), corridorI);
        addStation(new JunctionStation("Ameerpet", Arrays.asList(1, 3)), corridorI); // Interchange
        addStation(new NormalStation("Punjagutta", 1), corridorI);
        addStation(new NormalStation("Irrum Manzil", 1), corridorI);
        addStation(new NormalStation("Khairatabad", 1), corridorI);
        addStation(new NormalStation("Lakdikapul", 1), corridorI);
        addStation(new NormalStation("Assembly", 1), corridorI);
        addStation(new NormalStation("Nampally", 1), corridorI);
        addStation(new NormalStation("Gandhi Bhavan", 1), corridorI);
        addStation(new NormalStation("Osmania Medical College", 1), corridorI);
        addStation(new JunctionStation("MG Bus Station", Arrays.asList(1, 2)), corridorI); // Interchange
        addStation(new NormalStation("Malakpet", 1), corridorI);
        addStation(new NormalStation("New Market", 1), corridorI);
        addStation(new NormalStation("Musarambagh", 1), corridorI);
        addStation(new NormalStation("Dilsukhnagar", 1), corridorI);
        addStation(new NormalStation("Chaitanyapuri", 1), corridorI);
        addStation(new NormalStation("Victoria Memorial", 1), corridorI);
        addStation(new TerminalStation("LB Nagar", 1), corridorI);
        corridors.add(corridorI);

        // Corridor II (JBS Parade Ground ↔ MG Bus Station)
        List<Station> corridorII = new ArrayList<>();
        addStation(new TerminalStation("JBS Parade Ground", 2), corridorII);
        addStation(new NormalStation("Secunderabad West", 2), corridorII);
        addStation(new NormalStation("Gandhi Hospital", 2), corridorII);
        addStation(new NormalStation("Musheerabad", 2), corridorII);
        addStation(new NormalStation("RTC Cross Roads", 2), corridorII);
        addStation(new NormalStation("Chikkadpally", 2), corridorII);
        addStation(new NormalStation("Narayanguda", 2), corridorII);
        addStation(new NormalStation("Sultan Bazar", 2), corridorII);
        Station mgBusStation = stations.get("MG Bus Station");
        corridorII.add(mgBusStation); // Reuse existing interchange station
        corridors.add(corridorII);

        // Corridor III (Nagole ↔ Raidurg)
        List<Station> corridorIII = new ArrayList<>();
        addStation(new TerminalStation("Nagole", 3), corridorIII);
        addStation(new NormalStation("Uppal", 3), corridorIII);
        addStation(new NormalStation("Survey of India", 3), corridorIII);
        addStation(new NormalStation("NGRI", 3), corridorIII);
        addStation(new NormalStation("Habsiguda", 3), corridorIII);
        addStation(new NormalStation("Tarnaka", 3), corridorIII);
        addStation(new NormalStation("Mettuguda", 3), corridorIII);
        addStation(new NormalStation("Secunderabad East", 3), corridorIII);
        addStation(new NormalStation("Parade Ground", 3), corridorIII);
        addStation(new NormalStation("Paradise", 3), corridorIII);
        addStation(new NormalStation("Rasoolpura", 3), corridorIII);
        addStation(new NormalStation("Prakash Nagar", 3), corridorIII);
        addStation(new NormalStation("Begumpet", 3), corridorIII);
        Station ameerpet = stations.get("Ameerpet");
        corridorIII.add(ameerpet); // Reuse existing interchange station
        addStation(new NormalStation("Madhura Nagar", 3), corridorIII);
        addStation(new NormalStation("Yusufguda", 3), corridorIII);
        addStation(new NormalStation("Road No-5 Jubilee Hills", 3), corridorIII);
        addStation(new NormalStation("Jubilee Hills Check Post", 3), corridorIII);
        addStation(new NormalStation("Pedamma Temple", 3), corridorIII);
        addStation(new NormalStation("Madhapur", 3), corridorIII);
        addStation(new NormalStation("Durgam Cheruvu", 3), corridorIII);
        addStation(new NormalStation("HITEC City", 3), corridorIII);
        addStation(new TerminalStation("Raidurg", 3), corridorIII);
        corridors.add(corridorIII);

        // Connect stations within each corridor
        connectStationsInCorridor(corridorI);
        connectStationsInCorridor(corridorII);
        connectStationsInCorridor(corridorIII);
    }

    private void addStation(Station station, List<Station> corridor) {
        stations.put(station.getName(), station);
        corridor.add(station);
    }

    private void connectStationsInCorridor(List<Station> corridor) {
        for (int i = 0; i < corridor.size() - 1; i++) {
            Station current = corridor.get(i);
            Station next = corridor.get(i + 1);
            current.addConnection(next);
            next.addConnection(current);
        }
    }

    public Station getStation(String name) {
        return stations.get(name);
    }

    public List<List<Station>> getCorridors() {
        return corridors;
    }

    public Collection<Station> getAllStations() {
        return stations.values();
    }
}