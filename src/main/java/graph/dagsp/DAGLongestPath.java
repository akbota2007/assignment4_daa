package graph.dagsp;

import graph.Graph;
import graph.Graph.Edge;
import java.util.*;

public class DAGLongestPath {
    private Graph graph;
    private int[] dist;
    private int[] parent;
    private List<Integer> criticalPath;
    private int criticalLength;
    private graph.dagsp.Metrics metrics;

    public DAGLongestPath(Graph graph, List<Integer> topoOrder) {
        this.graph = graph;
        this.dist = new int[graph.getN()];
        this.parent = new int[graph.getN()];
        this.metrics = new graph.dagsp.Metrics();

        Arrays.fill(dist, Integer.MIN_VALUE);
        Arrays.fill(parent, -1);

        long startTime = System.nanoTime();
        computeLongestPaths(topoOrder);
        long endTime = System.nanoTime();

        metrics.setExecutionTime((endTime - startTime) / 1_000_000.0);
    }


    private void computeLongestPaths(List<Integer> topoOrder) {
        // Initialize distances for source nodes (no incoming edges)
        boolean[] hasIncoming = new boolean[graph.getN()];
        for (int u = 0; u < graph.getN(); u++) {
            for (Edge edge : graph.getAdjList()[u]) {
                hasIncoming[edge.to] = true;
            }
        }

        for (int i = 0; i < graph.getN(); i++) {
            if (!hasIncoming[i]) {
                dist[i] = 0;
            }
        }

        // Process vertices in topological order
        for (int u : topoOrder) {
            if (dist[u] != Integer.MIN_VALUE) {
                for (Edge edge : graph.getAdjList()[u]) {
                    int v = edge.to;
                    metrics.incrementRelaxations();

                    // Relaxation for longest path (maximize instead of minimize)
                    if (dist[u] + edge.weight > dist[v]) {
                        dist[v] = dist[u] + edge.weight;
                        parent[v] = u;
                    }
                }
            }
        }

        // Find the critical path (longest path ending anywhere)
        criticalLength = Integer.MIN_VALUE;
        int endNode = -1;

        for (int i = 0; i < graph.getN(); i++) {
            if (dist[i] > criticalLength) {
                criticalLength = dist[i];
                endNode = i;
            }
        }

        // Reconstruct critical path
        criticalPath = new ArrayList<>();
        if (endNode != -1) {
            int current = endNode;
            while (current != -1) {
                criticalPath.add(current);
                current = parent[current];
            }
            Collections.reverse(criticalPath);
        }
    }


    public int getDistance(int vertex) {
        return dist[vertex];
    }


    public List<Integer> getCriticalPath() {
        return criticalPath;
    }


    public int getCriticalLength() {
        return criticalLength;
    }


    public List<Integer> getPath(int vertex) {
        if (dist[vertex] == Integer.MIN_VALUE) {
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

    public graph.dagsp.Metrics getMetrics() {
        return metrics;
    }
}