import java.util.ArrayList;

final class VehicleStatistics {

    public boolean feasable;
    public int travel_time;
    public int total_idle_time;
    public int vehicle_index;
    public ArrayList<int[]> idle_time;

    public VehicleStatistics(boolean feasable, int travel_time, int total_idle_time, int vehicle_index, ArrayList<int[]> idle_time) {
        this.feasable = feasable;
        this.travel_time = travel_time;
        this.idle_time = idle_time;
        this.vehicle_index = vehicle_index;
        this.total_idle_time = total_idle_time;
    }

}
