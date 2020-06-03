import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Comparator;

/**
 * MASTER class for running algorithms
 *
 */
public class Assignment2 {

    public int number_of_calls;
    public int number_of_nodes;
    public int number_of_vehicles;
    private ArrayList<Vehicle> list_of_vehicles;
    public ArrayList<Call> list_of_calls;
    private ArrayList<int[][]> list_of_maps_of_travel_time;
    private ArrayList<int[][]> list_of_maps_of_travel_cost;
    private ArrayList<ArrayList<int[]>> node_times_and_costs;
    private ArrayList<ArrayList<Integer>> comp_vehicles_per_call;
    private Random rand;
    private int[][] sorted_cost_of_not_transporting;
    public int[][] clusters;
    public double[][] average_dist_map;
    public int[][][] map_for_calls;
    public int[][] map_for_vehicles;
    public int[][] call_index_per_node;
    private boolean[] tabu_list;
    private ArrayList<Integer> tabu_index_list;
    private ArrayList<Integer> past_solution;
    private int[] pos_of_zeroes;
    public ArrayList<ArrayList<ArrayList<Integer>>> calls_and_vehicles_in_cluster;

    /**
     * constructor only to be used from ReadFromFile class
     *
     * @param number_of_calls number of calls in this instance
     * @param number_of_nodes number of nodes in this instance
     * @param number_of_vehicles number of vehicle in this instance
     * @param list_of_vehicles Arraylist of all vehicle instances(class)
     * @param list_of_calls Arraylist of all call instances(class)
     * @param list_of_maps_of_travel_time Arraylist of int[][] for each vehicle a 2d chart of travel time from/to every node
     * @param list_of_maps_of_travel_cost Arraylist of int[][] for each vehicle a 2d chart of travel cost from/to every node
     * @param node_times_and_costs Arraylist of Arraylist of int[] of node times and cost for each vehicle
     */
    public Assignment2(int number_of_calls, int number_of_nodes, int number_of_vehicles,
                       ArrayList<Vehicle> list_of_vehicles, ArrayList<Call> list_of_calls, ArrayList<int[][]> list_of_maps_of_travel_time,
                       ArrayList<int[][]> list_of_maps_of_travel_cost, ArrayList<ArrayList<int[]>> node_times_and_costs) {
        this.number_of_calls = number_of_calls;
        this.number_of_nodes = number_of_nodes;
        this.number_of_vehicles = number_of_vehicles;
        this.list_of_vehicles = list_of_vehicles;
        this.list_of_calls = list_of_calls;
        this.list_of_maps_of_travel_time = list_of_maps_of_travel_time;
        this.list_of_maps_of_travel_cost = list_of_maps_of_travel_cost;
        this.node_times_and_costs = node_times_and_costs;
        this.comp_vehicles_per_call = new ArrayList<>();
        this.rand = new Random();
        this.map_for_vehicles = new int[number_of_nodes][];
        this.map_for_calls = new int[number_of_nodes][number_of_nodes][];
        this.call_index_per_node = new int[number_of_nodes][];
        this.calls_and_vehicles_in_cluster = new ArrayList<>();
        this.tabu_list = new boolean[number_of_vehicles];
        this.tabu_index_list = new ArrayList<>();
        average_dist_map = new double[number_of_nodes][number_of_nodes];
        for (int i = 0; i < number_of_nodes; i++) {
            for (int j = 0; j < number_of_vehicles; j++) {
                if(list_of_vehicles.get(j).getHome()==i) {
                    if(map_for_vehicles[i] == null) {
                        int[] a = {j+1};
                        map_for_vehicles[i] = a;
                    }else {
                        int[] a = new int[map_for_vehicles[i].length+1];
                        for (int k = 0; k < map_for_vehicles[i].length; k++) {
                            a[k] = map_for_vehicles[i][k];
                        }
                        a[map_for_vehicles[i].length] = j+1;
                        map_for_vehicles[i] = a;
                    }
                }
            }
            for (int j = 0; j < number_of_nodes; j++) {
                int average_dist = 0;
                for (int k = 0; k < number_of_vehicles; k++) {
                    average_dist += list_of_maps_of_travel_time.get(k)[i][j];
                }
                average_dist = average_dist/number_of_vehicles;
                average_dist_map[i][j] = average_dist;
            }
        }
        int[] cost_of_not_transporting = new int[number_of_calls];
        for (int i = 0; i < number_of_calls; i++) {
            cost_of_not_transporting[i] = list_of_calls.get(i).getCost_of_not_transporting();
            ArrayList<Integer> temp = new ArrayList<>();
            for (Vehicle vehicle :
                    list_of_vehicles) {
                if (vehicle.isCallAvailableForThisVehicle(i+1)) {
                    temp.add(vehicle.getIndex()-1);
                }
            }
            comp_vehicles_per_call.add(temp);
        }
        Arrays.sort(cost_of_not_transporting);
        sorted_cost_of_not_transporting = new int[number_of_calls][2];
        for (int i = 0; i < number_of_calls; i++) {
            for (int j = 0; j < number_of_calls; j++) {
                if (cost_of_not_transporting[i] == list_of_calls.get(j).getCost_of_not_transporting()) {
                    int[] a = {cost_of_not_transporting[i], (j+1)};
                    sorted_cost_of_not_transporting[i] = a;
                }
            }
        }
        kmeans();
    }

    /**
     * method to run kmeans on the instance and calculate calls and vehicles that are in the same cluster
     * and halfway in the same cluster(e.g on node inside)
     * and making a map e.g nodes*nodes chart of call nodes
     * and making a call index per node map
     */
    public void kmeans() {
        int random_seed = rand.nextInt();
        BasicKMeans kmeans = new BasicKMeans(average_dist_map , 12, 100, random_seed );
        kmeans.run();
        this.clusters = kmeans.results.clone();
        for (int a = 0; a < clusters.length; a++) {
            int cluster_index = a;
            ArrayList<Integer> calls_in_same_cluster = new ArrayList<>();
            ArrayList<Integer> calls_halfway_in_the_same_cluster = new ArrayList<>();
            ArrayList<Integer> vehicles_in_same_cluster = new ArrayList<>();
            for (int i = 0; i < clusters[cluster_index].length - 1; i++) {
                for (int j = i; j < clusters[cluster_index].length; j++) {
                    if (map_for_calls[clusters[cluster_index][i]][clusters[cluster_index][j]] != null) {
                        for (int k = 0; k < map_for_calls[clusters[cluster_index][i]][clusters[cluster_index][j]].length; k++) {
                            calls_in_same_cluster.add(map_for_calls[clusters[cluster_index][i]][clusters[cluster_index][j]][k] - 1);
                        }
                    } else {
                        if (j == 0) {
                            if (call_index_per_node[i] != null) {
                                if (call_index_per_node[i].length > 0) {
                                    for (int k = 0; k < call_index_per_node[i].length; k++) {
                                        calls_halfway_in_the_same_cluster.add(call_index_per_node[i][k]);
                                    }
                                }
                            }
                        }
                    }
                }
                if (map_for_vehicles[clusters[cluster_index][i]] != null) {
                    for (int j = 0; j < map_for_vehicles[clusters[cluster_index][i]].length; j++) {
                        vehicles_in_same_cluster.add(map_for_vehicles[clusters[cluster_index][i]][j]);
                    }
                }
            }
            ArrayList<ArrayList<Integer>> temp = new ArrayList<>();
            temp.add(calls_in_same_cluster);
            temp.add(calls_halfway_in_the_same_cluster);
            temp.add(vehicles_in_same_cluster);
            calls_and_vehicles_in_cluster.add(a, temp);
        }

        for (int i = 0; i < number_of_calls; i++) {
            if(map_for_calls[list_of_calls.get(i).getOrigin_node()-1][list_of_calls.get(i).getDestination_node()-1] == null) {
                int[] a = {i+1};
                map_for_calls[list_of_calls.get(i).getOrigin_node()-1][list_of_calls.get(i).getDestination_node()-1] = a;
            }else {
                int length = map_for_calls[list_of_calls.get(i).getOrigin_node() - 1][list_of_calls.get(i).getDestination_node() - 1].length;
                int[] a = new int[length+1];
                for (int j = 0; j < length; j++) {
                    a[j] = map_for_calls[list_of_calls.get(i).getOrigin_node()-1][list_of_calls.get(i).getDestination_node()-1][j];
                }
                a[length] = i+1;
                map_for_calls[list_of_calls.get(i).getOrigin_node()-1][list_of_calls.get(i).getDestination_node()-1] = a;
            }

            if(call_index_per_node[list_of_calls.get(i).getOrigin_node()-1] != null) {
                int length = call_index_per_node[list_of_calls.get(i).getOrigin_node() - 1].length;
                int[] a = new int[length+1];
                for (int j = 0; j < length; j++) {
                    a[j] = call_index_per_node[list_of_calls.get(i).getOrigin_node()-1][j];
                }
                a[length] = i+1;
                call_index_per_node[list_of_calls.get(i).getOrigin_node() - 1] = a;
            }else {
                int[] a = {i+1};
                call_index_per_node[list_of_calls.get(i).getOrigin_node() - 1] = a;
            }
            if(call_index_per_node[list_of_calls.get(i).getDestination_node()-1] != null) {
                int length = call_index_per_node[list_of_calls.get(i).getDestination_node() - 1].length;
                int[] a = new int[length+1];
                for (int j = 0; j < length; j++) {
                    a[j] = call_index_per_node[list_of_calls.get(i).getDestination_node()-1][j];
                }
                a[length] = i+1;
                call_index_per_node[list_of_calls.get(i).getDestination_node() - 1] = a;
            }else {
                int[] a = {i+1};
                call_index_per_node[list_of_calls.get(i).getDestination_node() - 1] = a;
            }
        }
    }


