import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class to read input file
 */
public class ReadFromFile {

    /**
     * Static method to read spesific file format and initialize Assignment2 class
     * @param abs_filepath filepath to file to read
     * @param print wether or not to print status to console
     * @return Assignment2 class initialized
     * @throws IOException if file not found
     */
    public static Assignment2 read_from_file(String abs_filepath, boolean print) throws IOException {


        //initializing file and scanner
        Path path = Paths.get(abs_filepath);
        Scanner sc = new Scanner(path);

        //start scanning number of nodes and vehicles
        if(print)System.out.println(sc.nextLine());
        else sc.nextLine();
        int number_of_nodes = sc.nextInt();
        if(print)System.out.println("number of nodes: " + number_of_nodes);
        if(print)System.out.println(sc.nextLine());
        else sc.nextLine();
        if(print)System.out.println(sc.nextLine());
        else sc.nextLine();
        int number_of_vehicles = sc.nextInt();
        if(print)System.out.println("number of vehicles: " + number_of_vehicles);
        if(print)System.out.println(sc.nextLine());
        else sc.nextLine();
        if(print)System.out.println(sc.nextLine());
        else sc.nextLine();

        //reading vehicles, creating an object Vehicle for each, storing them in list_of_vehicles
        ArrayList<Vehicle> list_of_vehicles= new ArrayList<>();
        for (int i = 0; i < number_of_vehicles; i++) {
            String line = sc.nextLine();
            String[] list = line.split(",");
            int index = Integer.parseInt(list[0]);
            if(print)System.out.println("index: "+ index);
            int home = Integer.parseInt(list[1]);
            if(print)System.out.println("home: "+ home);
            int starting_time = Integer.parseInt(list[2]);
            if(print)System.out.println("starting time: "+ starting_time);
            int capacity = Integer.parseInt(list[3]);
            if(print)System.out.println("capacity: "+ capacity);
            Vehicle vehicle = new Vehicle(index, home, starting_time, capacity);
            list_of_vehicles.add(vehicle);
            if(print)System.out.println();
            if(print)System.out.println(vehicle.toString());
            if(print)System.out.println();
        }

        //reading available calls for each vehicle, adding that list to their respective vehicle objects
        if(print)System.out.println(sc.nextLine());
        else sc.nextLine();
        int number_of_calls = sc.nextInt();
        if(print)System.out.println(sc.nextLine());
        else sc.nextLine();
        if(print)System.out.println(sc.nextLine());
        else sc.nextLine();
        for (int i = 0; i < number_of_vehicles; i++) {
            String line2 = sc.nextLine();
            String[] list2 = line2.split(",");
            int[] list_of_calls_for_vehicle = new int[list2.length-1];
            for (int j = 1; j < list2.length; j++) {
                //System.out.println("vehicle: "+list2[0]+"available call: "+ Integer.parseInt(list2[j])+" listlength "+ list2.length);
                list_of_calls_for_vehicle[j-1] = Integer.parseInt(list2[j]);
            }
            list_of_vehicles.get(Integer.parseInt(list2[0])-1).setList_of_available_calls(list_of_calls_for_vehicle);
        }

        //reading calls, creating object for each call, storing them in list_of_calls
        ArrayList<Call> list_of_calls = new ArrayList<>();
        if(print)System.out.println(sc.nextLine());
        else sc.nextLine();
        for (int i = 0; i < number_of_calls; i++) {
            String line3 = sc.nextLine();
            if(print)System.out.println("Read line: "+line3);
            String[] list3 = line3.split(",");
            int call_index = Integer.parseInt(list3[0]);
            int origin_node = Integer.parseInt(list3[1]);
            int destination_node = Integer.parseInt(list3[2]);
            int size = Integer.parseInt(list3[3]);
            int cost_of_not_transporting = Integer.parseInt(list3[4]);
            int lowerbound_for_pickup = Integer.parseInt(list3[5]);
            int upperbound_for_pickup = Integer.parseInt(list3[6]);
            int lowerbound_for_delivery = Integer.parseInt(list3[7]);
            int upperbound_for_delivery = Integer.parseInt(list3[8]);
            Call call = new Call(call_index, origin_node, destination_node, size, cost_of_not_transporting,
                    lowerbound_for_pickup, upperbound_for_pickup, lowerbound_for_delivery, upperbound_for_delivery);
            list_of_calls.add(call);
            if(print)System.out.println(call.toString());
            if(print)System.out.println();
        }


        //reading travel times and costs, creating a list 2d map for each vehicle with travel time and cost in every position
        // where map[origin][destination] returns a string with the number separated by a ','
        if(print)System.out.println(sc.nextLine());
        else sc.nextLine();
        ArrayList<int[][]> list_of_maps_of_travel_time = new ArrayList<>();
        ArrayList<int[][]> list_of_maps_of_travel_cost = new ArrayList<>();
        for (int i = 0; i < number_of_vehicles; i++) {
            int[][] map_for_vehicle_time = new int[number_of_nodes][number_of_nodes];
            int[][] map_for_vehicle_cost = new int[number_of_nodes][number_of_nodes];
            list_of_maps_of_travel_time.add(map_for_vehicle_time);
            list_of_maps_of_travel_cost.add(map_for_vehicle_cost);
        }
        for (int i = 0; i < number_of_nodes * number_of_nodes * number_of_vehicles; i++) {
            String line4 = sc.nextLine();
            String[] list4 = line4.split(",");
            //vehicle, origin node, destination node, travel time (in hours), travel cost (in €)
            int vehicle_index = Integer.parseInt(list4[0]);
            int origin = Integer.parseInt(list4[1]);
            int destination = Integer.parseInt(list4[2]);
            int travel_time = Integer.parseInt(list4[3]);
            int travel_cost = Integer.parseInt(list4[4]);
            if(print)System.out.println(String.format("Vehicle: %d, origin: %d, dest: %d, travel time: %d, travel cost: %d", vehicle_index,
                    origin, destination, travel_time, travel_cost));
            list_of_maps_of_travel_time.get(vehicle_index-1)[origin-1][destination-1] = travel_time;
            list_of_maps_of_travel_cost.get(vehicle_index-1)[origin-1][destination-1] = travel_cost;
        }

        //reading the final inputs...
        if(print)System.out.println(sc.nextLine());
        else sc.nextLine();
        ArrayList<ArrayList<int[]>> node_times_and_costs = new ArrayList<>();
        for (int j = 0; j < number_of_vehicles; j++) {
            ArrayList<int[]> list = new ArrayList<>();
            node_times_and_costs.add(list);
            for (int i = 0; i < number_of_calls; i++) {
                String[] line5 = sc.nextLine().split(",");
                int[] array = new int[6];
                array[0] = Integer.parseInt(line5[0]);
                array[1] = Integer.parseInt(line5[1]);
                array[2] = Integer.parseInt(line5[2]);
                array[3] = Integer.parseInt(line5[3]);
                array[4] = Integer.parseInt(line5[4]);
                array[5] = Integer.parseInt(line5[5]);
                node_times_and_costs.get(j).add(array);
            }
        }
        sc.close();
        return new Assignment2(number_of_calls, number_of_nodes, number_of_vehicles, list_of_vehicles, list_of_calls, list_of_maps_of_travel_time, list_of_maps_of_travel_cost, node_times_and_costs);
    }

}
