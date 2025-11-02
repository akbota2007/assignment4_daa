package graph.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Dataset generator for testing graph algorithms
 * Generates 9 datasets: 3 small, 3 medium, 3 large
 */
public class DatasetGenerator {

    static class EdgeData {
        int u, v, w;

        EdgeData(int u, int v, int w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }

    static class GraphData {
        boolean directed = true;
        int n;
        EdgeData[] edges;
        int source;
        String weight_model = "edge";
    }

    private Random random;

    public DatasetGenerator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Generate a DAG (Directed Acyclic Graph)
     */
    public GraphData generateDAG(int n, double density) {
        List<EdgeData> edges = new ArrayList<>();

        // Generate edges only from lower to higher numbered vertices (ensures DAG)
        for (int u = 0; u < n - 1; u++) {
            for (int v = u + 1; v < n; v++) {
                if (random.nextDouble() < density) {
                    int weight = random.nextInt(10) + 1;
                    edges.add(new EdgeData(u, v, weight));
                }
            }
        }

        GraphData data = new GraphData();
        data.n = n;
        data.edges = edges.toArray(new EdgeData[0]);
        data.source = 0;
        return data;
    }

    /**
     * Generate a graph with cycles
     */
    public GraphData generateCyclic(int n, double density) {
        List<EdgeData> edges = new ArrayList<>();

        // First create a cycle to ensure it's cyclic
        for (int i = 0; i < n; i++) {
            int weight = random.nextInt(10) + 1;
            edges.add(new EdgeData(i, (i + 1) % n, weight));
        }

        // Add random additional edges
        int targetEdges = (int)(n * (n - 1) * density);
        while (edges.size() < targetEdges) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            if (u != v) {
                int weight = random.nextInt(10) + 1;
                edges.add(new EdgeData(u, v, weight));
            }
        }

        GraphData data = new GraphData();
        data.n = n;
        data.edges = edges.toArray(new EdgeData[0]);
        data.source = 0;
        return data;
    }

    /**
     * Generate a graph with multiple SCCs
     */
    public GraphData generateMultipleSCCs(int n, int numSCCs) {
        List<EdgeData> edges = new ArrayList<>();
        int nodesPerSCC = n / numSCCs;

        // Create strongly connected components
        for (int scc = 0; scc < numSCCs; scc++) {
            int start = scc * nodesPerSCC;
            int end = (scc == numSCCs - 1) ? n : start + nodesPerSCC;

            // Create cycle within SCC
            for (int i = start; i < end; i++) {
                int next = (i == end - 1) ? start : i + 1;
                int weight = random.nextInt(10) + 1;
                edges.add(new EdgeData(i, next, weight));
            }

            // Add some internal edges
            for (int i = start; i < end - 1; i++) {
                if (random.nextBoolean()) {
                    int j = start + random.nextInt(end - start);
                    if (i != j) {
                        int weight = random.nextInt(10) + 1;
                        edges.add(new EdgeData(i, j, weight));
                    }
                }
            }
        }

        // Add edges between SCCs (forward only to maintain DAG structure between SCCs)
        for (int i = 0; i < numSCCs - 1; i++) {
            int u = i * nodesPerSCC + random.nextInt(nodesPerSCC);
            int v = (i + 1) * nodesPerSCC + random.nextInt(nodesPerSCC);
            int weight = random.nextInt(10) + 1;
            edges.add(new EdgeData(u, v, weight));
        }

        GraphData data = new GraphData();
        data.n = n;
        data.edges = edges.toArray(new EdgeData[0]);
        data.source = 0;
        return data;
    }

    /**
     * Save graph data to JSON file
     */
    public void saveToFile(GraphData data, String filename) throws IOException {
        // Create parent directory if it doesn't exist
        java.io.File file = new java.io.File(filename);
        java.io.File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
            System.out.println("Created directory: " + parent.getPath());
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(data, writer);
        }
        System.out.println("Generated: " + filename +
                " (n=" + data.n + ", edges=" + data.edges.length + ")");
    }

    /**
     * Generate all 9 required datasets
     */
    public void generateAllDatasets(String outputDir) throws IOException {
        // Small datasets (6-10 nodes)
        saveToFile(generateDAG(6, 0.3), outputDir + "/small_dag_1.json");
        saveToFile(generateCyclic(8, 0.2), outputDir + "/small_cyclic_1.json");
        saveToFile(generateMultipleSCCs(10, 2), outputDir + "/small_mixed_1.json");

        // Medium datasets (10-20 nodes)
        saveToFile(generateDAG(12, 0.25), outputDir + "/medium_dag_1.json");
        saveToFile(generateCyclic(15, 0.2), outputDir + "/medium_cyclic_1.json");
        saveToFile(generateMultipleSCCs(18, 3), outputDir + "/medium_mixed_1.json");

        // Large datasets (20-50 nodes)
        saveToFile(generateDAG(25, 0.15), outputDir + "/large_dag_1.json");
        saveToFile(generateCyclic(35, 0.1), outputDir + "/large_cyclic_1.json");
        saveToFile(generateMultipleSCCs(40, 4), outputDir + "/large_mixed_1.json");
    }

    public static void main(String[] args) {
        try {
            DatasetGenerator generator = new DatasetGenerator(42); // Fixed seed for reproducibility
            generator.generateAllDatasets("data");
            System.out.println("\n✓ All 9 datasets generated successfully!");
        } catch (IOException e) {
            System.err.println("Error generating datasets: " + e.getMessage());
            e.printStackTrace();
        }
    }
}