    /**
     * Simulated annealing algorithm changed a bit from the implementation from assignment 3
     *
     * @param initial_solution initial solution
     * @param n number of iterations
     * @param p1 possibility of selecting heuristic 1
     * @param p2 possibility of selecting heuristic 2the possibility of selecting heuristic 3 is the remainder left
     * @param init_temp initial temperature
     * @param alpha alpha
     * @return the best found solution
     */
    public int[] sim_annealing(int[] initial_solution, int n, int p1, int p2, double init_temp, double alpha) {
        long startTime_rand = System.nanoTime();
        int[] incumbent = initial_solution;
        int[] best_solution = initial_solution;
        int[] new_solution;
        double temp = init_temp;
        int sum = 0;
        int number_of_heuristics = 6;
        int count = 0;
        int delta_low = 10000000;
        int delta_high = -1000000;
        double[] heuristics_score = new double[number_of_heuristics];
        int[] heuristics_used = new int[number_of_heuristics];
        double[] heuristic_weigths = {0.166, 0.166, 0.166, 0.166, 0.166, 0.166};
        double r = 0.8;
        int segment = 300;
        for (int i = 0; i < n; i++) {
            double random_number_2 = rand.nextInt(10000);
            if(random_number_2!=0) random_number_2 = random_number_2/10000;
//            new_solution = getSolution(p1, p2, incumbent.clone(), random_number);
            int cargoes_bound;
            if(number_of_calls == 7) {
                cargoes_bound = 7 - i/(n/7);
                //10 seconds
                long endTime_rand = System.nanoTime();
                long duration_rand = (endTime_rand - startTime_rand) / 1000000;
                if(duration_rand/1000 >= 9) {
                    break;
                }
            }else if (number_of_calls == 18) {
                cargoes_bound = 7 - i/(n/7);
                //20 seconds
                long endTime_rand = System.nanoTime();
                long duration_rand = (endTime_rand - startTime_rand) / 1000000;
                if(duration_rand/1000 >= 19) {
                    break;
                }
            }else if(number_of_calls == 35) {
                cargoes_bound = 7 - i/(n/7) + number_of_calls/3;
                //50 seconds
                long endTime_rand = System.nanoTime();
                long duration_rand = (endTime_rand - startTime_rand) / 1000000;
                if(duration_rand/1000 >= 49) {
                    break;
                }
            }else if(number_of_calls == 80){
                cargoes_bound = 7 - i/(n/7) + number_of_calls/7;
                //120 seconds
                long endTime_rand = System.nanoTime();
                long duration_rand = (endTime_rand - startTime_rand) / 1000000;
                if(duration_rand/1000 >= 119) {
                    break;
                }
            }else if(number_of_calls == 130){
                cargoes_bound = 7 - i/(n/7) + number_of_calls/11;
                //400 seconds
                long endTime_rand = System.nanoTime();
                long duration_rand = (endTime_rand - startTime_rand) / 1000000;
                if(duration_rand/1000 >= 399) {
                    break;
                }
            }else {
                cargoes_bound = 7 - i/(n/7);
            }
            if(cargoes_bound<=1) {
                cargoes_bound = 2;
            }
            int heuristic = 0;
            int[][] res = getSolution2(incumbent.clone(), cargoes_bound, heuristic_weigths);
            new_solution = res[0];
            heuristic = res[1][0];
            heuristics_used[heuristic]++;
            int delta_e = (calc_obj_func(null,new_solution.clone(), false)[0] - calc_obj_func(null,incumbent.clone(), false)[0]);
            double a =  (-delta_e/temp);
            double p = Math.exp(a);
            if(i == segment) {
                segment += 200;
                for (int j = 0; j < heuristic_weigths.length; j++) {
                    heuristic_weigths[j] = heuristic_weigths[j]*(1-r) + r * (heuristics_score[j]/heuristics_used[j]);
                    if(Double.isNaN(heuristic_weigths[j])) {
                        heuristic_weigths[j] = 0.2;
                    }else if(heuristic_weigths[j] <= 0.2) {
                        heuristic_weigths[j] = 0.2;
                    }
                }
                heuristics_score = new double[number_of_heuristics];
                heuristics_used = new int[number_of_heuristics];
            }
            if(i<200) {
                p = 0.8;
            }else if(i==200) {
                int avg = sum/count;
                double t_avg = -avg/Math.log(0.8);
                double n_double = n;
                double t_final = delta_low;
                double t_init = t_avg/Math.pow(0.998, (n_double)/2);
                temp = t_avg;
                alpha = 0.998;
            }
            int no_improvment = 0;
            if(check_feasibility(null, new_solution.clone(), false) && delta_e<0) {
                no_improvment = 0;
                incumbent = new_solution;
                heuristics_score[heuristic] += 10;
                if (calc_obj_func(null,incumbent.clone(), false)[0] < calc_obj_func(null,best_solution.clone(), false)[0]) {
                    heuristics_score[heuristic] += 20;
                    best_solution = incumbent;
                }
            }else if (check_feasibility(null, new_solution.clone(), false) && random_number_2 > p) {
                heuristics_score[heuristic] += 5; //if this is a unseen solution plus 1
                no_improvment++;
                if(i < 200) {
                    count++;
                    sum += delta_e;
                    if(delta_low<delta_e) delta_low = delta_e;
                    if(delta_high>delta_e) delta_high = delta_e;
                }
                incumbent = new_solution;
            }else {
                no_improvment++;
            }
            if(no_improvment>= 100) {
                incumbent = random_solution(number_of_calls, number_of_vehicles, 1);
            }
            temp = alpha*temp;
        }
        return best_solution;
    }

