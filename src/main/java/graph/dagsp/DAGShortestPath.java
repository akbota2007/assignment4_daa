package graph.dagsp;

import graph.Graph;
import graph.Graph.Edge;
import java.util.*;


public class DAGShortestPath {
    private Graph graph;
    private int source;
    private int[] dist;
    private int[] parent;
    private graph.dagsp.Metrics metrics;

    public DAGShortestPath(Graph graph, int source, List<Integer> topoOrder) {
        this.graph = graph;
        this.source = source;
        this.dist = new int[graph.getN()];
        this.parent = new int[graph.getN()];
        this.metrics = new graph.dagsp.Metrics();

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);

        long startTime = System.nanoTime();
        computeShortestPaths(topoOrder);
        long endTime = System.nanoTime();

        metrics.setExecutionTime((endTime - startTime) / 1_000_000.0);
    }


    private void computeShortestPaths(List<Integer> topoOrder) {
        dist[source] = 0;

        // Process vertices in topological order
        for (int u : topoOrder) {
            if (dist[u] != Integer.MAX_VALUE) {
                for (Edge edge : graph.getAdjList()[u]) {
                    int v = edge.to;
                    metrics.incrementRelaxations();

                    // Relaxation step
                    if (dist[u] + edge.weight < dist[v]) {
                        dist[v] = dist[u] + edge.weight;
                        parent[v] = u;
                    }
                }
            }
        }
    }


    public int getDistance(int vertex) {
        return dist[vertex];
    }


    public List<Integer> getPath(int vertex) {
        if (dist[vertex] == Integer.MAX_VALUE) {
            return Collections.emptyList();
        }

        List<Integer> path = new ArrayList<>();
        int current = vertex;

        while (current != -1) {
            path.add(current);
            current = parent[current];
        }

        Collections.reverse(path);
        return path;
    }


    public int[] getAllDistances() {
        return dist;
    }

    public graph.dagsp.Metrics getMetrics() {
        return metrics;
    }
}