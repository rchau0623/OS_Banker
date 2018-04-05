Ryan Chau - rc3009

Operating Systems - Lab 4

Programming Language: Java

Please place the random file into the src folder.

In order to compile, please cd to the directory containing the Linker and module file and enter: 
	javac *.java

To run the program, please enter arguments in the following format: 
	java Paging 0 0 0 0 0 (lowercase algorithm) 0

where Paging is the name of the main method of the program, and the trailing arguments are the machine size, the page size, the size per process, the "job mix", the references per process, the replacement algorithm (lifo, random, or lru)(case sensitive), and level of debugging respectively. The level of debugging must be 0. It is included due to the normal outputs on the professor's website include it as well, although it is not used in the program. The fourth number, the "jobmix" must be between 1 and 4. Please enter the replacement algorithm in all lowercase letters.