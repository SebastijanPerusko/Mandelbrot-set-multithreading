import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Worker implements Runnable {
    private Simulation sim;
    private int start, end, chunk;
    CyclicBarrier barrier;
    private boolean testing = false;

    public Worker(int start, int end, int chunk, Simulation sim) {
        this.sim = sim;
        this.start= start;
        this.end = end;
        this.chunk = chunk;
    }

    public Worker(int start, int end, int chunk, Simulation sim, CyclicBarrier barrier) {
        this.sim = sim;
        this.start= start;
        this.end = end;
        this.chunk = chunk;
        this.barrier = barrier;
        testing = true;
    }

    @Override
    public void run() {
        if(testing){
            for (int row = start; row < end; row++) {
                for (int col = 0; col < sim.getWidthS(); col++) {
                    sim.ArrPixel[Math.abs(col%1000)] = sim.mandelbrot(row, col);
                }
            }
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        } else {
            for (int row = start; row < end; row++) {
                for (int col = 0; col < sim.getWidthS(); col++) {
                    sim.ArrPixel[(row * sim.getWidthS()) + col] = sim.mandelbrot(row, col);
                }
            }
        }
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setChunk(int chunk) {
        this.chunk = chunk;
    }
}
