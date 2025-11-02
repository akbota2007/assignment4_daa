package graph.dagsp;


public class Metrics {
    private int relaxations;
    private double executionTime;

    public Metrics() {
        this.relaxations = 0;
        this.executionTime = 0.0;
    }

    public void incrementRelaxations() {
        relaxations++;
    }

    public void setExecutionTime(double time) {
        this.executionTime = time;
    }

    public int getRelaxations() {
        return relaxations;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    @Override
    public String toString() {
        return String.format("Edge Relaxations: %d, Time: %.3f ms",
                relaxations, executionTime);
    }
}