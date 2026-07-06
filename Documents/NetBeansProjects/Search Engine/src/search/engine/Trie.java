/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package search.engine;

import java.util.*;

/*
 * A Trie (prefix tree) used for autocomplete suggestions.
 * Inserting and searching a word of length L takes O(L) time,
 * regardless of how many words are stored in the trie.
 */
public class Trie {

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
    }

    private final TrieNode root = new TrieNode();

    /** Inserts a single word into the trie. */
    public void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }
        current.isEndOfWord = true;
    }

    /**
     * Returns up to `limit` words in the trie that start with the given prefix.
     * Step 1: walk down the trie following the prefix letters -> O(L)
     * Step 2: DFS from that point to collect matching words
     */
    public List<String> getSuggestions(String prefix, int limit) {
        List<String> results = new ArrayList<>();
        TrieNode current = root;

        for (char c : prefix.toCharArray()) {
            current = current.children.get(c);
            if (current == null) {
                return results; // no words with this prefix
            }
        }

        collectWords(current, prefix, results, limit);
        return results;
    }

    private void collectWords(TrieNode node, String prefix, List<String> results, int limit) {
        if (results.size() >= limit) return;

        if (node.isEndOfWord) {
            results.add(prefix);
        }

        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            if (results.size() >= limit) return;
            collectWords(entry.getValue(), prefix + entry.getKey(), results, limit);
        }
    }
}