import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class DocumentSearchEngine {
    public static void main(String[] args) {
        String directoryPath = "documents/";
        InvertedIndex invertedIndex = new InvertedIndex();

        try {
            invertedIndex.indexDocuments(directoryPath);
            SearchEngine searchEngine = new SearchEngine(invertedIndex);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter search query: ");
            String query = scanner.nextLine();

            List<String> results = searchEngine.search(query);

            if (results.isEmpty()) {
                System.out.println("No relevant documents found.");
            } else {
                System.out.println("Relevant documents: " + results);
            }

            scanner.close();
        } catch (IOException e) {
            System.out.println("Error processing documents: " + e.getMessage());
        }
    }
}
