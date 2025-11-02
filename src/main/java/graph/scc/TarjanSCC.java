package graph.scc;

import graph.Graph;
import graph.Graph.Edge;
import java.util.*;


public class TarjanSCC {
    private final Graph graph;
    private final int[] index;      // Discovery time
    private final int[] lowlink;    // Lowest reachable index
    private final boolean[] onStack;
    private final Stack<Integer> stack;
    private final List<List<Integer>> components;
    private final int[] nodeToComponent; // Maps node to its component index
    private int currentIndex;
    private final Metrics metrics;

    public TarjanSCC(Graph graph) {
        this.graph = graph;
        int n = graph.getN();
        this.index = new int[n];
        this.lowlink = new int[n];
        this.onStack = new boolean[n];
        this.stack = new Stack<>();
        this.components = new ArrayList<>();
        this.nodeToComponent = new int[n];
        this.currentIndex = 0;
        this.metrics = new Metrics();

        Arrays.fill(index, -1);
        Arrays.fill(nodeToComponent, -1);

        long startTime = System.nanoTime();

        // Run Tarjan's algorithm
        for (int v = 0; v < n; v++) {
            if (index[v] == -1) {
                strongConnect(v);
            }
        }

        long endTime = System.nanoTime();
        metrics.setExecutionTime((endTime - startTime) / 1_000_000.0); // Convert to ms

        // Sort components by their minimum node for consistency
        components.sort(Comparator.comparingInt(Collections::min));

        // Update nodeToComponent mapping after sorting
        for (int i = 0; i < components.size(); i++) {
            for (int node : components.get(i)) {
                nodeToComponent[node] = i;
            }
        }
    }

    private void strongConnect(int v) {
        // Set the depth index for v
        index[v] = currentIndex;
        lowlink[v] = currentIndex;
        currentIndex++;
        stack.push(v);
        onStack[v] = true;
        metrics.incrementDFSVisits();

        // Consider successors of v
        for (Edge edge : graph.getAdjList()[v]) {
            int w = edge.to;
            metrics.incrementEdgesExplored();

            if (index[w] == -1) {
                // Successor w has not yet been visited; recurse on it
                strongConnect(w);
                lowlink[v] = Math.min(lowlink[v], lowlink[w]);
            } else if (onStack[w]) {
                // Successor w is in stack and hence in the current SCC
                lowlink[v] = Math.min(lowlink[v], index[w]);
            }
        }

        // If v is a root node, pop the stack and create an SCC
        if (lowlink[v] == index[v]) {
            List<Integer> component = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                component.add(w);
            } while (w != v);

            Collections.sort(component); // Sort nodes within component
            components.add(component);
        }
    }


    public Graph buildCondensation() {
        int numComponents = components.size();
        Graph condensation = new Graph(numComponents, true);

        Set<String> addedEdges = new HashSet<>();

        // Add edges between different components
        for (int u = 0; u < graph.getN(); u++) {
            int compU = nodeToComponent[u];
            for (Edge edge : graph.getAdjList()[u]) {
                int v = edge.to;
                int compV = nodeToComponent[v];

                if (compU != compV) {
                    String edgeKey = compU + "-" + compV;
                    if (!addedEdges.contains(edgeKey)) {
                        condensation.addEdge(compU, compV, edge.weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }

        return condensation;
    }

    public List<List<Integer>> getComponents() {
        return components;
    }

    public Metrics getMetrics() {
        return metrics;
    }


    public List<Integer> getTaskOrder(List<Integer> sccTopoOrder) {
        List<Integer> taskOrder = new ArrayList<>();
        for (int sccIdx : sccTopoOrder) {
            taskOrder.addAll(components.get(sccIdx));
        }
        return taskOrder;
    }
}