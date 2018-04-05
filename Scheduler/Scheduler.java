/*
 * Name: Ryan Chau
 * Net ID: rc3009
 */

import java.util.*;
import java.io.*;

public class Scheduler {
    /**
     * Scheduler takes a file input, and with the given processes in the input, schedules the runtime
     * of the processes using four different algorithms: First Come First Serve, Round Robin with a quantum
     * of 2, Uniprogramming, and Shortest Job First, in that order.
     *
     * @param args array containing input file and verbose flag.
     */
    public static void main (String[] args) {

        try {

            // Checking for verbose flag in the first argument, as shown in Lab instructions.
            File input;
            boolean verbose = false;
            if (args[0].equals("--verbose")) {
                input = new File(args[1]);
                verbose = true;
            } else {
                input = new File(args[0]);
            }

            // Getting the file with all random numbers.
            File Random = new File("src\\Random.txt");
            Scanner read = new Scanner(input);
            Scanner rand = new Scanner(Random);

            // Total number of processes in order to set up array of processes.
            int x = read.nextInt();
            int total = 0;
            Process[] processes = getProcesses(x, read, verbose);

            // Getting total CPU time.
            for (int i = 0; i < x; i++) {
                total += processes[i].C;
            }

            int dynasty = 0; // Moded random number.
            int cycle = 0;
            boolean isRunning = false;
            int[] detail = new int[x*2]; // Temporary array for printing.
            int run; // Index of running process

            int cpUtil = 0; // Data collection for CPU Utilization.
            int ioUtil = 0; // Data collection for I/O Utilization.

            // Initializing array to keep track of how long a process has been ready for.
            int[] readyTime = new int[x];
            for (int i = 0; i < x; i++) { readyTime[i] = 0; }

            // A single iteration of the while loop is considered one cycle.
            while (total > 0) {

                // Iterating through every process.
                for (int j = 0; j < x; j++) {

                    detail[j * 2] = processes[j].state; // Tracking state.
                    detail[j * 2 + 1] = processes[j].era; // Tracking remaining time in state.

                    if (processes[j].state == 3) { // Checking and handling blocked processes.
                        if (processes[j].era > 0) {
                            processes[j].era--;
                            processes[j].io++;
                        }
                        if (processes[j].era == 0) {
                            processes[j].state = 1;
                            readyTime[j]++;
                        }
                    } else if (processes[j].state == 2) { // Checking and handling running processes.
                        if (processes[j].era > 0) {
                            processes[j].era--;
                            processes[j].C--;
                            readyTime[j] = 0;
                            total--;
                        }
                        if (processes[j].era == 0) {
                            processes[j].state = 3;
                            processes[j].era = processes[j].getIO(dynasty);
                            isRunning = false;
                        }
                        if (processes[j].C == 0) {
                            processes[j].state = 4;
                            processes[j].era = 0;
                            isRunning = false;
                            processes[j].finish = cycle; // Data collection for finishing time.
                        }
                    } else if (processes[j].state == 0) { // unstarted
                        if (processes[j].A == cycle) {
                            processes[j].state = 1;
                            readyTime[j]++;
                        }
                    }
                    if (j == x - 1) { // Only start checking for which process to run after setting the array up.

                        // Checking for ties.
                        boolean isTie = getTie(processes);

                        for (int k = 0; k < x; k++) {
                            if (processes[k].state == 1) { // Checking and handling ready processes
                                if (!isRunning) { // Checking if there is another process running.
                                    if (!isTie) { run = k; }
                                    else { run = max(readyTime); } // Using the array that has waited longest.
                                    if (processes[run].C > 0) {
                                        processes[run].state = 2;
                                        readyTime[run] = 0;
                                        isRunning = true;
                                        // Storing dynasty for use in finding I/O burst run time.
                                        dynasty = processes[run].randomOS(rand.nextInt());
                                        processes[run].era = dynasty;
                                    } else {
                                        processes[run].state = 4;
                                        processes[run].era = 0;
                                        isRunning = false;
                                    }
                                } if (processes[k].state == 1){
                                    processes[k].wait++; // Data collection for wait time.
                                }
                                readyTime[k]++;
                            }
                        }
                    }
                }

                boolean[] check = getUtil(detail, x);
                if (check[0]) { cpUtil++; }
                if (check[1]) { ioUtil++; }

                getDetailed(detail, cycle, x, verbose); // Printing detailed.

                cycle++; // Incrementing cycle.
            }

            // Formatting & printing.
            if (!verbose) {
                System.out.println("");
            } System.out.println("The scheduling algorithm used was First Come First Served\n");

            output(processes, cycle, x, cpUtil, ioUtil); // Printing summary.

            System.out.println("--------------------------------------------------\n");

            // Resetting the cursor of the scanners.
            read = new Scanner(input);
            rand = new Scanner(Random);

            // Getting and printing processes.
            x = read.nextInt();
            total = 0;
            processes = getProcesses(x, read, verbose);

            // Getting total CPU time.
            for (int i = 0; i < x; i++) {
                total += processes[i].C;
            }

            cycle = 0;
            isRunning = false;
            detail = new int[x*2];
            run = 0;
            int[] runTime = new int[x]; // Array containing the moded random number (dynasty array).

            // Data collection.
            cpUtil = 0;
            ioUtil = 0;

            while (total > 0) {
                for (int j = 0; j < x; j++) {

                    detail[j * 2] = processes[j].state;
                    detail[j * 2 + 1] = processes[j].era;

                    if (processes[j].state == 3) { // Blocked.
                        if (processes[j].era > 0) {
                            processes[j].era--;
                            processes[j].io++;
                        }
                        if (processes[j].era == 0) {
                            processes[j].state = 1;
                        }
                    } else if (processes[j].state == 2) { // Running.
                        if (processes[j].era > 0) {
                            processes[j].era--;
                            processes[j].C--;
                            runTime[j]--;
                            total--;
                        } if (processes[j].era == 0){
                            processes[j].state = 1;
                            isRunning = false;
                            run++; // Moving run to the next process after each quantum.
                            run = run % x;
                        }
                        if (runTime[j] == 0) {
                            processes[j].state = 3;
                            processes[j].era = processes[j].getIO(dynasty);
                            isRunning = false;
                        }
                        if (processes[j].C == 0) {
                            processes[j].state = 4;
                            processes[j].era = 0;
                            isRunning = false;
                            processes[j].finish = cycle;
                        }
                    } else if (processes[j].state == 0) { // Unstarted.
                        if (processes[j].A == cycle) {
                            processes[j].state = 1;
                        }
                    }
                    if (j == x - 1) {
                        for (int k = 0; k < x; k++) {
                            if (processes[k].state == 1) { // Ready.
                                if (!isRunning) {
                                    // Incrementing the index to determine which process runs.
                                    while (processes[run].state != 1) {
                                        run++;
                                        run = run % x;
                                    }
                                    if (processes[run].C > 0) {
                                        processes[run].state = 2;
                                        readyTime[run] = 0;
                                        isRunning = true;
                                        if (processes[run].B <= 2) {
                                            runTime[run] = processes[run].randomOS(rand.nextInt());
                                            processes[run].era = runTime[run];
                                        } else if (processes[run].B > 2 && processes[run].C > 1) {
                                            runTime[run] = processes[run].randomOS(rand.nextInt());
                                            processes[run].era = 2;
                                        } else {
                                            runTime[run] = processes[run].randomOS(rand.nextInt());
                                            processes[run].era = 1;
                                        }
                                    } else {
                                        processes[run].state = 4;
                                        processes[run].era = 0;
                                        isRunning = false;
                                    }
                                } if (processes[k].state == 1){
                                    processes[k].wait++;
                                }
                            }
                        }
                    }
                }

                boolean[] check = getUtil(detail, x);
                if (check[0]) { cpUtil++; }
                if (check[1]) { ioUtil++; }

                getDetailed(detail, cycle, x, verbose);

                cycle++;
            }

            if (!verbose) {
                System.out.println("");
            } System.out.println("The scheduling algorithm used was Round Robin\n");

            output(processes, cycle, x, cpUtil, ioUtil);

            System.out.println("--------------------------------------------------\n");

            read = new Scanner(input);
            rand = new Scanner(Random);

            x = read.nextInt();
            total = 0;
            processes = getProcesses(x, read, verbose);

            for (int i = 0; i < x; i++) {
                total += processes[i].C;
            }

            dynasty = 0;
            cycle = 0;
            detail = new int[x*2];
            run = 0;

            cpUtil = 0;
            ioUtil = 0;

            while (total > 0) {

                for (int j = 0; j < x; j++) {
                    detail[j * 2] = processes[j].state;
                    detail[j * 2 + 1] = processes[j].era;

                    if (processes[j].state == 3) { // Blocked.
                        if (processes[j].era > 0) {
                            processes[j].era--;
                            processes[j].io++;
                        }
                        if (processes[j].era == 0) {
                            processes[j].state = 1;
                        }
                    } else if (processes[j].state == 2) { // Running.
                        if (processes[j].era > 0) {
                            processes[j].era--;
                            processes[j].C--;
                            total--;
                        }
                        if (processes[j].era == 0) {
                            processes[j].state = 3;
                            processes[j].era = processes[j].getIO(dynasty);
                        }
                        if (processes[j].C == 0) {
                            processes[j].state = 4;
                            processes[j].era = 0;
                            processes[j].finish = cycle;
                        }
                    } else if (processes[j].state == 0) { // Unstarted.
                        if (processes[j].A == cycle) {
                            processes[j].state = 1;
                        }
                    }

                    if (j == x - 1) {
                        for (int k = run + 1; k < x ; k++) {
                            if (processes[k].state == 1) {
                                processes[k].wait++;
                            }
                        }
                        // Incrementing the index of processes if the current process is terminated.
                        if (processes[run].C == 0 && run < x - 1) {
                            run++;
                        }
                        if (processes[run].state == 1) { // Ready.
                            if (processes[run].C > 0) {
                                processes[run].state = 2;
                                dynasty = processes[run].randomOS(rand.nextInt());
                                processes[run].era = dynasty;
                            } else {
                                processes[run].state = 4;
                            }
                        }
                    }
                }

                boolean[] check = getUtil(detail, x);
                if (check[0]) { cpUtil++; }
                if (check[1]) { ioUtil++; }

                getDetailed(detail, cycle, x, verbose);

                cycle++;
            }

            if (!verbose) {
                System.out.println("");
            } System.out.println("The scheduling algorithm used was Uniprocessing\n");

            for (int k = 1; k < x; k++) {
                processes[k].wait--;
            }

            output(processes, cycle, x, cpUtil, ioUtil);

            System.out.println("--------------------------------------------------\n");

            read = new Scanner(input);
            rand = new Scanner(Random);

            x = read.nextInt();
            total = 0;
            processes = getProcesses(x, read, verbose);

            for (int i = 0; i < x; i++) {
                total += processes[i].C;
            }

            dynasty = 0;
            cycle = 0;
            isRunning = false;
            detail = new int[x*2];

            cpUtil = 0;
            ioUtil = 0;

            while (total > 0) {

                for (int j = 0; j < x; j++) {
                    detail[j * 2] = processes[j].state;
                    detail[j * 2 + 1] = processes[j].era;

                    if (processes[j].state == 3) { // Blocked.
                        if (processes[j].era > 0) {
                            processes[j].era--;
                            processes[j].io++;
                        }
                        if (processes[j].era == 0) {
                            processes[j].state = 1;
                        }
                    } else if (processes[j].state == 2) { // Running.
                        if (processes[j].era > 0) {
                            processes[j].era--;
                            processes[j].C--;
                            total--;
                        }
                        if (processes[j].era == 0) {
                            processes[j].state = 3;
                            processes[j].era = processes[j].getIO(dynasty);
                            isRunning = false;
                        }
                        if (processes[j].C == 0) {
                            processes[j].state = 4;
                            processes[j].era = 0;
                            isRunning = false;
                            processes[j].finish = cycle;
                        }
                    } else if (processes[j].state == 0) { // Unstarted.
                        if (processes[j].A == cycle) {
                            processes[j].state = 1;
                        }
                    }

                    if (j == x - 1) {

                        boolean isTie = getTie(processes);

                        for (int k = 0; k < x; k++) {
                            if (processes[k].state == 1) { // ready
                                if (!isRunning) {
                                    if (!isTie) {
                                        run = k;
                                    } else {
                                        run = min(processes); // Getting the index of the shortest job.
                                    }
                                    if (processes[run].C > 0) {
                                        processes[run].state = 2;
                                        isRunning = true;
                                        dynasty = processes[run].randomOS(rand.nextInt());
                                        processes[run].era = dynasty;
                                    } else {
                                        processes[run].state = 4;
                                        processes[run].era = 0;
                                        isRunning = false;
                                    }
                                } if (processes[k].state == 1){
                                    processes[k].wait++;
                                }
                            }
                        }
                    }
                }

                boolean[] check = getUtil(detail, x);
                if (check[0]) { cpUtil++; }
                if (check[1]) { ioUtil++; }

                getDetailed(detail, cycle, x, verbose);

                cycle++;
            }

            if (!verbose) {
                System.out.println("");
            } System.out.println("The scheduling algorithm used was Shortest Job First\n");

            output(processes, cycle, x, cpUtil, ioUtil);

        } catch (FileNotFoundException e) {
            System.out.print("File not found.");
        }
    }

