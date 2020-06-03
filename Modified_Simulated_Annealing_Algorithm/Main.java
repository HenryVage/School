import java.io.IOException;
import java.util.ArrayList;

/**
 * Main class to run and sort/edit output and inputs to the algorithms
 */
public class Main {

    /**
     * main method to run
     *
     * @param args not used
     * @throws IOException if something went wrong with reading files
     */
    public static void main(String[] args) throws IOException {

        Assignment2 call_7_vehicle_3;
        call_7_vehicle_3 = ReadFromFile.read_from_file("C:\\Users\\hfv\\Desktop\\Call_7_Vehicle_3.txt", false);
        test(call_7_vehicle_3);
        System.out.println("Call_7_Vehicle_3");
        int[] solution1 = {0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7};
        assignment_3(call_7_vehicle_3, solution1, 1476444);
        System.out.println();

        Assignment2 call_18_vehicle_5 =ReadFromFile.read_from_file("C:\\Users\\hfv\\Desktop\\Call_18_Vehicle_5.txt", false);
        System.out.println("Call_18_Vehicle_5");
        int[] solution2 = {0,0,0,0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13,14,14,15,15,16,16,17,17,18,18};
        assignment_3(call_18_vehicle_5, solution2, 2400016);
        System.out.println();

        Assignment2 call_035_vehicle_07 =ReadFromFile.read_from_file("C:\\Users\\hfv\\Desktop\\Call_035_Vehicle_07.txt", false);
        System.out.println("Call_035_Vehicle_07");
        int[] solution3 = initial_solution(35, 7);
        assignment_3(call_035_vehicle_07, solution3, 4580935);
        System.out.println();

        Assignment2 call_080_vehicle_20 =ReadFromFile.read_from_file("C:\\Users\\hfv\\Desktop\\Call_080_Vehicle_20.txt", false);
        System.out.println("Call_080_Vehicle_20:");
        int[] solution4 = initial_solution(80, 20);
        assignment_3(call_080_vehicle_20, solution4, 11543162  );
        System.out.println();

        Assignment2 call_130_vehicle_40 = ReadFromFile.read_from_file("C:\\Users\\hfv\\Desktop\\Call_130_Vehicle_40.txt", false);
        System.out.println("Call_130_Vehicle_40:");
        int[] solution5 = initial_solution(130, 40);
        assignment_3(call_130_vehicle_40, solution5, 17270951);
        System.out.println();

    }

    /**
     * Method for doing exam
     * i.e running one simulated annealing on one instance once and printing results
     * @param assignment2
     * @param solution
     * @param score_to_beat
     */
    public static void exam(Assignment2 assignment2, int[] solution, int score_to_beat) {
        int obj_of_initial_solution = assignment2.calc_obj_func(null, solution, false)[0];
        long startTime_sim = System.nanoTime();
        int[] sim_solution = assignment2.sim_annealing(solution, 10000, 33, 33, 50000000, 0.99868);
        long endTime_sim = System.nanoTime();
        long duration_sim = (endTime_sim - startTime_sim) / 1000000;
        double sim_annealing_time = duration_sim;
        int simulated_annealing_score = assignment2.calc_obj_func(null, sim_solution, false)[0];

        if (!assignment2.check_feasibility(null, sim_solution, false) || simulated_annealing_score < score_to_beat) {
            System.out.println("sim anealing score = " + simulated_annealing_score);
            System.out.println(assignment2.check_feasibility(null, sim_solution, false));
            for (int j = 0; j < sim_solution.length; j++) {
                System.out.print(sim_solution[j] + ", ");
            }
            System.out.println();
        }
        System.out.println("sim anealing score = " + simulated_annealing_score);
        System.out.printf("sim anealing running time = %3.3f \n", (sim_annealing_time) / 1000);
        double sim_improv_score = obj_of_initial_solution - simulated_annealing_score;
        sim_improv_score = sim_improv_score / obj_of_initial_solution;
        sim_improv_score = sim_improv_score * 100;
        System.out.printf("sim anealing improvement score = %3.1f\n", sim_improv_score);
        System.out.println("simulated annealing solution: ");
        for (int j = 0; j < sim_solution.length; j++) {
            System.out.print(sim_solution[j] + ", ");
        }
        System.out.println();
    }

