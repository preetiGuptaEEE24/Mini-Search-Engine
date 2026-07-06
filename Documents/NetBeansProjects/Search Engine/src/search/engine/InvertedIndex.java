/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package search.engine;

import java.util.*;


public class InvertedIndex {

    // word -> set of document ids containing it
    private final Map<String, Set<Integer>> index = new HashMap<>();

    // docId -> (word -> count of that word in that doc)
    private final Map<Integer, Map<String, Integer>> termFrequency = new HashMap<>();

    // docId -> total number of words in that doc (needed to normalize TF)
    private final Map<Integer, Integer> docLength = new HashMap<>();

    // docId -> Document object, so we can return titles/content later
    private final Map<Integer, Document> documents = new HashMap<>();

    private int totalDocuments = 0;

    /** Breaks text into lowercase words, stripping punctuation. */
    private List<String> tokenize(String text) {
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] words = cleaned.trim().split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String w : words) {
            if (!w.isEmpty()) tokens.add(w);
        }
        return tokens;
    }

    public void addDocument(Document doc) {
        documents.put(doc.getId(), doc);
        totalDocuments++;

        List<String> words = tokenize(doc.getContent() + " " + doc.getTitle());
        docLength.put(doc.getId(), words.size());

        Map<String, Integer> freqMap = termFrequency.computeIfAbsent(doc.getId(), k -> new HashMap<>());

        for (String word : words) {
            // update term frequency for this doc
            freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);

            // update inverted index: this word now points to this doc
            index.computeIfAbsent(word, k -> new HashSet<>()).add(doc.getId());
        }
    }

    /** Returns the inverse document frequency of a word. */
    private double idf(String word) {
        int docFrequency = index.containsKey(word) ? index.get(word).size() : 0;//in how many doc this word is present
        // +1 smoothing avoids divide-by-zero for words not in any doc
        return Math.log((double) totalDocuments / (1 + docFrequency));
    }

    /** Returns the (normalized) term frequency of a word in a given document. */
    private double tf(String word, int docId) {
        int count = termFrequency.getOrDefault(docId, Collections.emptyMap())
                                  .getOrDefault(word, 0);
        int length = docLength.getOrDefault(docId, 1);
        return (double) count / length;
    }


    public List<SearchResult> search(String query) {
        List<String> queryWords = tokenize(query);

        // Step 1: use the inverted index to find candidate documents (O(1) per word lookup)
        Set<Integer> candidateDocs = new HashSet<>();
        for (String word : queryWords) {
            if (index.containsKey(word)) {
                candidateDocs.addAll(index.get(word));
            }
        }

        // Step 2: score every candidate document using TF-IDF
        Map<Integer, Double> scores = new HashMap<>();
        for (int docId : candidateDocs) {
            double score = 0.0;
            for (String word : queryWords) {
                score += tf(word, docId) * idf(word);
            }
            scores.put(docId, score);
        }

        // Step 3: sort candidates by score, descending
        List<SearchResult> results = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : scores.entrySet()) {
            Document doc = documents.get(entry.getKey());
            results.add(new SearchResult(doc, entry.getValue()));
        }
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return results;
    }

    /** Small container class pairing a document with its relevance score. */
    public static class SearchResult {
        private final Document document;
        private final double score;

        public SearchResult(Document document, double score) {
            this.document = document;
            this.score = score;
        }

        public Document getDocument() { return document; }
        public double getScore() { return score; }
    }
}
