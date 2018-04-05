/*
 * Ryan Chau - RC3009
 * Lab #4 Demand Paging
 */

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;

/**
 * This is the main. It does everything.
 * Given 7 arguments (machine size, page size, size per process, "job mix", references per process,
 * replacement algorithm, and level of debugging), it computes total faults and average residency for each
 * process, later on producing total faults, and overall average residency. The level of debugging is included
 * as the normal outputs include it as well, although it is not used in the program.
 */
public class Paging {
    public static void main(String[] args) {
        try {
            File random = new File("src\\Random.txt");
            Scanner scanner = new Scanner(random);

            if (args.length != 7) {
                System.out.print("Incorrect number of arguments, please include level of debugging: 0 as the 7th argument.");
                System.exit(0);
            }

            int M = Integer.parseInt(args[0]); // The machine size in words.
            int P = Integer.parseInt(args[1]); // The page size in words.
            int S = Integer.parseInt(args[2]); // The size of a process, i.e., the references are to virtual addresses 0..S-1.
            int J = Integer.parseInt(args[3]); // The "job mix", which determines A, B, and C, as described below.
            int N = Integer.parseInt(args[4]); // The number of references for each process.
            String R = args[5]; // The replacement algorithm, LIFO (NOT FIFO), RANDOM, or LRU.
            int D = Integer.parseInt(args[6]); // The level of debugging output.

            System.out.println("The machine size is " + M + ".");
            System.out.println("The page size is " + P + ".");
            System.out.println("The job size is " + S + ".");
            System.out.println("The job mix number is " + J + ".");
            System.out.println("The number of references per process is " + N + ".");
            System.out.println("The replacement algorithm is " + R + ".");
            System.out.println("The level of debugging output is " + D + ".\n");

            int totalProcesses;
            double[][] probability;

            if (J == 1) {
                // One process with A=1 and B=C=0, the simplest (fully sequential) case.
                totalProcesses = 1;
                probability = new double[][]{{1,0,0,0}};
            } else if (J == 2) {
                // Four processes, each with A=1 and B=C=0.
                totalProcesses = 4;
                probability = new double[][]{{1,0,0,0},{1,0,0,0},{1,0,0,0},{1,0,0,0}};
            } else if (J == 3) {
                // Four processes, each with A=B=C=0 (fully random references).
                totalProcesses = 4;
                probability = new double[][]{{0,0,0,1},{0,0,0,1},{0,0,0,1},{0,0,0,1}};
            } else if (J == 4){
                /* One process with A=.75, B=.25 and C=0; one process with A=.75, B=0, and C=.25;
                 * one process with A=.75, B=.125 and C=.125; and one process with A=.5, B=.125 and C=.125.
                 */
                totalProcesses = 4;
                probability = new double[][]{{0.75,0.25,0,0},{0.75,0,0.25,0},{0.75,0.125,0.125,0},{0.5,0.125,0.125,0.25}};
            } else {
                System.out.print("Invalid \"job mix\" number");
                return;
            }

            int[][] frame = new int[M/P][3]; // Creating frame table rather than page table as advised.
            for (int i = 0; i < frame.length; i++) {
                for (int j = 0; j < 3; j++) {
                    frame[i][j] = -1; // Initializing to -1 to indicate free.
                }
            }

            int[] word = new int[totalProcesses]; // Word references per process.
            // Initial word for each process.
            for (int i = 0; i < totalProcesses; i++) {
                word[i] = (111 * (i + 1)) % S;
            }

            boolean match; // Checking for a match.
            int matchIndex = 0; // Index of match.
            boolean free; // Checking if a frame is free.
            int freeIndex = 0; // Index of freed frame.

            int current = 0; // Current process.
            int[] evictions = new int[totalProcesses]; // Evictions per process.
            int[] residency = new int[totalProcesses]; // Residency per process.
            int[] faults = new int[totalProcesses]; // Faults per process.
            int[] loadTime = new int[M/P]; // Time it took to load page.

            int[] references = new int[totalProcesses]; // Remaining References per process.
            for (int i = 0; i < totalProcesses; i++) {
                references[i] = N;
            }

            // Totals for outputting the overall data.
            int totalFault = 0;
            int totalEvictions = 0;
            int totalResidency = 0;

            for (int i = 1; i < N*totalProcesses+1; i ++) {
                // Resetting per each attempt.
                match = false;
                free = false;

                for (int j = 0; j < frame.length; j++) { // Locating matching frame.
                    if (frame[j][0] == current && frame[j][1] == word[current]/P) {
                        match = true;
                        matchIndex = j;
                        break;
                    }
                }

                if (match) { // Given match found.
                    frame[matchIndex][2] = i;
                } else { // Page fault.
                    faults[current]++; // Incrementing faults per process.
                    for (int k = 0; k < frame.length; k++) { // Locating first available frame.
                        if (frame[k][1] == -1) {
                            free = true;
                            freeIndex = k;
                            break;
                        }
                    }
                    if (free) { // Placement into a free frame.
                        frame[freeIndex][0] = current;
                        frame[freeIndex][1] = word[current] / P;
                        frame[freeIndex][2] = i;
                        loadTime[freeIndex] = i;
                    } else { // Evicting.
                        // Using replacement algorithm to evict a frame.
                        if (R.equals("lru")) { // Least Recently Used.
                            freeIndex = 0;
                            for(int k = 1; k < frame.length; k++) { // Locating Least Recently Used.
                                if (frame[freeIndex][2] > frame[k][2]) {
                                    freeIndex = k;
                                }
                            }
                        } else if (R.equals("random")) { // Random.
                            freeIndex = (scanner.nextInt()+1) % frame.length;
                        } else if (R.equals("lifo")) { // Last In First Out.
                            freeIndex = (frame.length - 1) % frame.length;
                        } else {
                            System.out.println("Invalid replacement algorithm.");
                            return;
                        }

                        evictions[frame[freeIndex][0]]++; // Incrementing number of evicted frames for evicted process.
                        residency[frame[freeIndex][0]] += (i - loadTime[freeIndex]); //  the time (measured in memory references) that the page was evicted minus the time it was loaded.

                        // Replacing evicted frame.
                        frame[freeIndex][0] = current;
                        frame[freeIndex][1] = word[current] / P;
                        frame[freeIndex][2] = i;
                        loadTime[freeIndex] = i;
                    }
                }

                // Processing

                // Formatting the probabilities to match the professor's notes.
                double A = probability[current][0];
                double B = probability[current][1];
                double C = probability[current][2];
                int w = word[current];

                // Gives a quotient y satisfying 0≤y<1.
                double y = scanner.nextInt() / (Integer.MAX_VALUE + 1d);

                if (y < A) { // If y<A, do case 1 (it occurs with probability A).
                    // w+1 mod S with probability A.
                    w = (w + 1) % S;
                } else if (y < A + B) { // Else if y<A+B, do case 2, (it occurs with probability B).
                    // w-5 mod S with probability B.
                    w = (w - 5 + S) % S; // As per Note #1.
                } else if (y < A + B + C) { // Else if y<A+B+C, do case 3 (it occurs with probability C).
                    // w+4 mod S with probability C.
                    w = (w + 4) % S;
                } else { // Else do case 4 (it occurs with probability 1-A-B-C).
                    // A random value in 0..S-1 each with probability (1-A-B-C)/S.
                    w = scanner.nextInt() % S;
                }
                word[current] = w;
                references[current]--;

                if (references[current] == 0) { // Moving on to next process given a completion.
                    current++;
                } else if (i % 3 == 0 && references[current] > (N % 3) - 1) { // Moving on to the next current based on the quantum.
                    current = (current + 1) % totalProcesses;
                }
            }

            // Output.
            for (int i = 0; i < totalProcesses; i++) {
                // Calculating total for output.
                totalFault += faults[i];
                totalEvictions += evictions[i];
                totalResidency += residency[i];
                if (evictions[i] == 0) {
                    System.out.println("Process " + (i + 1) + " had " + faults[i] + " faults.\n     With no evictions, the average residence is undefined.");
                } else {
                    System.out.println("Process " + (i + 1) + " had " + faults[i] + " faults and " + ((double)residency[i] / (double)evictions[i]) + " average residency.");
                }
            }System.out.println("");

            if (totalEvictions == 0) {
                System.out.print("The total number of faults is " + totalFault + ".\n     With no evictions, the overall average residence is undefined.");
            } else {
                System.out.print("The total number of faults is " + totalFault + " and the overall average residency is " + ((double)totalResidency / (double)totalEvictions) + ".");
            }
        } catch (FileNotFoundException e) {
            System.out.print("File not found.");
        }
    }
}
