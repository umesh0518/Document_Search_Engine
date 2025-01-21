import java.io.*;
import java.util.*;

public class InvertedIndex {
    private final Map<String, Map<String, Integer>> index = new HashMap<>();
    private final Map<String, Integer> docLengths = new HashMap<>();

    public void indexDocuments(String directoryPath) throws IOException {
        File folder = new File(directoryPath);
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                indexFile(file);
            }
        }
    }

    private void indexFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String documentName = file.getName();
        String line;
        int totalWords = 0;

        while ((line = reader.readLine()) != null) {
            String[] words = line.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
            for (String word : words) {
                index.computeIfAbsent(word, k -> new HashMap<>())
                     .merge(documentName, 1, Integer::sum);
                totalWords++;
            }
        }
        docLengths.put(documentName, totalWords);
        reader.close();
    }

    public Map<String, Map<String, Integer>> getIndex() {
        return index;
    }

    public int getDocumentLength(String docName) {
        return docLengths.getOrDefault(docName, 1);
    }
}