    /**
     * used for the simulated annealing algorithm ive made
     * this method selects the heuristic to be used in this iteration
     * @param incumbent the solution to change
     * @param cargoes_bound how many cargoes to maximum remove
     * @param heuristic_weigths the weights to use when selecting heuristic
     * @return int[][] where result[0] is the new solution and res[1][0] is the heuristic used
     */
    public int[][] getSolution2(int[] incumbent, int cargoes_bound, double[] heuristic_weigths) {
        double sum = 0;
        for (int i = 0; i < heuristic_weigths.length; i++) {
            sum += heuristic_weigths[i];
        }
        double random_number = (double) rand.nextInt(100000)/100000;
        int[] new_solution;
        int how_many_to_remove = rand.nextInt(cargoes_bound)+1;
        int heuristic;
        if(random_number<heuristic_weigths[0]/sum) {
//            System.out.print("cluser removal:   " + how_many_to_remove + " :    ");
            new_solution = remove_calls_for_vehicle_with_short_travel_time(incumbent.clone(), how_many_to_remove);
            heuristic = 0;
        }else if (random_number<heuristic_weigths[1]/sum + heuristic_weigths[0]/sum) {
//            System.out.print("random removal:   " + how_many_to_remove + " :    ");
            new_solution = remove_random_call(incumbent.clone(), how_many_to_remove);
            heuristic = 1;
        }else if (random_number<heuristic_weigths[1]/sum + heuristic_weigths[0]/sum + heuristic_weigths[2]/sum) {
//            System.out.print("vehicl removal:   " + how_many_to_remove + " :    ");
            new_solution = vehicle_operator(incumbent.clone(), how_many_to_remove);
            heuristic = 2;
        } else if(random_number<heuristic_weigths[1]/sum + heuristic_weigths[0]/sum + heuristic_weigths[2]/sum + heuristic_weigths[3]/sum){
            new_solution = remove_cheapest_and_costliest_calls(incumbent.clone(), how_many_to_remove);
            heuristic = 3;
        } else if(random_number<heuristic_weigths[1]/sum + heuristic_weigths[0]/sum + heuristic_weigths[2]/sum + heuristic_weigths[3]/sum+ heuristic_weigths[4]/sum){
            new_solution = remove_calls_based_on_idle_time(incumbent.clone(), how_many_to_remove);
            heuristic = 4;
        }else {
            new_solution = cluster_removal(incumbent.clone(), how_many_to_remove);
            heuristic = 5;
        }
        int[][] result = new int[2][];
        result[0] = new_solution;
        int[] temp = {heuristic};
        result[1] = temp;
        return result;
    }


    /**
     * heuristic that returns a random solution
     * @param number_of_calls number of calls
     * @param number_of_vehicles number of vehicles
     * @param n number+n is the bound
     * @return
     */
    public int[] random_solution(int number_of_calls, int number_of_vehicles, int n) {
        ArrayList<Integer> calls = new ArrayList<>();
        for (int i = 0; i < number_of_vehicles; i++) {
            calls.add(0);
        }
        for (int i = 0; i < number_of_calls; i++) {
            int vehicle = rand.nextInt(number_of_vehicles+n);
            if(vehicle>=number_of_vehicles){
                calls.add(i+1);
                calls.add(i+1);
            }else {
                vehicle = comp_vehicles_per_call.get(i).get(rand.nextInt(comp_vehicles_per_call.get(i).size()));
                int[] range = find_range_of_vehicle_i(calls, vehicle);
                if((range[1]-range[0])<=0) {
                    calls.add(range[0], i+1);
                    calls.add(range[0], i+1);
                }else {
                    int random_number = random_number_between(range[0], range[1]+1);
                    calls.add(random_number, i+1);
                    random_number = random_number_between(range[0], range[1]+2);
                    calls.add(random_number, i+1);
                }
            }
        }
        int[] arr = new int[calls.size()];
        for (int i = 0; i < calls.size(); i++) {
            if (calls.get(i) != null) {
                arr[i] = calls.get(i);
            }
        }
        return arr;
    }

    /**
     * method to get the available calls list for given vehicle
     * @param vehicle vehicle to find calls to
     * @return int[] the list of available calls
     */
    public int[] find_calls_for_exchange(int vehicle) {
        return list_of_vehicles.get(vehicle).getList_of_available_calls();
    }

