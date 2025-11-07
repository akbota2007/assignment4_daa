package graph;

import graph.scc.TarjanSCC;
import graph.scc.KosarajuSCC;


public class CompareSCCAlgorithms {

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("COMPARISON: Tarjan vs Kosaraju SCC Algorithms");
        System.out.println("=".repeat(80));
        System.out.println();

        // Test on different graph sizes
        testGraph("Small Graph (8 nodes)", createSmallGraph());
        testGraph("Medium Graph (15 nodes)", createMediumGraph());
        testGraph("Large Graph with Cycles", createLargeGraph());

        // Test from file if available
        try {
            Graph g = Graph.fromJSON("data/tasks.json");
            testGraph("tasks.json", g);
        } catch (Exception e) {
            System.out.println("Note: data/tasks.json not found, skipping file test\n");
        }
    }

    private static void testGraph(String name, Graph g) {
        System.out.println("Testing: " + name);
        System.out.println("-".repeat(80));
        System.out.println("Graph: " + g.getN() + " nodes");

        // Count edges
        int edgeCount = 0;
        for (int i = 0; i < g.getN(); i++) {
            edgeCount += g.getAdjList()[i].size();
        }
        System.out.println("Edges: " + edgeCount);
        System.out.println();

        // Run Tarjan
        System.out.println("📊 TARJAN'S ALGORITHM:");
        TarjanSCC tarjan = new TarjanSCC(g);
        System.out.println("  SCCs found: " + tarjan.getComponents().size());
        System.out.println("  Components: " + tarjan.getComponents());
        System.out.println("  Metrics: " + tarjan.getMetrics());
        System.out.println();

        // Run Kosaraju
        System.out.println("📊 KOSARAJU'S ALGORITHM:");
        KosarajuSCC kosaraju = new KosarajuSCC(g);
        System.out.println("  SCCs found: " + kosaraju.getComponents().size());
        System.out.println("  Components: " + kosaraju.getComponents());
        System.out.println("  Metrics: " + kosaraju.getMetrics());
        System.out.println();

        // Verify results match
        boolean match = tarjan.getComponents().size() == kosaraju.getComponents().size();
        System.out.println("✓ Results match: " + (match ? "YES" : "NO"));

        // Compare performance
        double tarjanTime = tarjan.getMetrics().getExecutionTime();
        double kosarajuTime = kosaraju.getMetrics().getExecutionTime();
        String faster = tarjanTime < kosarajuTime ? "Tarjan" : "Kosaraju";
        double speedup = Math.max(tarjanTime, kosarajuTime) / Math.min(tarjanTime, kosarajuTime);

        System.out.println("⚡ Performance:");
        System.out.println("  Tarjan:   " + String.format("%.3f ms", tarjanTime));
        System.out.println("  Kosaraju: " + String.format("%.3f ms", kosarajuTime));
        System.out.println("  Faster: " + faster + " (" + String.format("%.2fx", speedup) + ")");

        System.out.println("\n" + "=".repeat(80) + "\n");
    }

    // Create test graphs
    private static Graph createSmallGraph() {
        Graph g = new Graph(8, true);
        g.addEdge(0, 1, 3);
        g.addEdge(1, 2, 2);
        g.addEdge(2, 3, 4);
        g.addEdge(3, 1, 1);  // Cycle: 1->2->3->1
        g.addEdge(4, 5, 2);
        g.addEdge(5, 6, 5);
        g.addEdge(6, 7, 1);
        return g;
    }

    private static Graph createMediumGraph() {
        Graph g = new Graph(15, true);
        // First SCC (cycle)
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);

        // Second SCC (cycle)
        g.addEdge(3, 4, 1);
        g.addEdge(4, 5, 1);
        g.addEdge(5, 3, 1);

        // Connect SCCs
        g.addEdge(2, 3, 1);

        // Third SCC
        g.addEdge(6, 7, 1);
        g.addEdge(7, 8, 1);
        g.addEdge(8, 6, 1);

        g.addEdge(5, 6, 1);

        // Add more nodes
        for (int i = 9; i < 15; i++) {
            g.addEdge(i, (i + 1) % 15, 1);
        }

        return g;
    }

    private static Graph createLargeGraph() {
        Graph g = new Graph(30, true);

        // Create multiple SCCs with different sizes
        // SCC 1: nodes 0-4 (size 5)
        for (int i = 0; i < 5; i++) {
            g.addEdge(i, (i + 1) % 5, 1);
        }

        // SCC 2: nodes 5-9 (size 5)
        for (int i = 5; i < 10; i++) {
            g.addEdge(i, 5 + ((i - 5 + 1) % 5), 1);
        }

        // SCC 3: nodes 10-14 (size 5)
        for (int i = 10; i < 15; i++) {
            g.addEdge(i, 10 + ((i - 10 + 1) % 5), 1);
        }

        // Connect SCCs
        g.addEdge(4, 5, 2);
        g.addEdge(9, 10, 2);

        // Add DAG tail
        for (int i = 15; i < 29; i++) {
            g.addEdge(i, i + 1, 1);
        }
        g.addEdge(14, 15, 2);

        return g;
    }
}
