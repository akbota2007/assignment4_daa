package graph;

import com.google.gson.Gson;
import java.io.FileReader;
import java.util.*;


public class Graph {
    private final int n; // number of vertices
    private final List<Edge>[] adjList;
    private final boolean directed;
    private int source;
    private String weightModel;

    public String getWeightModel() {
        return weightModel;
    }

    public void setWeightModel(String weightModel) {
        this.weightModel = weightModel;
    }

    public static class Edge {
        public int to;
        public int weight;

        public Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    public static class GraphData {
        public boolean directed;
        public int n;
        public EdgeData[] edges;
        public int source;
        public String weight_model;
    }

    public static class EdgeData {
        public int u;
        public int v;
        public int w;
    }

    @SuppressWarnings("unchecked")
    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        this.adjList = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            adjList[i] = new ArrayList<>();
        }
    }

    /**
     * Load graph from JSON file
     */
    public static Graph fromJSON(String filepath) throws Exception {
        Gson gson = new Gson();
        GraphData data = gson.fromJson(new FileReader(filepath), GraphData.class);

        Graph g = new Graph(data.n, data.directed);
        g.source = data.source;
        g.weightModel = data.weight_model;

        for (EdgeData e : data.edges) {
            g.addEdge(e.u, e.v, e.w);
        }

        return g;
    }

    public void addEdge(int u, int v, int weight) {
        adjList[u].add(new Edge(v, weight));
    }

    public List<Edge>[] getAdjList() {
        return adjList;
    }

    public int getN() {
        return n;
    }

    public int getSource() {
        return source;
    }

    public boolean isDirected() {
        return directed;
    }

    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        try {
            // Load graph
            Graph g = Graph.fromJSON("data/tasks.json");
            System.out.println("Graph loaded: " + g.getN() + " vertices");

            // Run SCC
            graph.scc.TarjanSCC scc = new graph.scc.TarjanSCC(g);
            System.out.println("\n=== SCC Results ===");
            System.out.println("Number of SCCs: " + scc.getComponents().size());
            for (int i = 0; i < scc.getComponents().size(); i++) {
                System.out.println("SCC " + i + ": " + scc.getComponents().get(i));
            }
            System.out.println("SCC Metrics: " + scc.getMetrics());

            // Build condensation
            Graph condensation = scc.buildCondensation();
            System.out.println("\n=== Condensation Graph ===");
            System.out.println("Nodes: " + condensation.getN());

            // Topological sort
            graph.topo.TopologicalSort topo = new graph.topo.TopologicalSort(condensation);
            System.out.println("\n=== Topological Order ===");
            System.out.println("Order: " + topo.getOrder());
            System.out.println("Topo Metrics: " + topo.getMetrics());

            // Check if original graph is DAG
            if (scc.getComponents().size() == g.getN()) {
                System.out.println("\n=== DAG Shortest/Longest Paths ===");
                graph.topo.TopologicalSort graphTopo = new graph.topo.TopologicalSort(g);

                // Shortest paths
                graph.dagsp.DAGShortestPath sp = new graph.dagsp.DAGShortestPath(g, g.getSource(), graphTopo.getOrder());
                System.out.println("Shortest distances from source " + g.getSource() + ":");
                for (int i = 0; i < g.getN(); i++) {
                    if (sp.getDistance(i) != Integer.MAX_VALUE) {
                        System.out.println("  Node " + i + ": " + sp.getDistance(i) +
                                " Path: " + sp.getPath(i));
                    }
                }
                System.out.println("SP Metrics: " + sp.getMetrics());

                // Longest path
                graph.dagsp.DAGLongestPath lp = new graph.dagsp.DAGLongestPath(g, graphTopo.getOrder());
                System.out.println("\nCritical Path: " + lp.getCriticalPath());
                System.out.println("Critical Length: " + lp.getCriticalLength());
                System.out.println("LP Metrics: " + lp.getMetrics());
            } else {
                System.out.println("\nGraph has cycles - shortest/longest paths computed on condensation only");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}