    public int[] remove_calls_for_vehicle_with_short_travel_time(int[] solution, int how_many_to_remove) {
        ArrayList<int[]> splitted_solution = split_solution_to_calls_per_vehicles(null, solution);
        int counter = 0;
        for (int i = 0; i < splitted_solution.size() - 1; i++) {
            if(splitted_solution.get(i).length==0) {
                counter++;
            }
        }
        if(counter>=splitted_solution.size()-1) {
            return remove_random_call(solution, how_many_to_remove);
        }
        splitted_solution.remove(splitted_solution.size()-1);
        ArrayList<Call> calls_for_this_vehicle;
        ArrayList<ArrayList<Call>> list_of_calls_per_vehicle = new ArrayList<>();
        for (int[] vehicle : splitted_solution) {
            calls_for_this_vehicle = new ArrayList<>();
            for (int i = 0; i < vehicle.length; i++) {
                calls_for_this_vehicle.add(list_of_calls.get(vehicle[i] - 1));
            }
            list_of_calls_per_vehicle.add(calls_for_this_vehicle);
        }
        int lowest_travel_time = 11110;
        int vehicle_index = -1;
        for (int i = 0; i < list_of_calls_per_vehicle.size(); i++) {
            VehicleStatistics temp = check_time_and_capacity(i, list_of_calls_per_vehicle.get(i), false);
            if(temp.travel_time<lowest_travel_time) {
                vehicle_index = temp.vehicle_index;
            }
        }
        int[] calls_for_xchange = find_calls_for_exchange(vehicle_index);
        int n_to_remove = Math.min(how_many_to_remove, calls_for_xchange.length);
        int[] calls_to_insert = new int[n_to_remove];
        for (int i = 0; i < n_to_remove; i++) {
            calls_to_insert[i] = calls_for_xchange[i];
        }
        ArrayList<Integer> calls = new ArrayList<>();
        for (int i = 0; i < solution.length; i++) {
            calls.add(solution[i]);
        }
        ArrayList<Integer> list;
        list = select_insertion_heuristic(calls, calls_to_insert);
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    /**
     * removal heuristic to remove calls that make vehicles have long idle/waiting time before picking up a packet
     * after removing the call it inputs the calls by calling select_insertion_heuristic
     * @param solution initial solution
     * @param how_many_to_remove how many calls to remove
     * @return the new hopefully better solution
     */
    public int[] remove_calls_based_on_idle_time(int[] solution, int how_many_to_remove) {
        ArrayList<int[]> splitted_solution = split_solution_to_calls_per_vehicles(null, solution);
        int counter = 0;
        for (int i = 0; i < splitted_solution.size() - 1; i++) {
            if(splitted_solution.get(i).length==0) {
                counter++;
            }
        }
        if(counter>=splitted_solution.size()-1) {
            return remove_random_call(solution, how_many_to_remove);
        }
        int[] calls_not_handled = splitted_solution.get(splitted_solution.size()-1);
        splitted_solution.remove(splitted_solution.size()-1);
        ArrayList<Call> calls_for_this_vehicle;
        ArrayList<ArrayList<Call>> list_of_calls_per_vehicle = new ArrayList<>();
        for (int[] vehicle : splitted_solution) {
            calls_for_this_vehicle = new ArrayList<>();
            for (int i = 0; i < vehicle.length; i++) {
                calls_for_this_vehicle.add(list_of_calls.get(vehicle[i] - 1));
            }
            list_of_calls_per_vehicle.add(calls_for_this_vehicle);
        }
        ArrayList<VehicleStatistics> list_of_stats_per_vehicle = new ArrayList<>();
        for (int i = 0; i < list_of_calls_per_vehicle.size(); i++) {
            VehicleStatistics temp = check_time_and_capacity(i, list_of_calls_per_vehicle.get(i), false);
            list_of_stats_per_vehicle.add(temp);
        }
        list_of_stats_per_vehicle.sort(new IdleTimeSorter());
        ArrayList<Integer> calls_to_remove = new ArrayList<>();
        boolean[] already_chosen = new boolean[number_of_calls+1];
        for (int i = list_of_stats_per_vehicle.size()-1; i > Math.max(list_of_stats_per_vehicle.size()-(how_many_to_remove/3), -1); i--) {
            int largest_idle_time = 0;
            int index = -1;
            if(list_of_stats_per_vehicle.get(i).idle_time.size() == 0) continue;
            for (int j = 0; j < list_of_stats_per_vehicle.get(i).idle_time.size(); j++) {
                if(list_of_stats_per_vehicle.get(i).idle_time.get(j)[0]>largest_idle_time) {
                    largest_idle_time = list_of_stats_per_vehicle.get(i).idle_time.get(j)[0];
                    index = j;
                }
            }
            calls_to_remove.add(list_of_stats_per_vehicle.get(i).idle_time.get(index)[1]);
            already_chosen[list_of_stats_per_vehicle.get(i).idle_time.get(index)[1]] = true;
            int[] calls_for_xchange = find_calls_for_exchange(list_of_stats_per_vehicle.get(i).vehicle_index);
            int count = 0;
            shuffleArray(calls_not_handled);
            shuffleArray(calls_for_xchange);
            for (int j = 0; j < calls_for_xchange.length; j++) {
                for (int k = 0; k < calls_not_handled.length; k++) {
                    if(calls_for_xchange[j] == calls_not_handled[k] && !already_chosen[calls_for_xchange[j]]) {
                        calls_to_remove.add(calls_for_xchange[j]);
                        already_chosen[calls_for_xchange[j]] = true;
                        count += 1;
                        break;
                    }
                }
                if(count==2) break;
            }
            for (int j = 0; j < 2 - count; j++) {
                for (int k = 0; k < calls_for_xchange.length; k++) {
                    if(!already_chosen[calls_for_xchange[k]]) {
                        calls_to_remove.add(calls_for_xchange[k]);
                        already_chosen[calls_for_xchange[k]] = true;
                        break;
                    }
                }
            }
        }
        ArrayList<Integer> calls = new ArrayList<>();
        for (int i = 0; i < solution.length; i++) {
            calls.add(solution[i]);
        }
        for (int i = 0; i < calls_to_remove.size(); i++) {
            if(calls.indexOf(calls_to_remove.get(i))!=-1) {
                calls.remove(calls.indexOf(calls_to_remove.get(i)));
                calls.remove(calls.indexOf(calls_to_remove.get(i)));
            }else {
                calls_to_remove.remove(calls_to_remove.get(i));
            }
        }

        int[] calls_to_insert = new int[calls_to_remove.size()];
        for (int i = 0; i < calls_to_remove.size(); i++) {
            calls_to_insert[i] = calls_to_remove.get(i);
        }
        ArrayList<Integer> list;
        list = select_insertion_heuristic(calls, calls_to_insert);
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    /**
     * comparator to sort list of VehicleStatistics
     */
    public class IdleTimeSorter implements Comparator<VehicleStatistics> {

        public int compare(VehicleStatistics a, VehicleStatistics b) {
            return a.total_idle_time-b.total_idle_time;
        }

    }


    /**
     * heuristic to remove n calls and reinsert them
     * @param solution current solution
     * @param how_many_to_remove how many to remove
     * @return int[] the changed solution
     */
    private int[] remove_random_call(int[] solution, int how_many_to_remove) {
        ArrayList<Integer> list = new ArrayList<>();
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 0; i < solution.length; i++) {
            list.add(solution[i]);
            if(i<number_of_calls) {
                temp.add(i+1);
            }
        }
        int[] calls_to_insert = new int[how_many_to_remove];
        for (int i = 0; i < how_many_to_remove; i++) {
            int random_number = rand.nextInt(temp.size());
            calls_to_insert[i] = temp.get(random_number);
            list.remove(list.indexOf(temp.get(random_number)));
            list.remove(list.indexOf(temp.get(random_number)));
            temp.remove(random_number);
        }
        list = select_insertion_heuristic(list, calls_to_insert);
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    /**
     * heursitic to remove and reinsert the cheapes and costliest calls based on the cost of not transporting
     * @param solution initial solution
     * @param how_many_to_remove how many calls to remove and reinsert
     * @return int[] the changed solution
     */
    public int[] remove_cheapest_and_costliest_calls(int[] solution, int how_many_to_remove) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int aSolution : solution) {
            list.add(aSolution);
        }
        int[] calls_to_remove = new int[how_many_to_remove];
        boolean flip = true;
        int number_of_call = number_of_calls-1;
        for (int i = 0; i < how_many_to_remove; i++) {
            if(how_many_to_remove>number_of_calls/2) {
                flip = true;
            }
            if(flip) {
                list.remove(list.indexOf(sorted_cost_of_not_transporting[i][1]));
                list.remove(list.indexOf(sorted_cost_of_not_transporting[i][1]));
                calls_to_remove[i] = (sorted_cost_of_not_transporting[i][1]);
                flip = false;
            }else {
                list.remove(list.indexOf(sorted_cost_of_not_transporting[number_of_call][1]));
                list.remove(list.indexOf(sorted_cost_of_not_transporting[number_of_call][1]));
                calls_to_remove[i] = (sorted_cost_of_not_transporting[number_of_call][1]);
                flip = true;
                number_of_call -= 1;
            }
        }
        list = select_insertion_heuristic(list, calls_to_remove);
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }


