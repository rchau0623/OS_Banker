/*
 * Created by rchau on 10/7/2016.
 */
public class Process {
    int A;
    int B;
    int C;
    int M;
    int state;
    int era;
    int finish;
    int io;
    int wait;

    /**
     * Process constructor.
     *
     * @param A arrival.
     * @param B max CPU time per run.
     * @param C total CPU time.
     * @param M I/O multiplier.
     * @param state current state.
     * @param era state tick time.
     * @param finish data collection for finishing time.
     * @param io data collection for io utilization.
     * @param wait data collect for wait utilization.
     */
    public Process (int A, int B, int C, int M, int state, int era, int finish, int io, int wait) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.M = M;
        this.state = state;
        this.era = era;
        this.finish = finish;
        this.io = io;
        this.wait = wait;
    }

    /**
     * Gets the I/O time for a running process.
     *
     * @param dynasty moded random integer.
     * @return I/O time.
     */
    public int getIO(int dynasty) {
        return dynasty * this.M;
    }

    /**
     * Gets the CPU time for each process.
     *
     * @param X B.
     * @return CPU time.
     */
    public int randomOS(int X) {
        return 1 + (X % this.B);
    }
}