    /**
     * Retrieves and stores all processes from the file into a process array. It then sorts them and prints.
     *
     * @param x the number of processes.
     * @param read the scanner for the input file.
     * @param verbose boolean.
     * @return the array of processes.
     */
    private static Process[] getProcesses(int x, Scanner read, boolean verbose) {
        System.out.print("The original input was: " + x + " ");
        Process[] processes = new Process[x];
        for (int i = 0; i < x; i++) {
            int A = read.nextInt();
            int B = read.nextInt();
            int C = read.nextInt();
            int M = read.nextInt();
            processes[i] = new Process(A, B, C, M, 0, 0, 0, 0, 0);
            System.out.print("(" + A  + " " + B + " "  + C + " "  + M + ") ");
        } System.out.println("");
        System.out.print("The (sorted) input is: " + x + " ");

        // Bubble sort.
        Process tmp;
        for (int i = 0; i < x - 1; i++) {
            for (int j = 0; j < x - 1 - i; j++) {
                if (processes[j+1].A < processes[j].A) {
                    tmp = processes[j];
                    processes[j] = processes[j+1];
                    processes[j+1] = tmp;
                }
            }
        }

        for (int i = 0; i < x; i++) {
            System.out.print("(" + processes[i].A  + " " + processes[i].B + " "
                    + processes[i].C + " "  + processes[i].M + ") ");
        } System.out.println("");
        if (verbose) {
            System.out.println("\nThis detailed printout gives the state and remaining burst for each process\n");
        }
        return processes;
    }

