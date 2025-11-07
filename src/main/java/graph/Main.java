package graph;

import graph.scc.TarjanSCC;
import graph.scc.KosarajuSCC;
import graph.topo.TopologicalSort;
import graph.dagsp.DAGShortestPath;
import graph.dagsp.DAGLongestPath;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


class RunAllDatasets {

    static class DatasetResult {
        String filename;
        int nodes;
        int edges;

        // SCC results (Tarjan)
        int sccs;
        int largestSCC;
        int tarjanDFSVisits;
        int tarjanEdgesExplored;
        double tarjanTime;

        // SCC results (Kosaraju)
        int kosarajuDFSVisits;
        int kosarajuEdgesExplored;
        double kosarajuTime;

        // Topological sort
        int topoPushes;
        int topoPops;
        double topoTime;
        boolean isDAG;

        // DAG Shortest Path (if applicable)
        int spRelaxations;
        double spTime;
        Integer maxDistance;

        // DAG Longest Path (if applicable)
        int lpRelaxations;
        double lpTime;
        int criticalLength;
        String criticalPath;
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(100));
        System.out.println("DAA ASSIGNMENT 4: COMPREHENSIVE ANALYSIS OF ALL DATASETS");
        System.out.println("=".repeat(100));
        System.out.println();

        String[] datasets = {
                "data/small_dag_1.json",
                "data/small_cyclic_1.json",
                "data/small_mixed_1.json",
                "data/medium_dag_1.json",
                "data/medium_cyclic_1.json",
                "data/medium_mixed_1.json",
                "data/large_dag_1.json",
                "data/large_cyclic_1.json",
                "data/large_mixed_1.json"
        };

        List<DatasetResult> results = new ArrayList<>();

        // Process each dataset
        for (String dataset : datasets) {
            try {
                System.out.println("Processing: " + dataset);
                System.out.println("-".repeat(100));

                DatasetResult result = processDataset(dataset);
                results.add(result);

                printSummary(result);
                System.out.println();

            } catch (Exception e) {
                System.out.println("⚠️  Error processing " + dataset + ": " + e.getMessage());
                System.out.println("     (File might not exist - run DatasetGenerator first!)");
                System.out.println();
            }
        }

        // Generate report tables
        if (!results.isEmpty()) {
            System.out.println("\n" + "=".repeat(100));
            System.out.println("REPORT TABLES (Copy to your report document)");
            System.out.println("=".repeat(100));

            printTable1_DatasetOverview(results);
            printTable2_TarjanVsKosaraju(results);
            printTable3_SCCDetection(results);
            printTable4_TopologicalSort(results);
            printTable5_DAGPaths(results);

            // Save to file
            saveResultsToFile(results);
        }

