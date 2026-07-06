package search.engine;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Swing GUI tying together the InvertedIndex (search + ranking)
 * and the Trie (autocomplete suggestions).
 *
 * Visual design notes:
 *   - A dark header bar gives the window a clear identity/branding.
 *   - The search field + button are styled as one rounded "pill" control.
 *   - Results render as white cards with soft borders on a light gray canvas,
 *     similar to a real search engine results page.
 */
public class SearchEngineGUI extends JFrame {

    // ---- Color palette ----
    private static final Color COLOR_HEADER      = new Color(0x1F2A44); // deep navy
    private static final Color COLOR_ACCENT      = new Color(0x3B6EF6); // primary blue
    private static final Color COLOR_BACKGROUND  = new Color(0xF3F4F6); // light gray canvas
    private static final Color COLOR_CARD        = Color.WHITE;
    private static final Color COLOR_BORDER      = new Color(0xE2E4E9);
    private static final Color COLOR_TEXT_MUTED  = new Color(0x6B7280);
    private static final Color COLOR_HIGHLIGHT   = new Color(0xFFF3A3);

    private static final String FONT_FAMILY = "Segoe UI";

    private final InvertedIndex invertedIndex;
    private final Trie trie;

    private final JTextField searchField;
    private final JList<String> suggestionList;
    private final DefaultListModel<String> suggestionModel;
    private final JPopupMenu suggestionPopup;

    private final JPanel resultsPanel;
    private final JLabel resultsCountLabel;

    public SearchEngineGUI(InvertedIndex invertedIndex, Trie trie) {
        this.invertedIndex = invertedIndex;
        this.trie = trie;

        setTitle("Mini Search Engine");
        setSize(780, 620);
        setMinimumSize(new Dimension(560, 420));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BACKGROUND);