    /**
     * Each state has a corresponding number, this function returns the String state for printing purposes.
     *
     * @param state numerical state of the process.
     * @return the label of the state number.
     */
    private static String getState(int state) {
        if (state == 0) return "unstarted";
        else if (state == 1) return "ready";
        else if (state == 2) return "running";
        else if (state == 3) return "blocked";
        else return "terminated";
    }

    /**
     * This prints the detailed output in the event that the verbose flag is true.
     *
     * @param detail the printed output pre cycle.
     * @param cycle the cycle number.
     * @param x the number of processes.
     * @param verbose boolean.
     */
    private static void getDetailed(int[] detail, int cycle, int x, boolean verbose) {
        if (verbose) {
            System.out.printf("Before cycle %4d:", cycle);
            for (int k = 0; k < x; k++) {
                System.out.printf("%12s%3d", getState(detail[k * 2]), detail[k * 2 + 1]);
            }
            System.out.println("");
        }
    }

    /**
     * This process checks for ties.
     *
     * @param processes array.
     * @return boolean.
     */
    private static boolean getTie(Process[] processes) {
        int numReady = 0;
        for (int k = 0; k < processes.length; k++) {
            if (processes[k].state == 1) {
                numReady++;
            }
            if (numReady > 1) { return true; }
        }
        return false;
    }