    /**
     * heuristic to remove calls that are in a vehicle or that is capable of being in given vehicle
     * uses a tabu list to not choose the same vehicles each time
     * @param solution initial solution
     * @param how_many_to_remove how many calls to remove and reinsert
     * @return int[] changed solution
     */
    private int[] vehicle_operator(int[] solution, int how_many_to_remove) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int aSolution : solution) {
            list.add(aSolution);
        }
        int[] zeroes = find_position_of_zeroes(list);
        ArrayList<ArrayList<Integer>> temp = new ArrayList<>(24);
        for (int i = 0; i < 24; i++) {
            temp.add(new ArrayList<>());
        }
        for (int i = 0; i < number_of_vehicles; i++) {
            int[] range = new int[2];
            if (i == 0) {
                range[1] = zeroes[0];
            }else {
                range[0] = zeroes[i - 1] + 1;
                range[1] = zeroes[i];
            }
            if(temp.get(range[1]-range[0]).size()==0) {
                ArrayList<Integer> a = new ArrayList<>();
                a.add(i);
                temp.add(range[1]-range[0], a);
            }else {
                temp.get(range[1] - range[0]).add(i);
            }
        }
        ArrayList<Integer> vehicles_to_remove = new ArrayList<>();
        for (ArrayList arr :
                temp) {
            for (int i = 0; i < arr.size(); i++) {
                vehicles_to_remove.add((Integer) arr.get(i));
            }
        }
        int[] index = new int[vehicles_to_remove.size()];
        for (int i = 0; i < vehicles_to_remove.size(); i++) {
            index[i] = vehicles_to_remove.get(i);
        }
        int limit = (int) (number_of_vehicles*0.25);
        if(limit<=0) {
            limit = 1;
        }
        int random_vehicle = 0;
        if(tabu_index_list.size()<limit) {
            tabu_index_list.add(index[random_vehicle]);
            tabu_list[index[random_vehicle]] = true;
        }else {
            while (tabu_list[index[random_vehicle]]) random_vehicle = random_vehicle+1;
            tabu_index_list.add(index[random_vehicle]);
            tabu_list[index[random_vehicle]] = true;
            tabu_list[tabu_index_list.get(0)]=false;
            tabu_index_list.remove(0);
        }
        int[] list_of_available_calls = list_of_vehicles.get(index[random_vehicle]).getList_of_available_calls();
        int[] calls_to_insert;
        shuffleArray(list_of_available_calls);
        if(list_of_available_calls.length>how_many_to_remove) {
            calls_to_insert = new int[how_many_to_remove];
            for (int i = 0; i < how_many_to_remove; i++) {
                calls_to_insert[i] = list_of_available_calls[i];
                list.remove(list.indexOf(list_of_available_calls[i]));
                list.remove(list.indexOf(list_of_available_calls[i]));
            }
        }else {
            calls_to_insert = new int[list_of_available_calls.length];
            for (int i = 0; i < list_of_available_calls.length; i++) {
                calls_to_insert[i] = list_of_available_calls[i];
                list.remove(list.indexOf(list_of_available_calls[i]));
                list.remove(list.indexOf(list_of_available_calls[i]));
            }
        }
        list = select_insertion_heuristic(list, calls_to_insert);
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    /**
     * method to shuffle an array
     * @param ar array to shuffle
     */
    private static void shuffleArray(int[] ar) {
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }


    /**
     * heuristic to remove and reinsert calls that are similar and/or in the same cluster
     * also remove some calls already inside a vehicle inside a cluster to make space for perhaps better calls
     * also remove and reinsert calls unhandled if there are few calls/vehicles in given cluster
     * @param sol initial solution
     * @param how_many_to_remove how many calls to remove
     * @return int[] changed solution
     */
    public int[] cluster_removal(int[] sol, int how_many_to_remove) {
        ArrayList<Integer> input = new ArrayList<>();
        for (int i = 0; i < sol.length; i++) {
            input.add(sol[i]);
        }
        int random_cluster_index = rand.nextInt(clusters.length);
        while (clusters[random_cluster_index].length < 2) random_cluster_index = rand.nextInt(clusters.length);
        ArrayList<Integer> calls_in_same_cluster = calls_and_vehicles_in_cluster.get(random_cluster_index).get(0);
        ArrayList<Integer> calls_halfway_in_the_same_cluster = calls_and_vehicles_in_cluster.get(random_cluster_index).get(1);
        ArrayList<Integer> vehicles_in_same_cluster = calls_and_vehicles_in_cluster.get(random_cluster_index).get(2);
        ArrayList<Integer> calls_to_remove = new ArrayList<>();
        if(calls_in_same_cluster.size()!=0) {
            if(calls_in_same_cluster.get(0) != -1) {
                if(vehicles_in_same_cluster.size() != 0) {
                    for (int i = 0; i < vehicles_in_same_cluster.size(); i++) {
                        int[] range = find_range_of_vehicle_i(input, vehicles_in_same_cluster.get(i) - 1);
                        if (range[1] - range[0] == 0) {
                            continue;
                        } else {
                            boolean[] calls = new boolean[number_of_calls + 1];
                            for (int j = range[0]; j < range[1]; j++) {
                                calls_to_remove.add(input.get(j));
                                calls[input.get(j)] = true;
                            }
                            for (int j = range[0]; j < range[1]; j++) {
                                if (calls[input.get(j)]) {
                                    calls_to_remove.remove(calls_to_remove.indexOf(input.get(j)));
                                    calls[input.get(j)] = false;
                                }
                            }
                        }
                    }
                    for (int i = 0; i < calls_in_same_cluster.size(); i++) {
                        boolean already_in = false;
                        for (int j = 0; j < calls_to_remove.size(); j++) {
                            if (calls_in_same_cluster.get(i) == calls_to_remove.get(j)) {
                                already_in = true;
                            }
                        }
                        if(!already_in) {
                            calls_to_remove.add(calls_in_same_cluster.get(i));
                        }
                    }
                }else {
                    ArrayList<Integer> temp = new ArrayList<>(calls_in_same_cluster);
                    calls_to_remove.addAll(temp);
                }
            }
        }else if(calls_halfway_in_the_same_cluster.size()!=0 ) { // second best is perhaps when half a packet and a vehicle?
            if(calls_halfway_in_the_same_cluster.get(0) != -1) {
                if(vehicles_in_same_cluster.size() != 0) {
                    for (int i = 0; i < vehicles_in_same_cluster.size(); i++) {
                        int[] range = find_range_of_vehicle_i(input, vehicles_in_same_cluster.get(i) - 1);
                        if (range[1] - range[0] == 0) {
                            continue;
                        } else {
                            boolean[] calls = new boolean[number_of_calls + 1];
                            for (int j = range[0]; j < range[1]; j++) {
                                calls_to_remove.add(input.get(j));
                                calls[input.get(j)] = true;
                            }
                            for (int j = range[0]; j < range[1]; j++) {
                                if (calls[input.get(j)]) {
                                    calls_to_remove.remove(calls_to_remove.indexOf(input.get(j)));
                                    calls[input.get(j)] = false;
                                }
                            }
                        }
                    }
                    for (int i = 0; i < calls_halfway_in_the_same_cluster.size(); i++) {
                        boolean already_in = false;
                        for (int j = 0; j < calls_to_remove.size(); j++) {
                            if (calls_halfway_in_the_same_cluster.get(i) == calls_to_remove.get(j)) {
                                already_in = true;
                            }
                        }
                        if(!already_in) {
                            calls_to_remove.add(calls_halfway_in_the_same_cluster.get(i));
                        }
                    }
                }else {
                    ArrayList<Integer> temp = new ArrayList<>(calls_halfway_in_the_same_cluster);
                    calls_to_remove.addAll(temp);
                }
            }
        }else {
            if(calls_to_remove.size()<how_many_to_remove) {
                int[] zeroes = find_position_of_zeroes(input);
                boolean[] calls = new boolean[number_of_calls + 1];
                for (int i = zeroes[zeroes.length-1]; i <input.size() ; i++) {
                    calls_to_remove.add(input.get(i));
                    calls[input.get(i)] = true;
                }
                for (int i = zeroes[zeroes.length-1]; i <input.size() ; i++) {
                    if(calls[input.get(i)]) {
                        calls_to_remove.remove(calls_to_remove.indexOf(input.get(i)));
                        calls[input.get(i)] = false;
                    }
                }
            }
        }
        int[] calls_to_insert;
        if(calls_to_remove.size()>how_many_to_remove) {
            calls_to_insert = new int[how_many_to_remove];
            for (int i = 0; i < how_many_to_remove; i++) {
                calls_to_insert[i] = calls_to_remove.get(i);
                input.remove(input.indexOf(calls_to_remove.get(i)));
                input.remove(input.indexOf(calls_to_remove.get(i)));
            }
        }else {
            calls_to_insert = new int[calls_to_remove.size()];
            for (int i = 0; i < calls_to_remove.size(); i++) {
                calls_to_insert[i] = calls_to_remove.get(i);
                input.remove(input.indexOf(calls_to_remove.get(i)));
                input.remove(input.indexOf(calls_to_remove.get(i)));
            }
        }
        ArrayList<Integer> solution;
        solution = select_insertion_heuristic(input, calls_to_insert);
        int[] result = new int[solution.size()];
        for (int i = 0; i < solution.size(); i++) {
            result[i] = solution.get(i);
        }
        return result;
    }

    /**
     * method to select insertion heuristic
     * @param solution solution to reinsert calls
     * @param calls calls to be reinserted
     * @return Arraylist<Integer></Integer> the final solution of one iteration
     */
    public ArrayList<Integer> select_insertion_heuristic(ArrayList<Integer> solution, int[] calls) {
        boolean selector = rand.nextBoolean();
        ArrayList<Integer> result;
        if (selector) {
            result = greedy_insert(solution, calls);
        }else {
            result = regret_k(solution, calls);
        }
        return result;
    }

    /**
     * greedy insert heuristic
     * inserts the calls at its best position
     * @param solution current solution
     * @param calls calls to insert
     * @return Arraylist<Integer></Integer> solution with inserted calls
     */
    public ArrayList<Integer> greedy_insert(ArrayList<Integer> solution, int[] calls) {
        ArrayList<Integer> best_solution = new ArrayList<>(solution);
        ArrayList<Integer> new_solution = new ArrayList<>(solution);
        int cheapest_solution;
        ArrayList<Integer> calls_to_insert = new ArrayList<>();
        for (int i = 0; i < calls.length; i++) {
            calls_to_insert.add(calls[i]);
        }
        for (int c = 0; c < calls.length; c++) {
            int index_of_best_call = -1;
            for (int a = 0; a < calls_to_insert.size(); a++) {
                ArrayList<Integer> sol = new ArrayList<>(new_solution);
                int call = calls_to_insert.get(a);
                ArrayList<Integer> vehicles = comp_vehicles_per_call.get(call - 1);
                ArrayList<Integer> current_solution;
                ArrayList<Integer> current_temp_solution = new ArrayList<>(sol);
                current_temp_solution.add(call);
                current_temp_solution.add(call);
                index_of_best_call = a;
                int score = calc_obj_func(current_temp_solution, null, false)[0];
                best_solution = current_temp_solution;
                cheapest_solution = score;
                for (int i = 0; i < vehicles.size(); i++) {
                    int[] range = find_range_of_vehicle_i(sol, vehicles.get(i));
                    if (((range[1] - range[0]) == 0) ) {
                        current_solution = new ArrayList<>(sol);
                        current_solution.add(range[0], call);
                        current_solution.add(range[0], call);
                        int obj_of_current_solution = calc_obj_func(current_solution, null, false)[0];
                        if (cheapest_solution > obj_of_current_solution && list_of_vehicles.get(vehicles.get(i)).getStarting_time()<list_of_calls.get(call-1).getLowerbound_for_pickup()) {
                            cheapest_solution = obj_of_current_solution;
                            best_solution = current_solution;
                            index_of_best_call = a;
                        }
                    } else {
                        int obj_of_current_solution;
                        int obj_func_of_current_vehicle;
                        int cheapest_vehicle_combo = 10000000;
                        ArrayList<Integer> best_solution_for_this_vehicle = new ArrayList<>();
                        for (int j = range[0]; j < range[1] + 1; j++) {
                            for (int k = j; k < range[1]+2; k++) {
                                current_solution = new ArrayList<>(sol);
                                current_solution.add(j, call);
                                current_solution.add(k, call);
                                List<Integer> calls_for_vehicle = current_solution.subList(range[0], range[1]+2);
                                ArrayList<Integer> b = new ArrayList<>(calls_for_vehicle);
                                obj_func_of_current_vehicle = calc_obj_func(b, null, false)[0];
                                if (obj_func_of_current_vehicle < cheapest_vehicle_combo && check_capasity_of_vehicle(calls_for_vehicle, vehicles.get(i))) {
                                    cheapest_vehicle_combo = obj_func_of_current_vehicle;
                                    best_solution_for_this_vehicle = current_solution;
                                }
                            }
                        }
                        if(best_solution_for_this_vehicle.size() !=0) {
                            obj_of_current_solution = calc_obj_func(best_solution_for_this_vehicle, null, false)[0];
                            if (cheapest_solution > obj_of_current_solution) {
                                cheapest_solution = obj_of_current_solution;
                                best_solution = best_solution_for_this_vehicle;
                                index_of_best_call = a;
                            }
                        }
                    }
                }
            }
            if(1<calls_to_insert.size()) calls_to_insert.remove(index_of_best_call);
            new_solution = new ArrayList<>(best_solution);
        }
        return best_solution;
    }

    /**
     * insertion heuristic
     * very similar to greedy_insert except it chooses the second best position of calls with big difference between the best and second best postion
     * @param solution solution with removed calls
     * @param calls calls to reinsert
     * @return Arraylist<Integer></Integer> solution with inserted calls
     */
    public ArrayList<Integer> regret_k(ArrayList<Integer> solution, int[] calls) {
        ArrayList<Integer> new_solution = new ArrayList<>(solution);
        int cheapest_solution;
        ArrayList<Integer> calls_to_insert = new ArrayList<>();
        for (int i = 0; i < calls.length; i++) {
            calls_to_insert.add(calls[i]);
        }
        for (int c = 0; c < calls.length; c++) {
            ArrayList<ArrayList<ArrayList<Integer>>> sorted_solutions_per_call = new ArrayList<>();
            ArrayList<ArrayList<Integer>> sorted_score_of_solutions_per_call = new ArrayList<>();
            int index_of_best_call = -1;
            ArrayList<Integer> index_of_calls = new ArrayList<>();
            for (int a = 0; a < calls_to_insert.size(); a++) {
                ArrayList<Integer> sol = new ArrayList<>(new_solution);
                int call = calls_to_insert.get(a);
                ArrayList<Integer> vehicles = comp_vehicles_per_call.get(call - 1);
                ArrayList<Integer> current_solution;
                ArrayList<ArrayList<Integer>> solutions_per_call = new ArrayList<>();
                ArrayList<Integer> cost_of_solutions_per_call = new ArrayList<>();
                index_of_calls.add(a);
                sorted_solutions_per_call.add(solutions_per_call);
                sorted_score_of_solutions_per_call.add(cost_of_solutions_per_call);
                ArrayList<Integer> current_temp_solution = new ArrayList<>(sol);
                current_temp_solution.add(call);
                current_temp_solution.add(call);
                int score = calc_obj_func(current_temp_solution, null, false)[0];
                solutions_per_call.add(current_temp_solution);
                cost_of_solutions_per_call.add(score);
                cheapest_solution = score;
                for (int i = 0; i < vehicles.size(); i++) {
                    int[] range = find_range_of_vehicle_i(sol, vehicles.get(i));
                    if (((range[1] - range[0]) == 0) ) {
                        current_solution = new ArrayList<>(sol);
                        current_solution.add(range[0], call);
                        current_solution.add(range[0], call);
                        int obj_of_current_solution = calc_obj_func(current_solution, null, false)[0];
                        if (cheapest_solution > obj_of_current_solution && list_of_vehicles.get(vehicles.get(i)).getStarting_time()<list_of_calls.get(call-1).getLowerbound_for_pickup()) {
                            solutions_per_call.add(current_solution);
                            cost_of_solutions_per_call.add(obj_of_current_solution);
                            cheapest_solution = obj_of_current_solution;
                        }
                    } else {
                        int obj_of_current_solution;
                        int obj_func_of_current_vehicle;
                        int cost_of_solution_except_curr_vehicle = 1000000000;
                        int cheapest_vehicle_combo = 10000000;
                        for (int j = range[0]; j < range[1] + 1; j++) {
                            for (int k = j; k < range[1]+2; k++) {
                                current_solution = new ArrayList<>(sol);
                                current_solution.add(j, call);
                                current_solution.add(k, call);
                                List<Integer> calls_for_vehicle = current_solution.subList(range[0], range[1]+2);
                                ArrayList<Integer> b = new ArrayList<>(calls_for_vehicle);
                                if (j == range[0] && k == j) {
                                    obj_of_current_solution = calc_obj_func(current_solution, null, false)[0];
                                    obj_func_of_current_vehicle = calc_obj_func(b, null, false)[0];
                                    cost_of_solution_except_curr_vehicle = obj_of_current_solution - obj_func_of_current_vehicle;
                                }
                                obj_func_of_current_vehicle = calc_obj_func(b, null, false)[0];
                                if (obj_func_of_current_vehicle < cheapest_vehicle_combo && check_capasity_of_vehicle(calls_for_vehicle, vehicles.get(i))) {
                                    cheapest_vehicle_combo = obj_func_of_current_vehicle;
                                    obj_of_current_solution = cost_of_solution_except_curr_vehicle + obj_func_of_current_vehicle;
                                    if (cheapest_solution > obj_of_current_solution) {
                                        solutions_per_call.add(current_solution);
                                        cost_of_solutions_per_call.add(obj_of_current_solution);
                                        cheapest_solution = obj_of_current_solution;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            int regret_k_value;
            int best_regret_value = 1;
            ArrayList<Integer> temp = new ArrayList<>();
            for (int i = 0; i < sorted_score_of_solutions_per_call.size(); i++) {
                if(sorted_score_of_solutions_per_call.get(i).size()>= 3) {
                    regret_k_value = sorted_score_of_solutions_per_call.get(i).get(sorted_score_of_solutions_per_call.get(i).size() - 3)
                            - sorted_score_of_solutions_per_call.get(i).get(sorted_score_of_solutions_per_call.get(i).size() - 1);
                }else {
                    regret_k_value = sorted_score_of_solutions_per_call.get(i).get(0);
                }
                if(regret_k_value>best_regret_value) {
                    best_regret_value = regret_k_value;
                    temp = sorted_solutions_per_call.get(i).get(sorted_solutions_per_call.get(i).size()-1);
                    index_of_best_call = index_of_calls.get(i);
                }
            }
            new_solution = new ArrayList<>(temp);
            if(1<calls_to_insert.size()) calls_to_insert.remove(index_of_best_call);
        }

        return new_solution;
    }

    /**
     * Method to checkk feasablility of one vehicle
     * @param solution solution of this vehicle
     * @param vehicle index of vehicle
     * @return boolean true if this is feasable for this vehicle
     */
    public boolean check_capasity_of_vehicle(List<Integer> solution, int vehicle) {
        ArrayList<Call> call_per_vehicle = new ArrayList<>();
        for (int i = 0; i < solution.size(); i++) {
            call_per_vehicle.add(list_of_calls.get(solution.get(i)-1));
        }
        return check_time_and_capacity(vehicle, call_per_vehicle, false).feasable;
    }


    /**
     * random number betwee low and high
     * @param low inlusive
     * @param high exclusive
     * @return
     */
    private int random_number_between(int low, int high) {
        if(high-low!=0) {
            return rand.nextInt(high - low) + low;
        }else {
            return 0;
        }
    }

    /**
     * method to find index for zeroes/splits in a solution
     * @param solution solution to examine
     * @return int[] where each element is an index of a zero
     */
    private int[] find_position_of_zeroes(ArrayList<Integer> solution) {
        int[] pos = new int[number_of_vehicles];
        int j = 0;
        for (int i = 0; i < solution.size(); i++) {
            if(solution.get(i)==0)pos[j++] = i;
        }
        return pos;
    }


    /**
     * find range i.e the index from and to in the solution
     * @param solution solution to examine
     * @param index index of vehicle
     * @return int[] where range[0] is from and range[1] is to
     */
    public int[] find_range_of_vehicle_i(ArrayList<Integer> solution, int index) {
        int[] zeroes;
        if(past_solution == solution) {
            zeroes = pos_of_zeroes;
        }else {
            zeroes = find_position_of_zeroes(solution);
            pos_of_zeroes = zeroes;
            past_solution = solution;
        }
        int[] range = new int[2];
        if(index==number_of_vehicles) {
            range[0] = 0;
            range[1] = -1;
            return range;
        }
        if(index==0) {
            range[1] = zeroes[0];
        }else {
            range[0] = zeroes[index-1]+1;
            range[1] = zeroes[index];
        }
        return range;
    }

    /**
     * method to calculate the objective function of a solution in either arraylist or int[] format
     * @param solution_1 solution
     * @param solution solution
     * @param debug wether or not to print debug mesages to console
     * @return int[] where int[0] is the cost and int[1] is the index of the most expensive call
     */
    public int[] calc_obj_func(ArrayList<Integer> solution_1, int[] solution, boolean debug) {
        int length = 0;
        boolean arraylist = false;
        if(solution_1!=null) {
            length = solution_1.size();
            arraylist = true;
        }else length = solution.length;
        int current_vehicle = 0;
        int cost = 0;
        int position = list_of_vehicles.get(current_vehicle).getHome();
        int[][] map_cost = list_of_maps_of_travel_cost.get(current_vehicle);
        int travel_cost;
        int index_of_dummy_vehicle = 0;
        boolean[] picked_up = new boolean[number_of_calls];
        int most_expensive_call = 0;
        int index_of_most_exp_call = 1;
        for (int i = 0; i < length; i++) {
            index_of_dummy_vehicle = i;
            if(!arraylist) {
                if (solution[i] == 0) {
                    current_vehicle++;
                    if (current_vehicle == number_of_vehicles) break;
                    position = list_of_vehicles.get(current_vehicle).getHome();
                    map_cost = list_of_maps_of_travel_cost.get(current_vehicle);
                    continue;
                }
            }else {
                if (solution_1.get(i) == 0) {
                    current_vehicle++;
                    if (current_vehicle == number_of_vehicles) break;
                    position = list_of_vehicles.get(current_vehicle).getHome();
                    map_cost = list_of_maps_of_travel_cost.get(current_vehicle);
                    continue;
                }
            }
            Call call;
            if(!arraylist) call = list_of_calls.get(solution[i]-1);
            else call = list_of_calls.get(solution_1.get(i)-1);
            int cost_of_call = cost;
            int origin = call.getOrigin_node();
            int destination = call.getDestination_node();
            if (!picked_up[call.getCall_index()-1]) {
                travel_cost = map_cost[position-1][origin-1];
                if(debug) System.out.println("Pickup Travel cost for vehicle: " + (current_vehicle + 1) + " from: " + position + " To: " +origin + " is: "+travel_cost);
                cost += travel_cost;
                position = origin;
                cost += node_times_and_costs.get(current_vehicle).get(call.getCall_index()-1)[3];
                if(debug) System.out.println("origin cost: " + node_times_and_costs.get(current_vehicle).get(call.getCall_index()-1)[3]);
                picked_up[call.getCall_index()-1] = true;
            }else{
                travel_cost = map_cost[position-1][destination-1];
                if(debug) System.out.println("DroppofTravel cost for vehicle: " + (current_vehicle + 1) + " from: " + position + " To: " +destination + " is: "+travel_cost);
                cost += travel_cost;
                cost += node_times_and_costs.get(current_vehicle).get(call.getCall_index()-1)[5];
                if(debug) System.out.println("destination cost: " + node_times_and_costs.get(current_vehicle).get(call.getCall_index()-1)[5]);
                position = destination;
            }
            cost_of_call = cost - cost_of_call;
            if(cost_of_call>most_expensive_call) {
                if(!arraylist) index_of_most_exp_call = solution[i]-1;
                else index_of_most_exp_call = solution_1.get(i)-1;
                most_expensive_call = cost_of_call;
            }
        }
        int cost_of_not_transporting = 0;
        for (int i = index_of_dummy_vehicle; i < length; i++) {
            if(!arraylist) {
                if (solution[i] == 0) continue;
            }else {
                if(solution_1.get(i)==0) continue;
            }
            Call call;
            if(!arraylist) call = list_of_calls.get(solution[i]-1);
            else call = list_of_calls.get(solution_1.get(i)-1);
            if(debug) System.out.println("Cost of not transporting call: " + call.getCall_index() + " cost : " + call.getCost_of_not_transporting());
            cost_of_not_transporting += call.getCost_of_not_transporting();
        }
        cost_of_not_transporting = cost_of_not_transporting/2;
        cost += cost_of_not_transporting;
        int[] results = new int[2];
        results[0] = cost;
        results[1] = index_of_most_exp_call;
        return results;
    }

    /**
     * method to check feasability of a solution in either arraylist or int[] format
     * @param sol solution
     * @param solution solution
     * @param debug wether or not to print debug messages to console
     * @return boolean true is solution is feasable
     */
    public boolean check_feasibility(ArrayList<Integer> sol, int[] solution, boolean debug) {
        ArrayList<int[]> list_of_calls_per_vehicle = split_solution_to_calls_per_vehicles(sol, solution);
        if(!check_pickups_and_deliveries(list_of_calls_per_vehicle)) {
            return false;
        }
        if(list_of_calls_per_vehicle.size()>=number_of_vehicles+2) {
            list_of_calls_per_vehicle.remove(list_of_calls_per_vehicle.size()-1);
            return false;
        }
        for (int i = 0; i < list_of_calls_per_vehicle.size(); i++) {
            ArrayList<Call> calls_for_this_vehicle = new ArrayList<>();
            if((list_of_calls_per_vehicle.get(i).length%2) != 0 )return false;
            for (int j = 0; j < list_of_calls_per_vehicle.get(i).length; j++) {
                calls_for_this_vehicle.add(list_of_calls.get(list_of_calls_per_vehicle.get(i)[j]-1));
                if(i<=number_of_vehicles-1) {
                    if (!list_of_vehicles.get(i).isCallAvailableForThisVehicle(list_of_calls_per_vehicle.get(i)[j])) {
                        if (debug)
                            System.out.println("vehicle: " + (i + 1) + " unable to handle solution: " + list_of_calls_per_vehicle.get(i)[j]);
                        return false;
                    }
                }
            }
            if(i<=number_of_vehicles-1) {
                if (!check_time_and_capacity(i, calls_for_this_vehicle, debug).feasable) {
                    return false;
                }
            }
        }
        if(debug)System.out.println("\nSolution is feasable!");
        return true;
    }

    /**
     * method to split solution into parts for each vehicle in either arraylist or int[] format
     * @param sol  solution to split
     * @param solution solution to split
     * @return arraylist<int[]></int[]> were each int[] is one vehicle
     */
    private ArrayList<int[]> split_solution_to_calls_per_vehicles(ArrayList<Integer> sol,  int[] solution) {
//        if(sol == null) {
//            ArrayList<Integer> temp = new ArrayList<>();
//            for (int i = 0; i < solution.length; i++) {
//                temp.add(solution[i]);
//            }
//            sol = temp;
//        }
//        int[] range;
//        ArrayList<int[]> result = new ArrayList<>();
//        for (int i = 0; i < number_of_vehicles; i++) {
//            range = find_range_of_vehicle_i(sol, i);
//            int[] solution_for_vehicle_i = new int[range[1]-range[0]];
//            int counter = 0;
//            for (int j = range[0]; j < range[1]; j++) {
//                solution_for_vehicle_i[counter++] = sol.get(j);
//            }
//            result.add(solution_for_vehicle_i);
//        }
//        return result;


        boolean arraylist = false;
        int length;
        if(sol != null) {
            arraylist = true;
            length = sol.size();
        }else {
            length = solution.length;
        }
        int number_of_zeroes = 0;
        String string = "";
        for (int i = 0; i < length; i++) {
            if(!arraylist) {
                if (i == 0 && solution[i] == 0) {
                    string += ("," + Integer.toString(solution[i]) + ",");
                } else {
                    string += (Integer.toString(solution[i]) + ",");
                }
                if (solution[i] == 0) {
                    number_of_zeroes++;
                }
            } else {
                if (i == 0 && sol.get(i) == 0) {
                    string += ("," + Integer.toString(sol.get(i)) + ",");
                } else {
                    string += (Integer.toString(sol.get(i)) + ",");
                }
                if (sol.get(i) == 0) {
                    number_of_zeroes++;
                }
            }
        }
        String[] string_calls_per_vehicle = string.split(",0");
        ArrayList<ArrayList<Integer>> temp = new ArrayList<>();
        ArrayList<int[]> list_of_calls_per_vehicle = new ArrayList<>();
        for (int i = 0; i < string_calls_per_vehicle.length; i++) {
            ArrayList<Integer> ints = new ArrayList<>();
            String[] string_calls_for_a_vehicle = string_calls_per_vehicle[i].split(",");
            for (int j = 0; j < string_calls_for_a_vehicle.length; j++) {
                try {
                    if(!string_calls_for_a_vehicle[j].equals("")) {
                        ints.add(Integer.parseInt(string_calls_for_a_vehicle[j]));
                    }
                }catch (NumberFormatException e) {}
            }
            temp.add(ints);
        }
        for (int i = 0; i < temp.size(); i++) {
            int[] temp2 = new int[temp.get(i).size()];
            for (int j = 0; j < temp.get(i).size(); j++) {
                temp2[j] = temp.get(i).get(j);
            }
            list_of_calls_per_vehicle.add(temp2);
        }
        if(number_of_zeroes!=number_of_vehicles) list_of_calls_per_vehicle.add(new int[0]);
        return list_of_calls_per_vehicle;
    }

    /**
     * method to check time and capasity of solution for a vehicle
     *
     * @param current_vehicle vehicle index
     * @param calls_for_this_vehicle list of calls for this vehicle
     * @param debug wether or not to print debug messages
     * @return int[] results[0] = -1 if not feasible else results[0] is travel time and results[1] is idle time
     */
    private VehicleStatistics check_time_and_capacity(int current_vehicle, ArrayList<Call> calls_for_this_vehicle, boolean debug) {
        Vehicle vehicle = list_of_vehicles.get(current_vehicle);
        int time = vehicle.getStarting_time();
        int position = vehicle.getHome();
        int[][] map_time = list_of_maps_of_travel_time.get(current_vehicle);
        int travel_time = 0;
        int capasity = vehicle.getCapacity();
        int current_capasity = 0;
        boolean[] picked_up = new boolean[number_of_calls];
        ArrayList<int[]> idle_time = new ArrayList<>();
        int total_idle_time = 0;
        int total_travel_time = 0;
        for (int i = 0; i < calls_for_this_vehicle.size(); i++) {
            Call call = calls_for_this_vehicle.get(i);
            int origin = call.getOrigin_node();
            int destination = call.getDestination_node();
            if (!picked_up[call.getCall_index()-1]) {
                travel_time = map_time[position-1][origin-1];
                if(debug) System.out.println("Pickup Travel time for vehicle: " + (current_vehicle + 1) + " from: " + position + " To: " +origin + " is: "+travel_time);
                current_capasity += call.getSize();
                time += travel_time;
                total_travel_time += travel_time;
                if (time > call.getUpperbound_for_pickup()){
                    return new VehicleStatistics(false, -1, -1, -1, null);
                }
                if(capasity < current_capasity) {
                    return new VehicleStatistics(false, -1, -1, -1,  null);
                }
                if (time < call.getLowerbound_for_pickup()) {
                    total_idle_time += call.getLowerbound_for_pickup()-time;
                    int[] temp = { (call.getLowerbound_for_pickup()-time), (call.getCall_index()) };
                    idle_time.add(temp);
                    time = call.getLowerbound_for_pickup();
                    position = origin;
                    time += node_times_and_costs.get(current_vehicle).get(call.getCall_index()-1)[2];
                    picked_up[call.getCall_index()-1] = true;
                }else {
                    position = origin;
                    time += node_times_and_costs.get(current_vehicle).get(call.getCall_index()-1)[2];
                    picked_up[call.getCall_index()-1] = true;
                }
            }else{
                travel_time = map_time[position-1][destination-1];
                if(debug) System.out.println("DroppofTravel time for vehicle: " + (current_vehicle + 1) + " from: " + position + " To: " +destination + " is: "+travel_time);
                current_capasity -= call.getSize();
                time += travel_time;
                total_travel_time += travel_time;
                if (time > call.getUpperbound_for_delivery()) {
                    return new VehicleStatistics(false, -1, -1, -1,  null);
                }
                if (time < call.getLowerbound_for_delivery()) {
                    total_idle_time += call.getLowerbound_for_pickup()-time;
                    int[] temp = { (call.getLowerbound_for_pickup()-time), (call.getCall_index()) };
                    idle_time.add(temp);
                    time = call.getLowerbound_for_delivery();
                    position = destination;
                    time += node_times_and_costs.get(current_vehicle).get(call.getCall_index()-1)[4];
                }else {
                    position = destination;
                    time += node_times_and_costs.get(current_vehicle).get(call.getCall_index()-1)[4];
                }
            }

        }
        VehicleStatistics a = new VehicleStatistics(true, total_travel_time, total_idle_time, current_vehicle, idle_time);
        return a;
    }

    /**
     * method to check that all calls are picked up and delivered
     * @param list_of_calls_per_vehicle list of calls to check
     * @return boolean true if feasable
     */
    public boolean check_pickups_and_deliveries(ArrayList<int[]> list_of_calls_per_vehicle) {
        boolean all_calls_are_handled = true;
        boolean[] call_picked_up_total = new boolean[number_of_calls+1];
        boolean[] call_delivered_total = new boolean[number_of_calls+1];
        for (int[] vehicle:list_of_calls_per_vehicle) {
            boolean[] call_picked_up = new boolean[number_of_calls+1];
            boolean[] call_delivered = new boolean[number_of_calls+1];
            for (int i = 0; i < vehicle.length; i++) {
                if (!call_picked_up[vehicle[i]]) {
                    call_picked_up[vehicle[i]] = true;
                    call_picked_up_total[vehicle[i]] =true;
                }
                else if(!call_delivered_total[vehicle[i]]){
                    call_delivered[vehicle[i]] = true;
                    call_delivered_total[vehicle[i]] = true;
                }else {
                    return false; //duplicates in the outsourced calls
                }
            }
            for (int i = 0; i < vehicle.length; i++) {
                if(call_picked_up[vehicle[i]] && !call_delivered[vehicle[i]]) all_calls_are_handled=false;
            }
        }
        for (int i = 1; i < number_of_calls + 1; i++) {
            if(call_picked_up_total[i] && !call_delivered_total[i]) all_calls_are_handled=false;
            if(!call_picked_up_total[i] && call_delivered_total[i]) all_calls_are_handled=false;
        }
        return all_calls_are_handled;
    }

}
