package graph.topo;

public class Metrics {
    private int pushes;
    private int pops;
    private double executionTime;

    public Metrics() {
        this.pushes = 0;
        this.pops = 0;
        this.executionTime = 0.0;
    }

    public void incrementPushes() {
        pushes++;
    }

    public void incrementPops() {
        pops++;
    }

    public void setExecutionTime(double time) {
        this.executionTime = time;
    }

    public int getPushes() {
        return pushes;
    }

    public int getPops() {
        return pops;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    @Override
    public String toString() {
        return String.format("Queue Pushes: %d, Queue Pops: %d, Time: %.3f ms",
                pushes, pops, executionTime);
    }
}