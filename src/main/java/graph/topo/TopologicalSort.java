package graph.topo;

import graph.Graph;
import graph.Graph.Edge;
import java.util.*;


public class TopologicalSort {
    private Graph graph;
    private List<Integer> order;
    private boolean isDAG;
    private Metrics metrics;

    public TopologicalSort(Graph graph) {
        this.graph = graph;
        this.order = new ArrayList<>();
        this.metrics = new Metrics();
        this.isDAG = false;

        long startTime = System.nanoTime();
        kahnSort();
        long endTime = System.nanoTime();

        metrics.setExecutionTime((endTime - startTime) / 1_000_000.0);
    }


    private void kahnSort() {
        int n = graph.getN();
        int[] inDegree = new int[n];

        // Calculate in-degrees
        for (int u = 0; u < n; u++) {
            for (Edge edge : graph.getAdjList()[u]) {
                inDegree[edge.to]++;
            }
        }

        // Initialize queue with all vertices having in-degree 0
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
                metrics.incrementPushes();
            }
        }

        // Process vertices
        while (!queue.isEmpty()) {
            int u = queue.poll();
            metrics.incrementPops();
            order.add(u);

            // Reduce in-degree for all neighbors
            for (Edge edge : graph.getAdjList()[u]) {
                inDegree[edge.to]--;
                if (inDegree[edge.to] == 0) {
                    queue.offer(edge.to);
                    metrics.incrementPushes();
                }
            }
        }

        // Check if topological sort is possible (DAG)
        isDAG = (order.size() == n);
    }

    public static List<Integer> dfsTopologicalSort(Graph graph) {
        int n = graph.getN();
        boolean[] visited = new boolean[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                dfsVisit(graph, i, visited, stack);
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!stack.isEmpty()) {
            order.add(stack.pop());
        }
        return order;
    }

    private static void dfsVisit(Graph graph, int v, boolean[] visited, Stack<Integer> stack) {
        visited[v] = true;
        for (Edge edge : graph.getAdjList()[v]) {
            if (!visited[edge.to]) {
                dfsVisit(graph, edge.to, visited, stack);
            }
        }
        stack.push(v);
    }

    public List<Integer> getOrder() {
        return order;
    }

    public boolean isDAG() {
        return isDAG;
    }

    public Metrics getMetrics() {
        return metrics;
    }
}
