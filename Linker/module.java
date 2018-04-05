/*
 * Name: Ryan Chau
 * NetID: rc3009
 */

/**
 * This is the module object, where information is stored.
 */
public class module {
    String[] def;
    String[] use;
    int[] text;

    /**
     * This is the module constructor.
     *
     * @param def is the first line of the module containing the definition list.
     * @param use is the second line of the module containing the use list.
     * @param text is the third line of the module containing the program text.
     */
    public module (String[] def, String[] use, int[] text) {
        this.def = def;
        this.use = use;
        this.text = text;
    }

    /**
     * Returns the specific address type.
     * @param address is the specific instruction from the program text.
     * @return the Type of the instruction i.e. 1, 2, 3, or 4.
     */
    public static int getType(int address) {
        return address%10;
    }

    /**
     * Returns the external reference.
     * @param address is the specific instruction from the program text.
     * @return the address field.
     */
    public static int getReference(int address) {
        return (address/10)%1000;
    }

    /**
     * Returns the relative address.
     * @param mod the module object array.
     * @param index the specific module object.
     * @return the relative address of the specific module.
     */
    public static int getApproximate(module[] mod, int index) {
        int total = 0;
        for (int i = 0; i < index; i++) {
            total = total + mod[i].text.length;
        }
        return total;
    }

    /**
     * Returns the module that a symbol is defined in.
     * @param mod the module object array.
     * @param symbol the symbol of which the module in which it was defined is returned.
     * @return the module in which the symbol is defined.
     */
    public static int getSymModule(module[] mod, String symbol) {
        for (int i = 0; i < mod.length; i++) {
            for (int j = 0; j < mod[i].def.length/2; j++) {
                if (symbol.equals(mod[i].def[j*2])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the module that a symbol was called in.
     * @param mod the module object array.
     * @param call the symbol of which the module in which it was called in returned.
     * @return the module in which the symbol is called.
     */
    public static int getCallModule(module[] mod, String call) {
        for (int i = 0; i < mod.length; i++) {
            for (int j = 0; j < mod[i].use.length; j++) {
                if (call.equals(mod[i].use[j])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Generates the output, it takes in all text and parses it using getType. Consider this Pass 2.
     * @param mod the module object array.
     * @param count the index of the output array.
     * @param symbol contains the symbols that have been defined with corresponding indexes to defAddress.
     * @param defAddress contains the absolute address of the symbols of corresponding indexes.
     * @return the memory map.
     */
    public static String[] getOutput(module[] mod, int count, String[] symbol, int[] defAddress) {
        String[] output = new String[count];
        int index = 0;
        int absolute;
        for (int i = 0; i < mod.length; i++) {
            for (int j = 0; j < mod[i].text.length; j++) {
                int x = getType(mod[i].text[j]);
                if (x == 1) {
                    output[index] = Integer.toString(mod[i].text[j] / 10);
                } else if (x == 2) {
                    /*
                     * Error 7: If an absolute address exceeds the size of the machine, print an error message
                     * and use the value zero.
                     */
                    if (getReference(mod[i].text[j]) > 599) {
                        output[index] = Integer.toString((mod[i].text[j] / 10) - getReference(mod[i].text[j]))
                                + " Error: Absolute address exceeds machine size; zero used.";
                    } else {
                        output[index] = Integer.toString(mod[i].text[j] / 10);
                    }
                } else if (x == 3) {
                    /*
                     * Error 8:  If a relative address exceeds the size of the module, print an error message
                     * and use the value zero (absolute).
                     */
                    if ((getReference(mod[i].text[j]) > mod[i].text.length)) {
                        output[index] = Integer.toString((mod[i].text[j] / 10) - getReference(mod[i].text[j]))
                                + " Error: Relative address exceeds module size; zero used.";
                    } else {
                        output[index] = Integer.toString((mod[i].text[j] / 10) + getApproximate(mod, i));
                    }
                } else {
                    /*
                     * Error 5: If an external address is too large to reference an entry in the use list,
                     * print an error message and treat the address as immediate.
                     */
                    if (getReference(mod[i].text[j]) > mod[i].use.length) {
                        output[index] = Integer.toString(mod[i].text[j] / 10)
                                + " Error: External address exceeds length of use list; treated as immediate.";
                    } else {
                        String relative = mod[i].use[getReference(mod[i].text[j])];
                        for (int k = 0; k < symbol.length; k++) {
                            if (relative.equals(symbol[k])) {
                                absolute = k;
                                /*
                                 *Error 4: If an address appearing in a definition exceeds the size of the module,
                                 * print an error message and treat the address given as 0 (relative).
                                 */
                                if ((defAddress[k] - getApproximate(mod, getSymModule(mod, symbol[k]))
                                        > mod[getSymModule(mod, symbol[k])].text.length)) {
                                    output[index] = Integer.toString((mod[i].text[j] / 10)
                                            - getReference(mod[i].text[j]) + getApproximate(mod, i))
                                            + " Error: Relative address of " + symbol[k]
                                            + " exceeds module size; zero used.";
                                } else {
                                    output[index] = Integer.toString(((mod[i].text[j] / 10)
                                            - getReference(mod[i].text[j])) + defAddress[absolute]);
                                }
                                break;
                            } else {
                                /*
                                 * Error 2: If a symbol is used but not defined, print an error message and use the
                                 * value zero.
                                 */
                                output[index] = Integer.toString(((mod[i].text[j] / 10)
                                        - getReference(mod[i].text[j]))) + " Error: "
                                        + relative + " is not defined; zero used.";
                            }
                        }
                    }
                }
                index++;
            }
        }
        return output;
    }
}