    /**
     * This process finds the maximum of readyTime for FCFS.
     *
     * @param readyTime The array that holds how long each process has been ready.
     * @return maximum readyTime.
     */
    private static int max(int[] readyTime) {
        int max = readyTime[0];
        int index = 0;
        for (int i = 0; i < readyTime.length; i++) {
            if (readyTime[i] > max) {
                max = readyTime[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * This process returns the minimum value greater than 0 for the total CPU time of each process.
     *
     * @param processes array.
     * @return minimum total CPU time across processes.
     */
    private static int min(Process[] processes) {
        int min = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < processes.length; i++) {
            if (processes[i].state == 1 && processes[i].C < min && processes[i].C > 0) {
                min = processes[i].C;
                index = i;
            }
        } return index;
    }

    /**
     * This process is for data collection for CPU Utilization and I/O Utilization.
     *
     * @param detail array holding state and tick timing.
     * @param x number of processes.
     * @return boolean array for data collection.
     */
    private static boolean[] getUtil(int[] detail, int x) {
        boolean[] check = new boolean[2];
        for (int k = 0; k < x; k++) {
            if (detail[k * 2] == 2) {
                check[0] = true;
            }
            if (detail[k * 2] == 3) {
                check[1] = true;
            }
        } return check;
    }

    /**
     * This prints the output.
     *
     * @param processes array.
     * @param cycle current cycle.
     * @param x number of processes.
     * @param cpUtil total cycles spent running.
     * @param ioUtil total cycles spent blocked.
     */
    private static void output(Process[] processes, int cycle, int x, int cpUtil, int ioUtil) {
        int totalTurn = 0;
        int totalWait = 0;

        for (int i = 0; i < x; i++) {
            System.out.println("Process " + i + ":");
            System.out.println("    (A, B, C, M) = (" + processes[i].A + ", " + processes[i].B + ", "
                    + (processes[i].finish - processes[i].A - processes[i].io - processes[i].wait) + ", "
                    + processes[i].M + ")");
            System.out.println("    Finishing time: " + processes[i].finish);
            System.out.println("    Turnaround time: " + (processes[i].finish - processes[i].A));
            totalTurn += (processes[i].finish - processes[i].A);
            System.out.println("    I/O time: " + processes[i].io);
            System.out.println("    Waiting time: " + processes[i].wait + "\n");
            totalWait += processes[i].wait;
        }

        System.out.println("Summary Data:");
        System.out.println("    Finishing time: " + (cycle - 1));
        System.out.printf("    CPU Utilization: %.6f\n", (float)cpUtil/(cycle-1));
        System.out.printf("    I/O Utilization: %.6f\n", (float)ioUtil/(cycle-1));
        System.out.printf("    Throughput: %.6f processes per hundred cycles\n", (float)100/(cycle-1)*x);
        System.out.printf("    Average turnaround time: %.6f\n", (float)totalTurn/x);
        System.out.printf("    Average waiting time: %.6f\n", (float)totalWait/x);
        System.out.println("");
    }
}


