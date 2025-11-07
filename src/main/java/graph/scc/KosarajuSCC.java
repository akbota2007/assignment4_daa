package graph.scc;

import graph.Graph;
import graph.Graph.Edge;
import java.util.*;


public class KosarajuSCC {
    private final Graph graph;
    private final List<List<Integer>> components;
    private final Metrics metrics;

    public KosarajuSCC(Graph graph) {
        this.graph = graph;
        this.components = new ArrayList<>();
        int[] nodeToComponent = new int[graph.getN()];
        this.metrics = new Metrics();

        Arrays.fill(nodeToComponent, -1);

        long startTime = System.nanoTime();
        findSCCs();
        long endTime = System.nanoTime();

        metrics.setExecutionTime((endTime - startTime) / 1_000_000.0);

        // Sort components by their minimum node
        components.sort(Comparator.comparingInt(Collections::min));

        // Update nodeToComponent mapping after sorting
        for (int i = 0; i < components.size(); i++) {
            for (int node : components.get(i)) {
                nodeToComponent[node] = i;
            }
        }
    }

    /**
     * Main Kosaraju's algorithm
     */
    private void findSCCs() {
        int n = graph.getN();

        // Step 1: First DFS pass on original graph to compute finish times
        boolean[] visited = new boolean[n];
        Stack<Integer> finishStack = new Stack<>();

        for (int v = 0; v < n; v++) {
            if (!visited[v]) {
                dfsFirstPass(v, visited, finishStack);
            }
        }

        // Step 2: Transpose (reverse) the graph
        List<Edge>[] transposed = transposeGraph();

        // Step 3: Second DFS pass on transposed graph
        Arrays.fill(visited, false);

        while (!finishStack.isEmpty()) {
            int v = finishStack.pop();
            if (!visited[v]) {
                List<Integer> component = new ArrayList<>();
                dfsSecondPass(v, visited, transposed, component);
                Collections.sort(component);
                components.add(component);
            }
        }
    }

    private void dfsFirstPass(int v, boolean[] visited, Stack<Integer> finishStack) {
        visited[v] = true;
        metrics.incrementDFSVisits();

        for (Edge edge : graph.getAdjList()[v]) {
            metrics.incrementEdgesExplored();
            int w = edge.to;
            if (!visited[w]) {
                dfsFirstPass(w, visited, finishStack);
            }
        }

        // Push vertex after visiting all descendants (finish time)
        finishStack.push(v);
    }

    /**
     * Second DFS pass: collect SCCs on transposed graph
     */
    private void dfsSecondPass(int v, boolean[] visited, List<Edge>[] transposed,
                               List<Integer> component) {
        visited[v] = true;
        component.add(v);
        metrics.incrementDFSVisits();

        for (Edge edge : transposed[v]) {
            metrics.incrementEdgesExplored();
            int w = edge.to;
            if (!visited[w]) {
                dfsSecondPass(w, visited, transposed, component);
            }
        }
    }

    /**
     * Transpose (reverse) the graph
     */
    @SuppressWarnings("unchecked")
    private List<Edge>[] transposeGraph() {
        int n = graph.getN();
        List<Edge>[] transposed = new ArrayList[n];

        for (int i = 0; i < n; i++) {
            transposed[i] = new ArrayList<>();
        }

        for (int u = 0; u < n; u++) {
            for (Edge edge : graph.getAdjList()[u]) {
                // Reverse edge direction: u -> v becomes v -> u
                transposed[edge.to].add(new Edge(u, edge.weight));
            }
        }

        return transposed;
    }

    public List<List<Integer>> getComponents() {
        return components;
    }

    public Metrics getMetrics() {
        return metrics;
    }

}