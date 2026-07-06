/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package search.engine;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Document> documents;
        try {
            documents = FileDocumentLoader.loadFromFolder("documents");
            if (documents.isEmpty()) {
                System.out.println("No .txt files found in 'documents' folder — using sample data instead.");
                documents = SampleDocuments.getDocuments();
            } else {
                System.out.println("Loaded " + documents.size() + " document(s) from 'documents' folder.");
            }
        } catch (IOException e) {
            System.out.println("Could not read 'documents' folder — using sample data instead. (" + e.getMessage() + ")");
            documents = SampleDocuments.getDocuments();
        }

        // 2. Build the inverted index (for search + TF-IDF ranking)
        InvertedIndex invertedIndex = new InvertedIndex();
        for (Document doc : documents) {
            invertedIndex.addDocument(doc);
        }

        // 3. Build the trie (for autocomplete) using every word from every document
        Trie trie = new Trie();
        for (Document doc : documents) {
            String[] words = (doc.getTitle() + " " + doc.getContent())
                    .toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", " ")
                    .trim()
                    .split("\\s+");
            for (String word : words) {
                if (!word.isEmpty()) trie.insert(word);
            }
        }

        // 4. Launch the GUI
        SwingUtilities.invokeLater(() -> {
            SearchEngineGUI gui = new SearchEngineGUI(invertedIndex, trie);
            gui.setVisible(true);
        });
    }
}
}