        System.out.println("\n✅ Analysis complete!");
        System.out.println("📊 Results saved to: results_summary.txt");
    }

    private static DatasetResult processDataset(String filepath) throws Exception {
        DatasetResult result = new DatasetResult();
        result.filename = filepath.substring(filepath.lastIndexOf('/') + 1);

        // Load graph
        Graph g = Graph.fromJSON(filepath);
        result.nodes = g.getN();

        // Count edges
        result.edges = 0;
        for (int i = 0; i < g.getN(); i++) {
            result.edges += g.getAdjList()[i].size();
        }

        // Run Tarjan SCC
        TarjanSCC tarjan = new TarjanSCC(g);
        result.sccs = tarjan.getComponents().size();
        result.largestSCC = tarjan.getComponents().stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);
        result.tarjanDFSVisits = tarjan.getMetrics().getDfsVisits();
        result.tarjanEdgesExplored = tarjan.getMetrics().getEdgesExplored();
        result.tarjanTime = tarjan.getMetrics().getExecutionTime();

        // Run Kosaraju SCC
        KosarajuSCC kosaraju = new KosarajuSCC(g);
        result.kosarajuDFSVisits = kosaraju.getMetrics().getDfsVisits();
        result.kosarajuEdgesExplored = kosaraju.getMetrics().getEdgesExplored();
        result.kosarajuTime = kosaraju.getMetrics().getExecutionTime();

        // Build condensation
        Graph condensation = tarjan.buildCondensation();

        // Topological sort on condensation
        TopologicalSort topo = new TopologicalSort(condensation);
        result.topoPushes = topo.getMetrics().getPushes();
        result.topoPops = topo.getMetrics().getPops();
        result.topoTime = topo.getMetrics().getExecutionTime();
        result.isDAG = topo.isDAG();

        // If original graph is DAG, compute paths
        if (result.sccs == result.nodes) {
            TopologicalSort graphTopo = new TopologicalSort(g);

            if (graphTopo.isDAG()) {
                int source = g.getSource();

                // Shortest paths
                DAGShortestPath sp = new DAGShortestPath(g, source, graphTopo.getOrder());
                result.spRelaxations = sp.getMetrics().getRelaxations();
                result.spTime = sp.getMetrics().getExecutionTime();

                // Find max distance
                int maxDist = 0;
                for (int i = 0; i < g.getN(); i++) {
                    if (sp.getDistance(i) != Integer.MAX_VALUE) {
                        maxDist = Math.max(maxDist, sp.getDistance(i));
                    }
                }
                result.maxDistance = maxDist;

                // Longest path
                DAGLongestPath lp = new DAGLongestPath(g, graphTopo.getOrder());
                result.lpRelaxations = lp.getMetrics().getRelaxations();
                result.lpTime = lp.getMetrics().getExecutionTime();
                result.criticalLength = lp.getCriticalLength();
                result.criticalPath = lp.getCriticalPath().toString();
            }
        }

        return result;
    }

    private static void printSummary(DatasetResult r) {
        System.out.println("Dataset: " + r.filename);
        System.out.println("  Nodes: " + r.nodes + ", Edges: " + r.edges);
        System.out.println("  SCCs: " + r.sccs + ", Largest SCC: " + r.largestSCC);
        System.out.println("  Tarjan:   " + String.format("%.3f ms", r.tarjanTime));
        System.out.println("  Kosaraju: " + String.format("%.3f ms", r.kosarajuTime));
        if (r.maxDistance != null) {
            System.out.println("  Critical Path Length: " + r.criticalLength);
        } else {
            System.out.println("  Type: Cyclic (paths computed on condensation)");
        }
    }

    private static void printTable1_DatasetOverview(List<DatasetResult> results) {
        System.out.println("\n### Table 1: Dataset Overview");
        System.out.println("```");
        System.out.println("| Dataset          | Nodes | Edges | Density | SCCs | Largest SCC | Type    |");
        System.out.println("|------------------|-------|-------|---------|------|-------------|---------|");

        for (DatasetResult r : results) {
            double density = (double) r.edges / (r.nodes * (r.nodes - 1));
            String type = r.sccs == r.nodes ? "DAG" : "Cyclic";
            System.out.printf("| %-16s | %5d | %5d | %7.3f | %4d | %11d | %-7s |\n",
                    r.filename, r.nodes, r.edges, density, r.sccs, r.largestSCC, type);
        }
        System.out.println("```");
    }

    private static void printTable2_TarjanVsKosaraju(List<DatasetResult> results) {
        System.out.println("\n### Table 2: Tarjan vs Kosaraju Comparison");
        System.out.println("```");
        System.out.println("| Dataset          | Nodes | Tarjan Time | Kosaraju Time | Faster   | Speedup |");
        System.out.println("|------------------|-------|-------------|---------------|----------|---------|");

        for (DatasetResult r : results) {
            String faster = r.tarjanTime < r.kosarajuTime ? "Tarjan" : "Kosaraju";
            double speedup = Math.max(r.tarjanTime, r.kosarajuTime) /
                    Math.min(r.tarjanTime, r.kosarajuTime);
            System.out.printf("| %-16s | %5d | %11.3f | %13.3f | %-8s | %7.2fx |\n",
                    r.filename, r.nodes, r.tarjanTime, r.kosarajuTime, faster, speedup);
        }
        System.out.println("```");
    }

    private static void printTable3_SCCDetection(List<DatasetResult> results) {
        System.out.println("\n### Table 3: SCC Detection (Tarjan)");
        System.out.println("```");
        System.out.println("| Dataset          | Nodes | Edges | SCCs | DFS Visits | Edges Explored | Time (ms) |");
        System.out.println("|------------------|-------|-------|------|------------|----------------|-----------|");

        for (DatasetResult r : results) {
            System.out.printf("| %-16s | %5d | %5d | %4d | %10d | %14d | %9.3f |\n",
                    r.filename, r.nodes, r.edges, r.sccs,
                    r.tarjanDFSVisits, r.tarjanEdgesExplored, r.tarjanTime);
        }
        System.out.println("```");
    }

    private static void printTable4_TopologicalSort(List<DatasetResult> results) {
        System.out.println("\n### Table 4: Topological Sort (on Condensation)");
        System.out.println("```");
        System.out.println("| Dataset          | Nodes | SCCs | Pushes | Pops | Time (ms) | Is DAG? |");
        System.out.println("|------------------|-------|------|--------|------|-----------|---------|");

        for (DatasetResult r : results) {
            System.out.printf("| %-16s | %5d | %4d | %6d | %4d | %9.3f | %-7s |\n",
                    r.filename, r.nodes, r.sccs, r.topoPushes, r.topoPops,
                    r.topoTime, r.isDAG ? "Yes" : "No");
        }
        System.out.println("```");
    }

    private static void printTable5_DAGPaths(List<DatasetResult> results) {
        System.out.println("\n### Table 5: DAG Shortest/Longest Paths");
        System.out.println("```");
        System.out.println("| Dataset          | Max Dist | SP Relax | SP Time | Critical Len | LP Relax | LP Time |");
        System.out.println("|------------------|----------|----------|---------|--------------|----------|---------|");

        for (DatasetResult r : results) {
            if (r.maxDistance != null) {
                System.out.printf("| %-16s | %8d | %8d | %7.3f | %12d | %8d | %7.3f |\n",
                        r.filename, r.maxDistance, r.spRelaxations, r.spTime,
                        r.criticalLength, r.lpRelaxations, r.lpTime);
            } else {
                System.out.printf("| %-16s | %8s | %8s | %7s | %12s | %8s | %7s |\n",
                        r.filename, "N/A", "N/A", "N/A", "N/A", "N/A", "N/A");
            }
        }
        System.out.println("```");
    }

    private static void saveResultsToFile(List<DatasetResult> results) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("results_summary.txt"))) {
            writer.println("DAA ASSIGNMENT 4 - RESULTS SUMMARY");
            writer.println("=".repeat(100));
            writer.println();

            for (DatasetResult r : results) {
                writer.println("Dataset: " + r.filename);
                writer.println("  Nodes: " + r.nodes + ", Edges: " + r.edges);
                writer.println("  SCCs: " + r.sccs + ", Largest SCC: " + r.largestSCC);
                writer.println("  Tarjan:   " + String.format("%.3f ms (DFS: %d, Edges: %d)",
                        r.tarjanTime, r.tarjanDFSVisits, r.tarjanEdgesExplored));
                writer.println("  Kosaraju: " + String.format("%.3f ms (DFS: %d, Edges: %d)",
                        r.kosarajuTime, r.kosarajuDFSVisits, r.kosarajuEdgesExplored));

                if (r.maxDistance != null) {
                    writer.println("  Shortest Path: Max distance = " + r.maxDistance +
                            ", Relaxations = " + r.spRelaxations);
                    writer.println("  Longest Path: Critical length = " + r.criticalLength +
                            ", Path = " + r.criticalPath);
                }
                writer.println();
            }

            writer.println("Summary:");
            writer.println("-".repeat(100));

            double avgTarjan = results.stream().mapToDouble(r -> r.tarjanTime).average().orElse(0);
            double avgKosaraju = results.stream().mapToDouble(r -> r.kosarajuTime).average().orElse(0);

            writer.println("Average Tarjan Time:   " + String.format("%.3f ms", avgTarjan));
            writer.println("Average Kosaraju Time: " + String.format("%.3f ms", avgKosaraju));

            int tarjanFaster = 0;
            for (DatasetResult r : results) {
                if (r.tarjanTime < r.kosarajuTime) tarjanFaster++;
            }

            writer.println("Tarjan faster in: " + tarjanFaster + "/" + results.size() + " cases");

        } catch (Exception e) {
            System.out.println("Error saving results: " + e.getMessage());
        }
    }
}