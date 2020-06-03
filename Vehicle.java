/**
 * Class to represent each vehicle
 */
public class Vehicle {
    private int index;
    private int home;
    private int starting_time;
    private int capacity;
    private int list_of_available_calls[];


    /**
     * constructor
     *
     * @param index index of vehicle
     * @param home home node for vehicle
     * @param starting_time starting time for vehicle
     * @param capacity capasity of vehicle
     */
    public Vehicle(int index, int home, int starting_time, int capacity){
        this.index = index;
        this.home = home;
        this.starting_time = starting_time;
        this.capacity = capacity;
    }

    /**
     * format output to print this vehicle
     * @return string to print
     */
    public String toString() {
        return String.format("Vehicle index: %s \nHome: %s \nStarting time: %s \nCapacity: %s ", index, home, starting_time, capacity);
    }

    /**
     *
     * @return int capasity of vehicle
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     *
     * @return int starting time of vehicle
     */
    public int getStarting_time() {
        return starting_time;
    }

    /**
     *
     * @returnint home node for vehicle
     */
    public int getHome() {
        return home;
    }

    /**
     *
     * @return int index of vehicle (e.g 1,2,3,4,5 NOT 0)
     */
    public int getIndex() {
        return index;
    }

    /**
     * calls that this vehicle can handle
     * @return int[] list of available calls for this vehicle
     */
    public int[] getList_of_available_calls() {
        return list_of_available_calls;
    }

    /**
     *
     * @param index of call to check
     * @return boolean false if vehicle cant handle this call(based on input file)
     */
    public boolean isCallAvailableForThisVehicle(int index){
        for (int i = 0; i < list_of_available_calls.length; i++) {
            if (list_of_available_calls[i]==index) return true;
        }
        return false;
    }

    /**
     * set the list of calls available to this vehicle
     * @param list_of_available_calls list of calls available for this vehicle
     */
    public void setList_of_available_calls(int[] list_of_available_calls) {
        this.list_of_available_calls = list_of_available_calls;
    }
}
