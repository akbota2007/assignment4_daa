package graph.scc;

public class Metrics {
    private int dfsVisits;
    private int edgesExplored;
    private double executionTime; // in milliseconds

    public Metrics() {
        this.dfsVisits = 0;
        this.edgesExplored = 0;
        this.executionTime = 0.0;
    }

    public void incrementDFSVisits() {
        dfsVisits++;
    }

    public void incrementEdgesExplored() {
        edgesExplored++;
    }

    public void setExecutionTime(double time) {
        this.executionTime = time;
    }

    public int getDfsVisits() {
        return dfsVisits;
    }

    public int getEdgesExplored() {
        return edgesExplored;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    @Override
    public String toString() {
        return String.format("DFS Visits: %d, Edges Explored: %d, Time: %.3f ms",
                dfsVisits, edgesExplored, executionTime);
    }
}