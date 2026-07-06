/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package search.engine;

import java.util.ArrayList;
import java.util.List;

/*
 * Provides a small in-memory dataset of documents to search over.
 
 */
public class SampleDocuments {

    public static List<Document> getDocuments() {
        List<Document> docs = new ArrayList<>();

        docs.add(new Document(1, "Introduction to Java",
                "Java is a popular object oriented programming language used for building applications."));

        docs.add(new Document(2, "Data Structures Overview",
                "A trie is a tree data structure used for efficient prefix search and autocomplete."));

        docs.add(new Document(3, "Search Engines Explained",
                "A search engine uses an inverted index to map words to documents for fast lookup."));

        docs.add(new Document(4, "TF-IDF Ranking",
                "TF IDF stands for term frequency inverse document frequency and is used to rank search results by relevance."));

        docs.add(new Document(5, "Graph Algorithms",
                "Breadth first search and depth first search are graph traversal algorithms used to explore nodes."));

        docs.add(new Document(6, "Java Collections Framework",
                "Java provides collections such as HashMap, ArrayList, and TreeMap for storing and organizing data."));

        docs.add(new Document(7, "Building a Maze Solver",
                "Recursive backtracking and breadth first search can be used to solve a maze and find the shortest path."));

        docs.add(new Document(8, "Autocomplete Systems",
                "Autocomplete systems use a trie or prefix tree to quickly suggest words as a user types."));

        return docs;
    }
}