    /**
     * method to run one instance 10 times each and calculate and print results
     *
     * @param assignment2   the instance to run
     * @param solution      the initial solution eg 0,0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7
     * @param score_to_beat prints the solution and score if this score is beaten
     */
    public static void assignment_3(Assignment2 assignment2, int[] solution, int score_to_beat) {
        int[] sim_annealing_scores = new int[10];
        long[] sim_annealing_time = new long[10];
        int[][] sim_annealing_solutions = new int[10][];
        int obj_of_initial_solution = assignment2.calc_obj_func(null, solution, false)[0];
        System.out.println("score of initial solution: " + obj_of_initial_solution);
        int n = 10;
        for (int i = 0; i < n; i++) {
            long startTime_sim = System.nanoTime();
            int[] sim_solution = assignment2.sim_annealing(solution, 10000, 33, 33, 50000000, 0.99868);
            long endTime_sim = System.nanoTime();
            sim_annealing_solutions[i] = sim_solution;
            long duration_sim = (endTime_sim - startTime_sim) / 1000000;
            sim_annealing_time[i] = duration_sim;
            sim_annealing_scores[i] = assignment2.calc_obj_func(null, sim_solution, false)[0];
            if (!assignment2.check_feasibility(null, sim_solution, false) || sim_annealing_scores[i] < score_to_beat) {
                System.out.println("sim anealing score = " + sim_annealing_scores[i]);
                System.out.println(assignment2.check_feasibility(null, sim_solution, false));
                for (int j = 0; j < sim_solution.length; j++) {
                    System.out.print(sim_solution[j] + ", ");
                }
                System.out.println();
            }
        }
        System.out.println();
        int sim_sum = 0;
        double sim_time_sum = 0;
        int sim_low = sim_annealing_scores[0];
        int sim_low_index = 0;
        for (int i = 0; i < n; i++) {
            sim_sum += sim_annealing_scores[i];
            sim_time_sum += sim_annealing_time[i];
            if (sim_low > sim_annealing_scores[i]) {
                sim_low = sim_annealing_scores[i];
                sim_low_index = i;
            }
        }
        System.out.println("sim anealing mean score = " + sim_sum / n);
        System.out.println(sim_sum / n);
        System.out.printf("sim anealing mean time = %3.3f \n", (sim_time_sum / n) / 1000);
        System.out.printf("%3.3f \n", (sim_time_sum / n) / 1000);
        double sim_improv_score = obj_of_initial_solution - sim_low;
        sim_improv_score = sim_improv_score / obj_of_initial_solution;
        sim_improv_score = sim_improv_score * 100;
        System.out.printf("sim anealing improvement score = %3.1f\n", sim_improv_score);
        System.out.println("sim anealing lowest score = " + sim_low);
        System.out.println(sim_low);
        System.out.println("simulated annealing best solution: ");
        for (int j = 0; j < sim_annealing_solutions[sim_low_index].length; j++) {
            System.out.print(sim_annealing_solutions[sim_low_index][j] + ", ");
        }
        System.out.println();
    }

    /**
     * method for making initial solutions where all calls are outsourced
     *
     * @param calls    number of call in this instance
     * @param vehicles number of vehicles in this instance
     * @return int[] the initial solution
     */
    public static int[] initial_solution(int calls, int vehicles) {
        ArrayList<Integer> solution = new ArrayList<>();
        for (int i = 0; i < vehicles; i++) {
            solution.add(0);
        }
        for (int i = 1; i < calls + 1; i++) {
            solution.add(i);
            solution.add(i);
        }
        int[] ret = new int[solution.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = solution.get(i);
        }
        return ret;
    }

    /**
     * method the test feasability of solutions used for error checking
     *
     * @param assignment2 the instance
     */
    public static void test(Assignment2 assignment2) {
        int[] solution_fail1 = {4, 1, 3, 7, 2, 0, 2, 6, 5, 0, 6, 4, 1, 5, 0, 7, 3};
        int[] solution_fail2 = {4, 1, 3, 0, 7, 2, 2, 6, 5, 0, 6, 4, 1, 5, 0, 7, 3};
        int[] solution_fail3 = {4, 1, 3, 7, 2, 0, 2, 6, 5, 6, 4, 1, 5, 7, 3};
        int[] solution_fail4 = {0, 0, 0, 4, 1, 3, 7, 2, 2, 6, 5, 6, 4, 1, 5, 3};
        int[] solution_fail5 = {0, 6, 4, 7, 4, 1, 3, 2, 5, 7, 3, 5, 1, 2, 6, 0, 0};
        int[] solution1 = {0, 3, 3, 0, 1, 1, 0, 5, 6, 2, 7, 7, 6, 4, 2, 4, 5};
        int[] solution2 = {3, 3, 0, 0, 7, 7, 1, 1, 0, 5, 4, 6, 2, 5, 6, 4, 2};
        int[] solution3 = {7, 7, 0, 1, 1, 0, 5, 5, 6, 6, 0, 3, 2, 3, 4, 2, 4};
        int[] solution4 = {0, 7, 7, 3, 3, 0, 5, 5, 0, 1, 4, 1, 2, 6, 2, 6, 4};
        int[] solution5 = {1, 1, 0, 7, 7, 0, 2, 2, 0, 3, 4, 5, 6, 4, 5, 3, 6};
        System.out.println("Check feasability: " + assignment2.check_feasibility(null, solution1, false));
        System.out.println("Check feasability: " + assignment2.check_feasibility(null, solution2, false));
        System.out.println("Check feasability: " + assignment2.check_feasibility(null, solution3, false));
        System.out.println("Check feasability: " + assignment2.check_feasibility(null, solution4, false));
        System.out.println("Check feasability: " + assignment2.check_feasibility(null, solution5, false));
        System.out.println("Check feasability: " + assignment2.check_feasibility(null, solution_fail1, false));
        System.out.println("Check feasability: " + assignment2.check_feasibility(null, solution_fail2, false));
        System.out.println("Check feasability: " + assignment2.check_feasibility(null, solution_fail3, false));
        System.out.println("Check feasability: " + assignment2.check_feasibility(null, solution_fail4, false));
        System.out.println("Check feasability: " + assignment2.check_feasibility(null, solution_fail5, false));
    }
}
