package graph;

import graph.scc.TarjanSCC;
import graph.topo.TopologicalSort;
import graph.dagsp.DAGShortestPath;
import graph.dagsp.DAGLongestPath;

import java.util.List;


public class Main {

    public static void main(String[] args) {
        System.out.println("=== DAA Assignment 4: Graph Algorithms ===\n");

        // Test 1: Simple DAG
        testSimpleDAG();

        // Test 2: Graph with cycle
        testGraphWithCycle();


    }

    public static void testSimpleDAG() {
        System.out.println("Test 1: Simple DAG (0->1->2->3)");
        System.out.println("-".repeat(50));

        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 2);
        g.addEdge(1, 2, 3);
        g.addEdge(2, 3, 1);

        // SCC
        TarjanSCC scc = new TarjanSCC(g);
        System.out.println("SCCs found: " + scc.getComponents().size());
        for (int i = 0; i < scc.getComponents().size(); i++) {
            System.out.println("  SCC " + i + ": " + scc.getComponents().get(i));
        }
        System.out.println("SCC Metrics: " + scc.getMetrics());

        // Topological Sort
        TopologicalSort topo = new TopologicalSort(g);
        System.out.println("\nTopological Order: " + topo.getOrder());
        System.out.println("Is DAG: " + topo.isDAG());
        System.out.println("Topo Metrics: " + topo.getMetrics());

        // Shortest Path
        DAGShortestPath sp = new DAGShortestPath(g, 0, topo.getOrder());
        System.out.println("\nShortest paths from source 0:");
        for (int i = 0; i < g.getN(); i++) {
            System.out.println("  To node " + i + ": distance=" + sp.getDistance(i) +
                    ", path=" + sp.getPath(i));
        }
        System.out.println("SP Metrics: " + sp.getMetrics());

        // Longest Path
        DAGLongestPath lp = new DAGLongestPath(g, topo.getOrder());
        System.out.println("\nCritical Path: " + lp.getCriticalPath());
        System.out.println("Critical Length: " + lp.getCriticalLength());
        System.out.println("LP Metrics: " + lp.getMetrics());

        System.out.println("\n" + "=".repeat(50) + "\n");
    }

    public static void testGraphWithCycle() {
        System.out.println("Test 2: Graph with Cycle (1->2->3->1)");
        System.out.println("-".repeat(50));

        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 2);
        g.addEdge(2, 3, 3);
        g.addEdge(3, 1, 1);  // Creates cycle
        g.addEdge(3, 4, 2);

        // SCC
        TarjanSCC scc = new TarjanSCC(g);
        System.out.println("SCCs found: " + scc.getComponents().size());
        for (int i = 0; i < scc.getComponents().size(); i++) {
            List<Integer> comp = scc.getComponents().get(i);
            System.out.println("  SCC " + i + ": " + comp +
                    (comp.size() > 1 ? " <- CYCLE!" : ""));
        }
        System.out.println("SCC Metrics: " + scc.getMetrics());

        // Build condensation (DAG)
        Graph condensation = scc.buildCondensation();
        System.out.println("\nCondensation graph nodes: " + condensation.getN());

        // Topological Sort on condensation
        TopologicalSort topo = new TopologicalSort(condensation);
        System.out.println("Condensation topo order: " + topo.getOrder());
        System.out.println("Is DAG: " + topo.isDAG());

        // Get task order
        List<Integer> taskOrder = scc.getTaskOrder(topo.getOrder());
        System.out.println("Task execution order: " + taskOrder);

        System.out.println("\n" + "=".repeat(50) + "\n");
    }

    public static void testFromFile() {
        System.out.println("Test 3: Load from tasks.json");
        System.out.println("-".repeat(50));

        try {
            Graph g = Graph.fromJSON("data/tasks.json");
            System.out.println("Graph loaded: " + g.getN() + " nodes");

            // Run all algorithms
            TarjanSCC scc = new TarjanSCC(g);
            System.out.println("\nSCCs: " + scc.getComponents().size());
            for (int i = 0; i < scc.getComponents().size(); i++) {
                System.out.println("  SCC " + i + ": " + scc.getComponents().get(i));
            }

            Graph condensation = scc.buildCondensation();
            TopologicalSort topo = new TopologicalSort(condensation);
            System.out.println("\nTopo order: " + topo.getOrder());

            // If pure DAG, compute paths
            if (scc.getComponents().size() == g.getN()) {
                TopologicalSort graphTopo = new TopologicalSort(g);
                DAGShortestPath sp = new DAGShortestPath(g, g.getSource(), graphTopo.getOrder());
                DAGLongestPath lp = new DAGLongestPath(g, graphTopo.getOrder());

                System.out.println("\nShortest from source " + g.getSource() + ":");
                for (int i = 0; i < g.getN(); i++) {
                    if (sp.getDistance(i) != Integer.MAX_VALUE) {
                        System.out.println("  Node " + i + ": " + sp.getDistance(i));
                    }
                }

                System.out.println("\nCritical path: " + lp.getCriticalPath());
                System.out.println("Critical length: " + lp.getCriticalLength());
            } else {
                System.out.println("\nGraph has cycles - use condensation for paths");
            }

        } catch (Exception e) {
            System.out.println("Error loading file: " + e.getMessage());
            System.out.println("Make sure data/tasks.json exists!");
        }

        System.out.println("\n" + "=".repeat(50) + "\n");
    }
}