/*
 * Name: Ryan Chau
 * NetID: rc3009
 */

import java.util.Scanner;
import java.io.*;

/**
 * Certain descriptions are taken directly from the Lab1 assignment details, particularly of the function of the
 * program, and especially of the errors. All credit for these descriptions are given to Allan Gottlieb.
 *
 * This program accepts a two-pass linker in Java. Pass one determines the base address for each module and
 * the absolute address for each external symbol, storing the later in the symbol table it produces. The first
 * module has base address zero; the base address for module I+1 is equal to the base address of module I
 * plus the length of module I. The absolute address for a relative address defined in module M is the base
 * address of M plus the relative address of S within M. Pass two uses the base addresses and the symbol
 * table computed in pass one to generate the actual output by relocating relative addresses and resolving
 * external references.
 */
public class Linker {
    public static void main (String[] args) {
        /*
         * The file is parsed through using File and Scanner. All three lines of each module are taken in
         * and are placed in a module object.
         */
        try {
            if (args.length != 2) {
                System.out.println("Please use two arguments, first for input, second for output.");
                return;
            }

            File input = new File(args[0]);
            Scanner read = new Scanner(input);
            PrintWriter writer = new PrintWriter(args[1]);

            if (input.length() == 0) {
                System.out.println("Input file empty.");
                return;
            }

            int lorem = read.nextInt();

            // A module array is created rather than individually setting up each module.
            module[] mod = new module[lorem];
            int counter = 0;
            int defCount = 0;
            int useCount = 0;

            for (int i = 0; i < lorem; i++) {
                int ipsum = read.nextInt();
                String[] def = new String[ipsum * 2];
                for (int j = 0; j < ipsum * 2; j++) {
                    def[j] = read.next();
                }
                // Counter for definitions.
                defCount = defCount + ipsum;

                ipsum = read.nextInt();
                String[] use = new String[ipsum];
                for (int j = 0; j < ipsum; j++) {
                    use[j] = read.next();
                }
                // Counter for uses.
                useCount = useCount + ipsum;

                ipsum = read.nextInt();
                int[] text = new int[ipsum];
                for (int j = 0; j < ipsum; j++) {
                    text[j] = read.nextInt();
                }
                // Counter for memory output.
                counter = counter + ipsum;

                // Instead of adding a size attribute, use mod[i].text.length.
                // Creating the actual module.
                mod[i] = new module(def, use, text);
            }

            String[] symbol = new String[defCount];
            int[] defAddress = new int[defCount];
            int index = 0;

            // Pass 1: takes and parses through all defined symbols in order to construct the Symbol Table.
            for (int x = 0; x < mod.length; x++) {
                for (int y = 0; y < mod[x].def.length; y++) {
                    if (y % 2 == 0) {
                        symbol[index] = mod[x].def[y];
                    } else {
                        defAddress[index] = Integer.parseInt(mod[x].def[y]) + module.getApproximate(mod, x);
                        index++;
                    }
                }
            }
            // End of Pass 1.

            /*
             * Error 1: If a symbol is multiply defined, print an error message and use the value given in
             * the first definition.
             */
            boolean[] duplicate = new boolean[symbol.length];
            for (int x = 0; x < symbol.length; x++) {
                for (int y = x + 1; y < symbol.length; y++) {
                    if (x != y && symbol[x].equals(symbol[y])) {
                        duplicate[x] = true;
                        duplicate[y] = true;
                        defAddress[y] = defAddress[x];
                    } else {
                        duplicate[x] = false;
                    }
                }
            }
            // End of Error 1.

            // Pass 2: Array for printing out the Memory Map.
            String[] output = module.getOutput(mod, counter, symbol, defAddress);
            // End of Pass 2.

            /*
             * Error 1: If a symbol is multiply defined, print an error message and use the value given in the
             * first definition.
             */
            int duplicateCount = 0;
            for (int x = 0; x < symbol.length; x++) {
                if (duplicate[x]) {
                    for (int y = x + 1; y < symbol.length; y++) {
                        if (symbol[x].equals(symbol[y])) {
                            for (int z = y; z < symbol.length - 1; z++) {
                                symbol[z] = symbol[z + 1];
                                defAddress[z] = defAddress[z + 1];
                            }
                            duplicateCount++;
                        }
                    }
                }
            }
            // End of Error 1.

            writer.println("Symbol Table");

            // Printing the Symbol Table.
            for (int x = 0; x < defCount - duplicateCount; x++) {
                if (defAddress[x] - module.getApproximate(mod, module.getSymModule(mod, symbol[x]))
                        <= mod[module.getSymModule(mod, symbol[x])].text.length) {
                    writer.print(symbol[x] + "=" + defAddress[x] + " ");
                    if (duplicate[x]) {
                        // Error 1.
                        writer.print("Error: This variable is multiply defined; first value used.");
                    }
                    writer.println("");
                } else {
                    /*
                     * Error 4: If an address appearing in a definition exceeds the size of the module, print an
                     * error message and treat the address given as 0 (relative).
                     */
                    defAddress[x] = module.getApproximate(mod, module.getSymModule(mod, symbol[x]));
                    writer.println(symbol[x] + "=" + defAddress[x] + " ");
                }
            }

            writer.println("");

            // Printing the Memory Map.
            writer.println("Memory Map");
            for (int y = 0; y < counter; y++) {
                writer.println(y + ": " + output[y]);
            }

            writer.println("");

            index = 0;
            String[] calls = new String[useCount];
            for (int b = 0; b < mod.length; b++) {
                for (int c = 0; c < mod[b].use.length; c++) {
                    calls[index] = mod[b].use[c];
                    index++;
                }
            }

            /*
             * Error 6:  If an external address is too large to reference an entry in the use list, print
             * an error message and treat the address as immediate.
             */
            boolean isExternal = false;
            for (int i = 0; i < mod.length; i++) {
                for (int j = 0; j < mod[i].text.length; j++) {
                    if (4 == module.getType(mod[i].text[j])) {
                        isExternal = true;
                        break;
                    }
                }
            }

            // For the case that no external references are found.
            if (!isExternal) {
                for (int k = 0; k < calls.length; k++) {
                    writer.println("Warning: In module " + module.getCallModule(mod, calls[k])
                            + " " + calls[k] + " is on use list but isn't used.");
                }
            } else {
                int tracker = 0;
                String[] relative = new String[counter];
                for (int i = 0; i < mod.length; i++) {
                    for (int j = 0; j < mod[i].text.length; j++) {
                        int x = module.getType(mod[i].text[j]);
                        if (x == 4) {
                            // Error 5
                            if (module.getReference(mod[i].text[j]) <= mod[i].use.length) {
                                relative[tracker] = mod[i].use[module.getReference(mod[i].text[j])];
                                tracker++;
                            }
                        }
                    }
                }
                boolean appears;
                for (int i = 0; i < calls.length; i++) {
                    appears = false;
                    for (int j = 0; j < relative.length; j++) {
                        if (calls[i].equals(relative[j])) {
                            appears = true;
                            break;
                        }
                    }
                    if (!appears) {
                        writer.println("Warning: In module " + module.getCallModule(mod, calls[i])
                                + " " + calls[i] + " is on use list but isn't used.");
                    }
                }
            }
            // End of Error 6.

            // Error 3: If a symbol is defined but not used, print a warning message and continue.
            boolean isUsed;
            for (int b = 0; b < mod.length; b++) {
                for (int c = 0; c < mod[b].def.length / 2; c++) {
                    isUsed = false;
                    for (int a = 0; a < calls.length; a++) {
                        if (calls[a].equals(mod[b].def[c * 2])) {
                            isUsed = true;
                            break;
                        }
                    }
                    if (!isUsed){
                        writer.println("Warning: " + (mod[b].def[c * 2]) + " was defined in module "
                                + b + " but never used.");
                    }
                }
            }
            // End of Error 3.

            writer.close();
            read.close();
        } catch (FileNotFoundException e){
                System.out.print("File not found.");
        }
    }
}