        // ================= Header =================
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel titleLabel = new JLabel("Mini Search Engine");
        titleLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Inverted Index  \u2022  TF-IDF Ranking  \u2022  Trie Autocomplete");
        subtitleLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(0xC7CDE0));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // ================= Search bar =================
        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(COLOR_BACKGROUND);
        searchWrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        JPanel searchBar = new JPanel(new BorderLayout(0, 0));
        searchBar.setBackground(COLOR_CARD);
        searchBar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        searchField = new JTextField();
        searchField.setFont(new Font(FONT_FAMILY, Font.PLAIN, 16));
        searchField.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        searchField.setBackground(COLOR_CARD);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBackground(COLOR_ACCENT);
        searchButton.setFocusPainted(false);
        searchButton.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchButton, BorderLayout.EAST);
        searchWrapper.add(searchBar, BorderLayout.CENTER);

        // Combine header + search bar into one north container
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(headerPanel, BorderLayout.NORTH);
        northContainer.add(searchWrapper, BorderLayout.SOUTH);
        add(northContainer, BorderLayout.NORTH);

        // ================= Autocomplete popup =================
        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);
        suggestionList.setFont(new Font(FONT_FAMILY, Font.PLAIN, 14));
        suggestionList.setSelectionBackground(new Color(0xE8EDFF));
        suggestionList.setSelectionForeground(Color.BLACK);
        suggestionList.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        suggestionPopup = new JPopupMenu();
        suggestionPopup.setFocusable(false);
        suggestionPopup.setBorder(new LineBorder(COLOR_BORDER, 1));
        suggestionPopup.add(new JScrollPane(suggestionList));

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selected = suggestionList.getSelectedValue();
                if (selected != null) {
                    searchField.setText(selected);
                    suggestionPopup.setVisible(false);
                    performSearch();
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateSuggestions();
            }
        });

        // ================= Results area =================
        resultsCountLabel = new JLabel(" ");
        resultsCountLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
        resultsCountLabel.setForeground(COLOR_TEXT_MUTED);
        resultsCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 24, 8, 24));

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(COLOR_BACKGROUND);
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));

        showEmptyState("Start typing above to search your indexed documents.");

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(COLOR_BACKGROUND);
        centerContainer.add(resultsCountLabel, BorderLayout.NORTH);
        centerContainer.add(resultsPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(centerContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(COLOR_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Re-wrap result cards whenever the window is resized, so text
        // reflows correctly instead of staying wrapped at the old width.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                fixResultCardWrapping();
            }
        });
    }

    /** Shows a centered, muted placeholder message where results normally go. */
    private void showEmptyState(String message) {
        resultsPanel.removeAll();
        JLabel label = new JLabel(message);
        label.setFont(new Font(FONT_FAMILY, Font.ITALIC, 14));
        label.setForeground(COLOR_TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(40, 4, 4, 4));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsPanel.add(label);
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    /** Called on every keystroke to refresh the autocomplete dropdown. */
    private void updateSuggestions() {
        String text = searchField.getText().trim().toLowerCase();

        String[] parts = text.split("\\s+");
        String lastWord = parts.length > 0 ? parts[parts.length - 1] : "";

        if (lastWord.isEmpty()) {
            suggestionPopup.setVisible(false);
            return;
        }

        List<String> suggestions = trie.getSuggestions(lastWord, 8);
        suggestionModel.clear();

        if (suggestions.isEmpty()) {
            suggestionPopup.setVisible(false);
            return;
        }

        for (String s : suggestions) {
            suggestionModel.addElement(s);
        }

        suggestionPopup.setPopupSize(searchField.getWidth(), Math.min(190, suggestions.size() * 26 + 10));
        suggestionPopup.show(searchField, 0, searchField.getHeight() + 6);
        searchField.requestFocus();
    }

    /** Runs a search using the inverted index and displays ranked, highlighted results. */
    private void performSearch() {
        suggestionPopup.setVisible(false);
        String query = searchField.getText().trim();

        resultsPanel.removeAll();

        if (query.isEmpty()) {
            resultsCountLabel.setText(" ");
            showEmptyState("Start typing above to search your indexed documents.");
            return;
        }

        List<InvertedIndex.SearchResult> results = invertedIndex.search(query);
        String[] queryWords = query.toLowerCase().split("\\s+");

        if (results.isEmpty()) {
            resultsCountLabel.setText("No results for \"" + query + "\"");
            showEmptyState("No documents matched your search. Try a different word.");
        } else {
            resultsCountLabel.setText(results.size() + " result" + (results.size() == 1 ? "" : "s")
                    + " for \"" + query + "\"");
            for (InvertedIndex.SearchResult result : results) {
                resultsPanel.add(buildResultCard(result, queryWords));
                resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();

        // Force each HTML result card to re-wrap its text at the ACTUAL
        // available width, since JEditorPane doesn't know its real width
        // until after it's been placed in the layout.
        SwingUtilities.invokeLater(this::fixResultCardWrapping);
    }

    /**
     * Re-measures every result card at the panel's real width so long
     * snippets wrap onto new lines instead of overflowing past the window.
     */
    private void fixResultCardWrapping() {
        int availableWidth = resultsPanel.getWidth();
        if (availableWidth <= 0) return;

        for (Component c : resultsPanel.getComponents()) {
            if (c instanceof JEditorPane pane) {
                // Ask the HTML view to lay out at this exact width...
                pane.setSize(new Dimension(availableWidth, Short.MAX_VALUE));
                // ...then read back the height it actually needs at that width.
                Dimension preferred = pane.getPreferredSize();
                Dimension fixed = new Dimension(availableWidth, preferred.height);
                pane.setPreferredSize(fixed);
                pane.setMaximumSize(fixed);
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    /**
     * Builds one HTML-rendered "card" for a single search result: title,
     * score, and a content snippet with every matched query word highlighted.
     */
    private JEditorPane buildResultCard(InvertedIndex.SearchResult result, String[] queryWords) {
        Document doc = result.getDocument();
        String snippet = buildHighlightedSnippet(doc.getContent(), queryWords);
        String highlightHex = String.format("#%06x", COLOR_HIGHLIGHT.getRGB() & 0xFFFFFF);

        String html = "<html><body style='font-family:" + FONT_FAMILY + ",sans-serif; margin:0;'>"
                + "<div style='font-size:15px; font-weight:bold; color:#1F2A44;'>"
                + doc.getTitle()
                + "</div>"
                + "<div style='font-size:11px; color:#6B7280; margin-bottom:6px;'>"
                + "Document #" + doc.getId()
                + " &nbsp;\u2022&nbsp; relevance score " + String.format("%.4f", result.getScore())
                + "</div>"
                + "<div style='font-size:13px; color:#374151; line-height:1.5;'>"
                + snippet.replaceAll("YELLOWHEX", highlightHex)
                + "</div>"
                + "</body></html>";

        JEditorPane pane = new JEditorPane("text/html", html);
        pane.setEditable(false);
        pane.setOpaque(true);
        pane.setBackground(COLOR_CARD);
        pane.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pane;
    }

    /**
     * Escapes HTML in the raw content, trims it to a short snippet, and wraps
     * every case-insensitive whole-word match of a query word in a highlighted
     * <span> so it stands out visually — same idea as Google's bolded terms.
     */
    private String buildHighlightedSnippet(String content, String[] queryWords) {
        String escaped = content
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        String snippet = escaped.length() > 220 ? escaped.substring(0, 220) + "..." : escaped;

        StringBuilder patternBuilder = new StringBuilder();
        for (String word : queryWords) {
            if (word.isBlank()) continue;
            if (patternBuilder.length() > 0) patternBuilder.append("|");
            patternBuilder.append(Pattern.quote(word));
        }
        if (patternBuilder.length() == 0) return snippet;

        Pattern pattern = Pattern.compile("\\b(" + patternBuilder + ")\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(snippet);

        StringBuilder highlighted = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            highlighted.append(snippet, lastEnd, matcher.start());
            highlighted.append("<span style='background-color:YELLOWHEX; padding:1px 2px; border-radius:2px;'>")
                       .append(matcher.group())
                       .append("</span>");
            lastEnd = matcher.end();
        }
        highlighted.append(snippet.substring(lastEnd));

        return highlighted.toString();
    }
}