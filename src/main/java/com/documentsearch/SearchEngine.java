import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class SearchEngine {
    private final InvertedIndex invertedIndex;

    public SearchEngine(InvertedIndex index) {
        this.invertedIndex = index;
    }

    public List<String> search(String query) {
        String[] words = query.toLowerCase().split("\\s+");
        Map<String, Double> scores = new ConcurrentHashMap<>();
        
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        List<Future<Void>> futures = new ArrayList<>();

        for (String word : words) {
            futures.add(executorService.submit(() -> {
                Map<String, Integer> postings = invertedIndex.getIndex().get(word);
                if (postings != null) {
                    for (String doc : postings.keySet()) {
                        double score = calculateTFIDF(word, doc);
                        scores.merge(doc, score, Double::sum);
                    }
                }
                return null;
            }));
        }

        executorService.shutdown();
        try {
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateTFIDF(String term, String doc) {
        Map<String, Integer> termFreq = invertedIndex.getIndex().get(term);
        int termCount = termFreq != null ? termFreq.getOrDefault(doc, 0) : 0;
        int docLength = invertedIndex.getDocumentLength(doc);
        double tf = (double) termCount / docLength;

        int numDocsContainingTerm = termFreq.size();
        int totalDocs = invertedIndex.getIndex().size();
        double idf = Math.log((double) totalDocs / (numDocsContainingTerm + 1));

        return tf * idf;
    }
}
