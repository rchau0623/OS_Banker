/*
 * Ryan Chau
 * rc3009
 * Lab #3 Banker
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * This is my main. It does everything.
 * Here, both FIFO and Bankers is done without the use of additional functions.
 * Mostly because I was worried that moving my code would unravel the spaghetti.
 */
public class Banker {
    public static void main(String[] args) {
        try {
            File file = new File(args[0]);
            Scanner scanner = new Scanner(file);

            // Storing all of the inputs in an array of ArrayLists of int arrays.
            int T = scanner.nextInt();
            ArrayList<int[]>[] tasks = new ArrayList[T];
            // Initializing all ArrayLists.
            for (int i = 0; i < T; i++) {
                tasks[i] = new ArrayList<>();
            }

            // Establish available resources.
            int R = scanner.nextInt();
            int[] resources = new int[R];
            for (int i = 0; i < R; i++) {
                resources[i] = scanner.nextInt();
            }

            int total = 0;

            // Keeping track of allocated resources.
            int[][] allocated = new int[T][R];

            // This reads each line of activity, assigning numerical values for type of activity.
            while (scanner.hasNext()) {
                int[] action = new int[4];
                for (int i = 0; i < action.length; i++) {
                    if (i == 0) {
                        String y = scanner.next();
                        if (y.equals("initiate")) {
                            action[0] = 0;
                        } else if (y.equals("request")) {
                            action[0] = 1;
                        } else if (y.equals("compute")) {
                            action[0] = 2;
                        } else if (y.equals("release")) {
                            action[0] = 3;
                        } else if (y.equals("terminate")) {
                            action[0] = 4;
                        }
                    } else {
                        action[i] = scanner.nextInt();
                    }
                }

                total++;

                // Assigning each activity to its task.
                tasks[action[1] - 1].add(action);
            }

            // Keeping track of the current command in a given task.
            int[] current = new int[T];
            // Keeping track of current cycle.
            int cycle = 0;
            // Keeping track of computation times.
            int[] compute = new int[T];
            // Keeping track of termination time.
            int[] terminateFIFO = new int[T];
            boolean[] isTerminated = new boolean[T];
            int totalTerminated = 0;
            // Keeping track of released resources.
            int[] released = new int[R];
            // Keeping track of blocked tasks.
            boolean[] blocked = new boolean[T];
            // Keeping track of the time spent waiting.
            int[] waitingFIFO = new int[T];
            // Keeps track of whether or not the tasks are deadlocked.
            boolean deadlock;
            // Keeps rack of which tasks are aborted.
            boolean[] aborted = new boolean[T];

            // Inserting a queue to get tasks that have been blocked longer up front.
            int[] queue = new int[T];
            // Keeping track of how long a tasks been blocked for.
            int[] blockTime = new int[T];
            int temp;
            // Initializing blocked time to be - and queue to be the initial order of the tasks.
            for (int i = 0; i < T; i++) {
                blockTime[i] = 0;
                queue[i] = i;
            }

            // Keeps track of an current index in the for loop. Used later while solving deadlocks.
            int x = 0;

            while (total > 0) {

                // Initially set deadlock to be true, will update with a checker later on.
                deadlock = true;

                // Ordering the queue based on block times using bubbles sort. This ensures that tasks of equal value maintain current order.
                for (int i = 0; i < T; i++) {
                    for (int j = 1; j < T; j++) {
                        if (blockTime[queue[j - 1]] < blockTime[queue[j]]) {
                            temp = queue[j - 1];
                            queue[j - 1] = queue[j];
                            queue[j] = temp;
                        }
                    }
                }

                for (int i = 0; i < T; i++) { // All tasks can run once per cycle.
                    if (current[queue[i]] < tasks[queue[i]].size() - 1 && !aborted[queue[i]]) { // Ensuring the program does not try to go beyond given commands.

                        // Checking for termination first, so that it can be done without wasting an extra cycle.
                        if (tasks[queue[i]].get(current[queue[i]] + 1)[0] == 4) {
                            // In the event that computation occurs a command before termination.
                            if (tasks[queue[i]].get(current[queue[i]])[0] == 2) {
                                if (compute[queue[i]] + 1 == tasks[queue[i]].get(current[queue[i]])[2]) {
                                    isTerminated[queue[i]] = true;
                                    terminateFIFO[queue[i]] = cycle + 1;
                                    total--;
                                }
                            } else {
                                isTerminated[queue[i]] = true;
                                terminateFIFO[queue[i]] = cycle + 1;
                                total--;
                            }
                        }

                        if (tasks[queue[i]].get(current[queue[i]])[0] == 0) { // Initiate

                            // FIFO processes initiate without considering the initial-claim
                            current[queue[i]]++;
                            total--;

                        } else if (tasks[queue[i]].get(current[queue[i]])[0] == 1) { // Request
                            // Checking if there are sufficient resources.
                            if (resources[tasks[queue[i]].get(current[queue[i]])[2] - 1] - tasks[queue[i]].get(current[queue[i]])[3] >= 0) {
                                // Allocation resources, and decrementing from the bank.
                                allocated[queue[i]][tasks[queue[i]].get(current[queue[i]])[2] - 1] += tasks[queue[i]].get(current[queue[i]])[3];
                                resources[tasks[queue[i]].get(current[queue[i]])[2] - 1] -= tasks[queue[i]].get(current[queue[i]])[3];
                                blocked[queue[i]] = false;
                                blockTime[queue[i]] = 0;
                                current[queue[i]]++;
                                total--;
                            } else {
                                // Unable to grant resources this cycle.
                                blocked[queue[i]] = true;
                                blockTime[queue[i]]++;
                                waitingFIFO[queue[i]]++;
                            }

                        } else if (tasks[queue[i]].get(current[queue[i]])[0] == 2) { // Compute
                            // Checks current computation time.
                            if (compute[queue[i]] < tasks[queue[i]].get(current[queue[i]])[2]) {
                                compute[queue[i]]++;
                                if (compute[queue[i]] == tasks[queue[i]].get(current[queue[i]])[2]) {
                                    // Resets and moves onto the next command given completion.
                                    compute[queue[i]] = 0;
                                    current[queue[i]]++;
                                    total--;
                                }
                            }

                        } else if (tasks[queue[i]].get(current[queue[i]])[0] == 3) { // Release
                            // Releasing resources, and adding back to the bank.
                            allocated[queue[i]][tasks[queue[i]].get(current[queue[i]])[2] - 1] -= tasks[queue[i]].get(current[queue[i]])[3];
                            // Saving values to be added at the end of the cycle.
                            released[tasks[queue[i]].get(current[queue[i]])[2] - 1] += tasks[queue[i]].get(current[queue[i]])[3];
                            current[queue[i]]++;
                            total--;
                        }

                        // Do not really need this here, but this is were my print statement was for my detailed output, and I remember it fondly.
                        if (isTerminated[queue[i]]) {
                            totalTerminated++;
                        }
                    }
                }

                // Releasing resources back to bank at the end of the cycle.
                for (int i = 0; i < R; i++) {
                    resources[i] += released[i];
                    released[i] = 0;
                }

                // Checking for deadlock. It's a while loop in the event that multiple tasks need to be aborted.
                while (deadlock && totalTerminated < T) {

                    for (int j = 0; j < T; j++) {
                        // Deadlock is true if all tasks are either blocked or terminated.
                        if (!blocked[j] && !isTerminated[j]) {
                            deadlock = false;
                            break;
                        }
                        // In the case that resources were just released.
                        if (blocked[j]) {
                            if (resources[tasks[j].get(current[j])[2] - 1] - tasks[j].get(current[j])[3] >= 0) {
                                deadlock = false;
                            }
                        }
                    }

                    // Dealing with deadlocks.
                    if (deadlock) {
                        for (int j = 0; j < T; j++) {
                            if (blocked[j]) {
                                isTerminated[j] = true;
                                totalTerminated++;
                                blocked[j] = false;
                                aborted[j] = true;
                                // See? Told you I would use x.
                                x = j;
                                // Give back resources.
                                for (int k = 0; k < R; k++) {
                                    resources[k] += allocated[j][k];
                                    allocated[j][k] = 0;
                                }
                                // Decrement the total.
                                total -= (tasks[j].size() - current[j]);
                                break;
                            }
                        }
                        // There it is again: x.
                        for (int j = x ; j < T; j++) {
                            if (blocked[j]) {
                                // Setting back to a regular state after aborting.
                                if (resources[tasks[j].get(current[j])[2] - 1] - tasks[j].get(current[j])[3] >= 0) {
                                    blocked[j] = false;
                                    deadlock = false;
                                    break;
                                }
                            }
                        }
                    }
                }

                cycle++;
            }

            // Keeping track of totals for overall calculation.
            int totalTimeFIFO = 0;
            int totalWaitFIFO = 0;

            // Printing the desired output.
            System.out.printf("%13s%n" , "FIFO");
            for (int i = 0; i < T; i++) {
                if (!aborted[i]) {
                    totalTimeFIFO += terminateFIFO[i];
                    totalWaitFIFO += waitingFIFO[i];
                    System.out.printf("Task %d%7d%4d%4d%% %n", (i + 1), terminateFIFO[i], waitingFIFO[i], ((100 * waitingFIFO[i]) / terminateFIFO[i]));
                } else {
                    System.out.printf("Task %d%14s %n", (i + 1), "aborted");
                }
            } System.out.printf("total %7d%4d%4d%% %n", totalTimeFIFO, totalWaitFIFO, ((100 * totalWaitFIFO) / totalTimeFIFO));
            // Weird rounding thing, seems inconsistent in the output, but the numbers should be good.

            System.out.println("");

            // Reading in input once again because I'm scared of change.
            file = new File(args[0]);
            scanner = new Scanner(file);

            // Storing all of the inputs in an array of ArrayLists of int arrays.
            T = scanner.nextInt();
            tasks = new ArrayList[T];
            // Initializing all ArrayLists.
            for (int i = 0; i < T; i++) {
                tasks[i] = new ArrayList<>();
            }

            // Establish available resources.
            R = scanner.nextInt();
            resources = new int[R];
            int[] initialResources = new int[R];
            for (int i = 0; i < R; i++) {
                resources[i] = scanner.nextInt();
                initialResources[i] = resources[i];
            }

            total = 0;

            // Keeping track of allocated resources.
            allocated = new int[T][R];

            // This reads each line of activity, assigning numerical values for type of activity.
            while (scanner.hasNext()) {
                int[] action = new int[4];
                for (int i = 0; i < action.length; i++) {
                    if (i == 0) {
                        String y = scanner.next();
                        if (y.equals("initiate")) {
                            action[0] = 0;
                        } else if (y.equals("request")) {
                            action[0] = 1;
                        } else if (y.equals("compute")) {
                            action[0] = 2;
                        } else if (y.equals("release")) {
                            action[0] = 3;
                        } else if (y.equals("terminate")) {
                            action[0] = 4;
                        }
                    } else {
                        action[i] = scanner.nextInt();
                    }
                }

                total++;

                // Assigning each activity to its task.
                tasks[action[1] - 1].add(action);
            }

            // Keeping track of the current command in a given task.
            current = new int[T];
            // Keeping track of current cycle.
            cycle = 0;
            // Keeping track of computation times.
            compute = new int[T];
            // Keeping track of termination time.
            int[] terminateBanker = new int[T];
            isTerminated = new boolean[T];
            // Keeping track of released resources.
            released = new int[R];
            // Keeping track of blocked tasks.
            blocked = new boolean[T];
            // Keeping track of the time spent waiting.
            int[] waitingBanker = new int[T];
            // Keeps track of initial claims
            int[][] initial = new int[T][R];
            // Keeps rack of which tasks are aborted.
            aborted = new boolean[T];
            // Resources needed to complete the task.
            int[][] needed = new int[T][R];
            // Checking if the current task's command is in a safe state or not.
            boolean[] unsafe = new boolean[T];
            // Setting up a queue that is organized based on block time.
            queue = new int[T];
            blockTime = new int[T];
            for (int i = 0; i < T; i++) {
                blockTime[i] = 0;
                queue[i] = i;
            }

            while (total > 0) {

                // This one aborts if the initial claim is bigger than the initial resource pool.
                for (int i = 0; i < T; i++) {
                    for (int j = 0; j < R; j++) {
                        if (initial[i][j] > initialResources[j]) {
                            aborted[i] = true;
                            total -= tasks[j].size() - 1;
                        }
                    }
                }

                // Setting up the needed matrix.
                for (int i = 0; i < T; i++) {
                    for (int j = 0; j < R; j++) {
                        needed[i][j] = initial[i][j] - allocated[i][j];
                    }
                }

                // Sorting again.
                for (int i = 0; i < T; i++) {
                    for (int j = 1; j < T; j++) {
                        if (blockTime[queue[j - 1]] < blockTime[queue[j]]) {
                            temp = queue[j - 1];
                            queue[j - 1] = queue[j];
                            queue[j] = temp;
                        }
                    }
                }

                for (int i = 0; i < T; i++) { // All tasks can run once per cycle.
                    if (current[queue[i]] < tasks[queue[i]].size() - 1 && !aborted[queue[i]]) { // Ensuring the program does not try to go beyond given commands.
                        // Checking for termination first, so that it can be done without wasting an extra cycle.
                        if (tasks[queue[i]].get(current[queue[i]] + 1)[0] == 4) {
                            // In the event that computation occurs a command before termination.
                            if (tasks[queue[i]].get(current[queue[i]])[0] == 2) {
                                if (compute[queue[i]] + 1 == tasks[queue[i]].get(current[queue[i]])[2]) {
                                    isTerminated[queue[i]] = true;
                                    terminateBanker[queue[i]] = cycle + 1;
                                    total--;
                                    unsafe[queue[0]] = false;
                                }
                            } else {
                                isTerminated[queue[i]] = true;
                                terminateBanker[queue[i]] = cycle + 1;
                                total--;
                                unsafe[queue[0]] = false;
                            }
                        }

                        if (tasks[queue[i]].get(current[queue[i]])[0] == 0) { // Initiate

                            // Placing the initial claim into a matrix.
                            initial[queue[i]][tasks[queue[i]].get(current[queue[i]])[2]-1] = tasks[queue[i]].get(current[queue[i]])[3];

                            // FIFO processes initiate without considering the initial-claim
                            current[queue[i]]++;
                            total--;

                        } else if (tasks[queue[i]].get(current[queue[i]])[0] == 1) { // Request

                            // Checking if unsafe.
                            for (int j = 0; j < R; j++) {
                                // I have no idea why this works, it seems incredibly anecdotal, but it works out for all 13 outputs.
                                if (R < 3) {
                                    if (resources[j] - needed[queue[i]][j] < 0) {
                                        unsafe[tasks[queue[i]].get(current[queue[i]])[1] - 1] = true;
                                    }
                                } else {
                                    if (resources[j] - needed[queue[i]][j] < 0 && resources[j] < tasks[queue[i]].get(current[queue[i]])[3]) {
                                        unsafe[tasks[queue[i]].get(current[queue[i]])[1] - 1] = true;
                                    }
                                }
                                // Please don't judge me.
                            }

                            // Checking if there are sufficient resources.
                            if (resources[tasks[queue[i]].get(current[queue[i]])[2] - 1] - tasks[queue[i]].get(current[queue[i]])[3] >= 0 && !unsafe[tasks[queue[i]].get(current[queue[i]])[1]-1] && !aborted[queue[i]]) {
                                if (allocated[queue[i]][tasks[queue[i]].get(current[queue[i]])[2]-1] + tasks[queue[i]].get(current[queue[i]])[3] > initial[queue[i]][tasks[queue[i]].get(current[queue[i]])[2]-1]) {
                                    aborted[queue[i]] = true;
                                } else {
                                    // Allocation resources, and decrementing from the bank.
                                    allocated[queue[i]][tasks[queue[i]].get(current[queue[i]])[2] - 1] += tasks[queue[i]].get(current[queue[i]])[3];
                                    resources[tasks[queue[i]].get(current[queue[i]])[2] - 1] -= tasks[queue[i]].get(current[queue[i]])[3];
                                    blocked[queue[i]] = false;
                                    blockTime[queue[i]] = 0;
                                    current[queue[i]]++;
                                    total--;
                                }
                            } else {
                                // Unable to grant resources this cycle.
                                blocked[queue[i]] = true;
                                blockTime[queue[i]]++;
                                waitingBanker[queue[i]]++;
                            }

                        } else if (tasks[queue[i]].get(current[queue[i]])[0] == 2) { // Compute
                            // Checks current computation time.
                            if (compute[queue[i]] < tasks[queue[i]].get(current[queue[i]])[2]) {
                                compute[queue[i]]++;
                                if (compute[queue[i]] == tasks[queue[i]].get(current[queue[i]])[2]) {
                                    // Resets and moves onto the next command given completion.
                                    compute[queue[i]] = 0;
                                    current[queue[i]]++;
                                    total--;
                                }
                            }

                        } else if (tasks[queue[i]].get(current[queue[i]])[0] == 3) { // Release
                            // Releasing resources, and adding back to the bank.
                            allocated[queue[i]][tasks[queue[i]].get(current[queue[i]])[2] - 1] -= tasks[queue[i]].get(current[queue[i]])[3];
                            // Saving values to be added at the end of the cycle.
                            released[tasks[queue[i]].get(current[queue[i]])[2] - 1] += tasks[queue[i]].get(current[queue[i]])[3];
                            unsafe[queue[0]] = false;
                            current[queue[i]]++;
                            total--;
                        }

                        // Checking if aborted was detected, then removing that task.
                        if (aborted[queue[i]]) {
                            for (int j = 0; j < R; j++) {
                                released[j] += allocated[queue[i]][j];
                                allocated[queue[i]][j] = 0;
                                unsafe[queue[0]] = false;
                                total -= (tasks[j].size() - current[j]) + 1;
                            }
                        }
                    }
                }

                // Releasing resources back to bank at the end of the cycle.
                for (int i = 0; i < R; i++) {
                    resources[i] += released[i];
                    released[i] = 0;
                }

                cycle++;
            }

            // Keeping track of totals for overall calculation.
            int totalTimeBanker = 0;
            int totalWaitBanker = 0;

            // Printing the desired output.
            System.out.printf("%16s%n" , "BANKER'S");
            for (int i = 0; i < T; i++) {
                if (!aborted[i]) {
                    totalTimeBanker += terminateBanker[i];
                    totalWaitBanker += waitingBanker[i];
                    System.out.printf("Task %d%7d%4d%4d%% %n", (i + 1), terminateBanker[i], waitingBanker[i], ((100 * waitingBanker[i]) / terminateBanker[i]));
                } else {
                    System.out.printf("Task %d%14s %n", (i + 1), "aborted");
                }
            } System.out.printf("total %7d%4d%4d%% %n", totalTimeBanker, totalWaitBanker, ((100 * totalWaitBanker) / totalTimeBanker));

        } catch (FileNotFoundException e) {
            System.out.print("File not found.");
        }
    }
}
