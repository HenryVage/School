/**
 * Class to represent calls
 */

public class Call {
    private int call_index;
    private int origin_node;
    private int destination_node;
    private int size;
    private int cost_of_not_transporting;
    private int lowerbound_for_pickup;
    private int upperbound_for_pickup;
    private int upperbound_for_delivery;
    private int lowerbound_for_delivery;


    /**
     * Constructor
     *
     * @param call_index index of call
     * @param origin_node origin node for call
     * @param destination_node ddestination node for call
     * @param size size of the package/call
     * @param cost_of_not_transporting cost of not transporting the call
     * @param lowerbound_for_pickup earliest time the package can be picked up
     * @param upperbound_for_pickup latest time the package can be picked up
     * @param lowerbound_for_delivery latest time the package can be dropped off
     * @param upperbound_for_delivery earliest time the package can be dropped off
     */
    public Call(int call_index, int origin_node, int destination_node, int size, int cost_of_not_transporting,
                int lowerbound_for_pickup, int upperbound_for_pickup, int lowerbound_for_delivery, int upperbound_for_delivery) {
        this.call_index = call_index;
        this.origin_node = origin_node;
        this.destination_node = destination_node;
        this.size = size;
        this.cost_of_not_transporting = cost_of_not_transporting;
        this.lowerbound_for_pickup = lowerbound_for_pickup;
        this.upperbound_for_pickup = upperbound_for_pickup;
        this.lowerbound_for_delivery = lowerbound_for_delivery;
        this.upperbound_for_delivery = upperbound_for_delivery;
    }

    /**
     * prints the call
     * @return String formatted output
     */
    public String toString() {
        return String.format("Call index: %s \nOrgin: %s \nDestination: %s \nSize: %s \nCost of not transporting: %s \n" +
                "Lowerbound for pickup: %s \nUpperbound for pickup: %s \nLowerbound for delivery: %s \nUpperbound for delivery: %s",
                call_index, origin_node, destination_node, size, cost_of_not_transporting, lowerbound_for_pickup, upperbound_for_pickup,
                lowerbound_for_delivery, upperbound_for_delivery);
    }

    /**
     * get index of call (e.g 1,2,3,4,5 etc NOT 0)
     * @return int index
     */
    public int getCall_index() {
        return call_index;
    }

    /**
     * get origin node of call
     * @return int origin node
     */
    public int getOrigin_node() {
        return origin_node;
    }

    /**
     * get destination node of call
     * @return int destination node
     */
    public int getDestination_node() {
        return destination_node;
    }

    /**
     * get size of package
     * @return int size
     */
    public int getSize() {
        return size;
    }

    /**
     * get cost of not transporting
     * @return int cost of not transporting
     */
    public int getCost_of_not_transporting() {
        return cost_of_not_transporting;
    }

    /**
     *
     * @return int upperbound for pickup
     */
    public int getUpperbound_for_pickup() {
        return upperbound_for_pickup;
    }

    /**
     *
     * @return int lowerbound for pickup
     */
    public int getLowerbound_for_pickup() {
        return lowerbound_for_pickup;
    }

    /**
     *
     * @return int upperbound for delivery
     */
    public int getUpperbound_for_delivery() {
        return upperbound_for_delivery;
    }

    /**
     *
     * @return int lowerbound for delivery
     */
    public int getLowerbound_for_delivery() {
        return lowerbound_for_delivery;
    }
}
