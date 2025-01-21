import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class DocumentSearchEngine {
    
    // Inverted index to store the mapping of words to documents
    private final Map<String, Map<String, Integer>> invertedIndex = new HashMap<>();
    private final Map<String, Integer> docFrequency = new HashMap<>();
    private final Map<String, Integer> docWordCount = new HashMap<>();
    
    // Method to index documents from a directory
    public void indexDocuments(String directoryPath) throws IOException {
        Files.walk(Paths.get(directoryPath))
            .filter(Files::isRegularFile)
            .forEach(filePath -> {
                try {
                    indexFile(filePath.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    private void indexFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String documentName = Paths.get(filePath).getFileName().toString();
        String line;
        int wordCount = 0;

        while ((line = reader.readLine()) != null) {
            String[] words = line.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
            for (String word : words) {
                invertedIndex.computeIfAbsent(word, k -> new HashMap<>()).merge(documentName, 1, Integer::sum);
                wordCount++;
            }
        }
        docWordCount.put(documentName, wordCount);
        docFrequency.put(documentName, docFrequency.getOrDefault(documentName, 0) + 1);
        reader.close();
    }

    // Compute TF-IDF score for a given term and document
    private double calculateTFIDF(String term, String document) {
        int termFrequency = invertedIndex.getOrDefault(term, new HashMap<>()).getOrDefault(document, 0);
        int totalWords = docWordCount.getOrDefault(document, 1);
        double tf = (double) termFrequency / totalWords;

        int numDocsWithTerm = invertedIndex.getOrDefault(term, new HashMap<>()).size();
        int totalDocs = docFrequency.size();
        double idf = Math.log((double) totalDocs / (numDocsWithTerm + 1));

        return tf * idf;
    }

    // Search for the most relevant documents using TF-IDF ranking
    public List<String> search(String query) {
        String[] words = query.toLowerCase().split("\\s+");
        Map<String, Double> scores = new HashMap<>();

        for (String word : words) {
            if (invertedIndex.containsKey(word)) {
                for (String doc : invertedIndex.get(word).keySet()) {
                    scores.put(doc, scores.getOrDefault(doc, 0.0) + calculateTFIDF(word, doc));
                }
            }
        }

        return scores.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        DocumentSearchEngine engine = new DocumentSearchEngine();
        try {
            // Index documents from a folder
            engine.indexDocuments("/Users/umeshyadav/Library/Mobile Documents/com~apple~CloudDocs/ParentOfAll/projects/personal_projects/document_search_engine/documents");

            // Perform search queries
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter search query: ");
            String query = scanner.nextLine();
            List<String> results = engine.search(query);

            if (results.isEmpty()) {
                System.out.println("No documents found.");
            } else {
                System.out.println("Relevant documents: " + results);
            }
            
            scanner.close();
        } catch (IOException e) {
            System.out.println("Error reading documents: " + e.getMessage());
        }
    }
}
