import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.Arrays;
public class CodeEditor extends JFrame implements ActionListener {
    private JTabbedPane tabbedPane;
    private JFileChooser fileChooser;
    private boolean isLightMode = true;
    private JTextPane textPane;
    private JButton analyzeButton;
    private JTextArea consoleOutput;
    //initializes the editor window, sets its layout, title, size, and close operation.
    //a dialog refers to a temporary window that is used to communicate information to the user or to gather input from them
    public CodeEditor() {
       setLayout(new BorderLayout());
        setTitle("Prem's Code Editor");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    
        tabbedPane = new JTabbedPane();
        JMenuBar menuBar = new JMenuBar();
        createFileMenu(menuBar);
        createViewMenu(menuBar);
        createEditMenu(menuBar);
       
        setJMenuBar(menuBar);
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Code Files", "java", "py", "c", "cpp"));

        add(tabbedPane, BorderLayout.CENTER); 
        
        //JTextArea codeTextArea = new JTextArea();
        // Create a panel for buttons (Run and Analyze)

        JPanel buttonPanel = new JPanel();
        createRunButton(buttonPanel);   // Add the Run button to the panel
        createAnalyzeButton(buttonPanel);   // Add the Analyze button to the panel
        add(buttonPanel, BorderLayout.NORTH); 
        // Add the button panel at the top of the window
    
        // Create the console output area
        createConsoleOutputArea();   // Add console output at the bottom
        addRenameFunctionalityToTab();
        setVisible(true);
        applyTheme();
        
    }
    private void addRenameFunctionalityToTab() {
        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (tabIndex != -1) {
                        String currentTitle = tabbedPane.getTitleAt(tabIndex);
                        String newTitle = JOptionPane.showInputDialog(
                            CodeEditor.this, "Rename file:", currentTitle
                        );
                        if (newTitle != null && !newTitle.trim().isEmpty()) {
                            tabbedPane.setTitleAt(tabIndex, newTitle);
                        }
                    }
                }
            }
        });
    }
      private void createFileMenu(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");//used to create a menu items together in a dropdown menu
        //file an argument to the constructor is the label(what user see on gui) for the menu.
        JMenuItem openMenuItem = new JMenuItem("Open");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        JMenuItem newMenuItem = new JMenuItem("New");
        JMenuItem closeMenuItem = new JMenuItem("Close");
       //to handle user interactions with a menu item
        openMenuItem.addActionListener(e -> openFile());
        //lambda expression representing the implementation of the actionPerformed method of the ActionListener interface.
       //e parameter represents the action event generated when the menu item is clicked,
        saveMenuItem.addActionListener(e -> saveCurrentTab());
        saveAsMenuItem.addActionListener(e -> saveCurrentTabContentAs());
        newMenuItem.addActionListener(e -> createNewTab());
        closeMenuItem.addActionListener(e -> closeCurrentTab());

        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(closeMenuItem);
        menuBar.add(fileMenu);
    }

    private void createViewMenu(JMenuBar menuBar) {
        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem lightModeMenuItem = new JCheckBoxMenuItem("Light Mode", true);
        JCheckBoxMenuItem darkModeMenuItem = new JCheckBoxMenuItem("Dark Mode");

        lightModeMenuItem.addActionListener(e -> {
            isLightMode = true;
            applyTheme();
        });
        darkModeMenuItem.addActionListener(e -> {
            isLightMode = false;
            applyTheme();
        });

        viewMenu.add(lightModeMenuItem);
        viewMenu.add(darkModeMenuItem);
        menuBar.add(viewMenu);
    }

    private void createEditMenu(JMenuBar menuBar) {
        JMenu editMenu = new JMenu("Edit");
        JMenuItem increaseFontSizeMenuItem = new JMenuItem("Increase Font Size");
        JMenuItem decreaseFontSizeMenuItem = new JMenuItem("Decrease Font Size");

        increaseFontSizeMenuItem.addActionListener(e -> adjustFontSize(2));
        decreaseFontSizeMenuItem.addActionListener(e -> adjustFontSize(-2));

        editMenu.add(increaseFontSizeMenuItem);
        editMenu.add(decreaseFontSizeMenuItem);
        menuBar.add(editMenu);
    }

    // Modify this method to take a panel where the button will be added
private void createAnalyzeButton(JPanel buttonPanel) {
    analyzeButton = new JButton("Analyze");
    analyzeButton.addActionListener(e -> analyzeCurrentTab());
    buttonPanel.add(analyzeButton);
}


    private void analyzeCurrentTab() {
        int tabIndex = tabbedPane.getSelectedIndex();
        if (tabIndex != -1) {
            JTextPane currentTextPane = getTextPaneAt(tabIndex);
            String code = currentTextPane.getText();
            List<String> codeLines = Arrays.asList(code.split("\n"));
            analyzeCode(codeLines);
        }
    }
    
    private void createRunButton(JPanel buttonPanel) {
        JButton runButton = new JButton("Run");
        runButton.addActionListener(e -> runCurrentCode());
        buttonPanel.add(runButton);
    }
// Existing methods for displaying output and errors...

private void runCurrentCode() {
    int tabIndex = tabbedPane.getSelectedIndex();//get selected tab
    if (tabIndex != -1) {
        JTextPane currentTextPane = getTextPaneAt(tabIndex);
        String code = currentTextPane.getText();//get info of textpane from selected tab
        String fileName = "Main"; // Temporary file name

        String selectedFileName = tabbedPane.getTitleAt(tabIndex);
        String fileExtension = selectedFileName.substring(selectedFileName.lastIndexOf(".") + 1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();//to capture output and error messages generated by the code execution.
        PrintStream printStream = new PrintStream(outputStream);
        PrintStream originalOut = System.out;//to collect console output generated by the running code.

        try {
            System.setOut(printStream);

            File tempFile = new File(fileName + "." + fileExtension);
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(code);
            }

            switch (fileExtension) {
                case "java":
                    compileAndRunJava(tempFile);
                    break;
                case "py":
                    runPython(tempFile);
                    break;
                case "c":
                case "cpp":
                    compileAndRunCOrCPP(tempFile, fileExtension);
                    break;
                default:
                    consoleOutput.setText("Unsupported file type.");
                    break;
            }
        } catch (IOException | InterruptedException ex) {
            consoleOutput.setText("Error: " + ex.getMessage());
        } finally {
            System.setOut(originalOut);
            consoleOutput.append(outputStream.toString());
        }
    }
}

private void compileAndRunJava(File tempFile) throws IOException, InterruptedException {
    // Compile the Java file
    ProcessBuilder compileProcessBuilder = new ProcessBuilder("javac", tempFile.getPath());
    Process compileProcess = compileProcessBuilder.start();
    compileProcess.waitFor();

    // Check for compilation errors
    if (compileProcess.exitValue() != 0) {
        displayErrors(compileProcess.getErrorStream()); // Handle compilation errors
        return;
    }

    // Run the compiled Java program
    String className = tempFile.getName().replace(".java", "");
    ProcessBuilder runProcessBuilder = new ProcessBuilder("java", className);
    Process runProcess = runProcessBuilder.start();

    // Capture and display output in the console area
    displayOutput(runProcess.getInputStream());
    displayErrors(runProcess.getErrorStream());
}

private void runPython(File tempFile) throws IOException, InterruptedException {
    ProcessBuilder runProcessBuilder = new ProcessBuilder("python", tempFile.getPath());
    Process runProcess = runProcessBuilder.start();

    // Capture and display output in the console area
    displayOutput(runProcess.getInputStream());
    displayErrors(runProcess.getErrorStream());
}

private void compileAndRunCOrCPP(File tempFile, String fileExtension) throws IOException, InterruptedException {
    String outputFileName = "tempOutput";
    // Compile the C or C++ file
    ProcessBuilder compileProcessBuilder = new ProcessBuilder("gcc", tempFile.getPath(), "-o", outputFileName);
    if (fileExtension.equals("cpp")) {
        compileProcessBuilder.command("g++", tempFile.getPath(), "-o", outputFileName);
    }
    Process compileProcess = compileProcessBuilder.start();
    compileProcess.waitFor();

    // Check for compilation errors
    if (compileProcess.exitValue() != 0) {
        displayErrors(compileProcess.getErrorStream()); // Handle compilation errors
        return;
    }

    // Run the compiled program
    ProcessBuilder runProcessBuilder = new ProcessBuilder("./" + outputFileName);
    Process runProcess = runProcessBuilder.start();

    // Capture and display output in the console area
    displayOutput(runProcess.getInputStream());
    displayErrors(runProcess.getErrorStream());
}

private void displayOutput(InputStream inputStream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder output = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
    }
    // Append output to console area
    consoleOutput.append(output.toString());  
}

private void displayErrors(InputStream errorStream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
    StringBuilder errors = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
        errors.append(line).append("\n");
    }
    // Append errors to console area
    consoleOutput.append(errors.toString());  
}


private void createConsoleOutputArea() {
    consoleOutput = new JTextArea(10, 50); // Adjust size as needed
    consoleOutput.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(consoleOutput);
    add(scrollPane, BorderLayout.SOUTH);
}
private void analyzeCode(List<String> codeLines) {
        int loopCount = 0;
        int recursiveCallCount = 0;
        int dataStructureCount = 0;

        int maxLoopDepth = 0;
        int currentLoopDepth = 0;

        for (String line : codeLines) {
            if (containsAnyKeyword(line, LOOP_KEYWORDS)) {
                loopCount++;
                currentLoopDepth++;
                maxLoopDepth = Math.max(maxLoopDepth, currentLoopDepth);
            } else if (line.contains("}")) {
                currentLoopDepth = Math.max(0, currentLoopDepth - 1);
            }
            if (containsAnyKeyword(line, RECURSION_KEYWORDS)) {
                recursiveCallCount++;
            }
            if (containsAnyKeyword(line, DATA_STRUCTURES)) {
                dataStructureCount++;
            }
        }

        String timeComplexity = estimateTimeComplexity(maxLoopDepth, recursiveCallCount, codeLines);
        String spaceComplexity = estimateSpaceComplexity(dataStructureCount);
        JOptionPane.showMessageDialog(this, 
                "Estimated Time Complexity: " + timeComplexity + 
                "\nEstimated Space Complexity: " + spaceComplexity, 
                "Complexity Analysis", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean containsAnyKeyword(String line, String[] keywords) {
        for (String keyword : keywords) {
            if (line.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String estimateTimeComplexity(int maxLoopDepth, int recursiveCallCount, List<String> codeLines) {
        if (recursiveCallCount > 0) {
            if (detectDivideAndConquerPattern(codeLines)) {
                return "O(n log n)";
            }
            if (maxLoopDepth > 0) {
                return "O(n^" + maxLoopDepth + " * recursion complexity)";
            }
            return "O(2^n) or O(n!)";
        } else if (maxLoopDepth == 1) {
            if (detectBinarySearchPattern(codeLines)) {
                return "O(log n)";
            }
            return "O(n)";
        } else if (maxLoopDepth > 1) {
            return "O(n^" + maxLoopDepth + ")";
        }
        return "O(1)";
    }

    private String estimateSpaceComplexity(int dataStructureCount) {
        if (dataStructureCount > 0) {
            return "O(n)"; // Space grows with the number of data structures
        }
        return "O(1)"; // No data structures used
    }

    private boolean detectDivideAndConquerPattern(List<String> codeLines) {
        for (String line : codeLines) {
            if (containsAnyKeyword(line, DIVIDE_AND_CONQUER_KEYWORDS)) {
                return true;
            }
        }
        return false;
    }

    private boolean detectBinarySearchPattern(List<String> codeLines) {
        for (String line : codeLines) {
            if (containsAnyKeyword(line, BINARY_SEARCH_KEYWORDS)) {
                return true;
            }
        }
        return false;
    }

    // Constants from CodeAnalyzer
    private static final String[] LOOP_KEYWORDS = { "for", "while", "do" };
    private static final String[] RECURSION_KEYWORDS = { "return", "call" };
    private static final String[] DIVIDE_AND_CONQUER_KEYWORDS = { "merge", "quick", "divide" };
    private static final String[] DATA_STRUCTURES = { "int[]", "ArrayList", "LinkedList", "HashMap" };
    private static final String[] BINARY_SEARCH_KEYWORDS = { "mid", "low", "high" };

    // Existing functionality for file handling and editor UI

    private void createNewTab() {
        textPane = new JTextPane();//component serves as a versatile text editing area that allows for more than just plain text input
        JScrollPane scrollPane = new JScrollPane(textPane);//wrap JTextPane in this
        tabbedPane.addTab("Untitled", scrollPane);//// Add the scrollable pane to the tabbed pane with a default title
    }
   //pane refers to a component that acts as a container for other components.(area where other components can be oraganized)
   //JScrollPane: A specialized pane that adds scrolling capabilities to another component, such as a JTextArea or JTextPane. This is useful when the content exceeds the visible area.
//JTabbedPane: A pane that allows for multiple tabs, where each tab can contain different components, allowing users to switch between different views or sets of components.

   private void openFile() {
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textPane = new JTextPane();
            JScrollPane scrollPane = new JScrollPane(textPane);
            tabbedPane.addTab(selectedFile.getName(), scrollPane);
            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                StyledDocument doc = textPane.getStyledDocument();
                String line;
                while ((line = reader.readLine()) != null) {
                     doc.insertString(doc.getLength(), line + "\n", null);
                }
                reader.close();
                applySyntaxHighlighting(selectedFile.getName());
            } catch (IOException | BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }
    private void saveCurrentTab() {
        int tabIndex = tabbedPane.getSelectedIndex();
        if (tabIndex != -1) {
            JTextPane currentTextPane = getTextPaneAt(tabIndex);
            String content = currentTextPane.getText();
            int returnVal = fileChooser.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    FileWriter writer = new FileWriter(selectedFile);
                    writer.write(content);
                    writer.close();
                    tabbedPane.setTitleAt(tabIndex, selectedFile.getName());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void saveCurrentTabContentAs() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex != -1) {
            JTextPane textPane = getTextPaneAt(selectedIndex);
            String content = textPane.getText();
            int returnVal = fileChooser.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try (FileWriter writer = new FileWriter(selectedFile)) {
                    writer.write(content);
                    tabbedPane.setTitleAt(selectedIndex, selectedFile.getName());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void closeCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex != -1) {
            tabbedPane.removeTabAt(selectedIndex);
        }
    }

    private JTextPane getTextPaneAt(int tabIndex) {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(tabIndex);
        return (JTextPane) scrollPane.getViewport().getView();
    }

    private void adjustFontSize(int increment) {
        /*Font currentFont = textPane.getFont();
        Font newFont = new Font(currentFont.getName(), currentFont.getStyle(), currentFont.getSize() + increment);
        textPane.setFont(newFont);*/
        Font currentFont = textPane.getFont();
int newSize = currentFont.getSize() + increment;
if (newSize > 0) {
    Font newFont = new Font(currentFont.getFontName(), currentFont.getStyle(), newSize);
    textPane.setFont(newFont);
}

    }

    private void applyTheme() {
        if (isLightMode) {
            textPane.setBackground(Color.WHITE);
            textPane.setForeground(Color.BLACK);
        } else {
            textPane.setBackground(Color.DARK_GRAY);
            textPane.setForeground(Color.WHITE);
        }
    }

    private void applySyntaxHighlighting(String fileName) {
        // Retrieve the existing document
        StyledDocument doc = (StyledDocument) textPane.getDocument();
        
        // Clear any previous styles
        clearDocumentStyles(doc);
    
        // Get the code text from the text pane
        String code = textPane.getText();
    
        // Check file extension and apply appropriate highlighting
        if (fileName.endsWith(".java")) {
            applyJavaSyntaxHighlighting(doc, code);
        } else if (fileName.endsWith(".py")) {
            applyPythonSyntaxHighlighting(doc, code);
        } else if (fileName.endsWith(".cpp") || fileName.endsWith(".c")) {
            applyCppSyntaxHighlighting(doc, code);
        } else if (fileName.endsWith(".js")) {
            applyJavaScriptSyntaxHighlighting(doc, code);
        }
    } 
    
    
    private void clearDocumentStyles(StyledDocument doc) {
        // Clear existing styles (optional, based on your requirements)
        int length = doc.getLength();
        SimpleAttributeSet defaultStyle = new SimpleAttributeSet();
        doc.setCharacterAttributes(0, length, defaultStyle, true);
    }
    
    private void applyJavaSyntaxHighlighting(StyledDocument doc, String code) {
        // Styles for Java
        SimpleAttributeSet keywordStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(keywordStyle, Color.BLUE);
    
        SimpleAttributeSet commentStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(commentStyle, Color.GRAY);
    
        SimpleAttributeSet stringStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(stringStyle, Color.GREEN);
    
        String[] keywords = { "public", "private", "class", "void", "int", "String", "for", "while", "if", "else", "return", "new", "import", "package", "static", "protected" };
    
        // Highlight keywords
        highlightKeywords(code, doc, keywords, keywordStyle);
        highlightJavaComments(code, doc, commentStyle);
        highlightJavaStrings(code, doc, stringStyle);
    }
    
    private void applyPythonSyntaxHighlighting(StyledDocument doc, String code) {
        // Styles for Python
        SimpleAttributeSet keywordStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(keywordStyle, Color.BLUE);
    
        SimpleAttributeSet commentStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(commentStyle, Color.GRAY);
    
        SimpleAttributeSet stringStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(stringStyle, Color.GREEN);
    
        String[] keywords = { "def", "class", "if", "elif", "else", "for", "while", "return", "import", "from", "as", "try", "except" };
    
        // Highlight keywords
        highlightKeywords(code, doc, keywords, keywordStyle);
        highlightPythonComments(code, doc, commentStyle);
        highlightPythonStrings(code, doc, stringStyle);
    }
    
    private void applyCppSyntaxHighlighting(StyledDocument doc, String code) {
        // Styles for C++
        SimpleAttributeSet keywordStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(keywordStyle, Color.BLUE);
    
        SimpleAttributeSet commentStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(commentStyle, Color.GRAY);
    
        SimpleAttributeSet stringStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(stringStyle, Color.GREEN);
    
        String[] keywords = { "class", "public", "private", "protected", "void", "int", "double", "float", "if", "else", "for", "while", "return", "new", "namespace", "using" };
    
        // Highlight keywords
        highlightKeywords(code, doc, keywords, keywordStyle);
        highlightCppComments(code, doc, commentStyle);
        highlightCppStrings(code, doc, stringStyle);
    }
    
    private void applyJavaScriptSyntaxHighlighting(StyledDocument doc, String code) {
        // Styles for JavaScript
        SimpleAttributeSet keywordStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(keywordStyle, Color.BLUE);
    
        SimpleAttributeSet commentStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(commentStyle, Color.GRAY);
    
        SimpleAttributeSet stringStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(stringStyle, Color.GREEN);
    
        String[] keywords = { "function", "var", "let", "const", "if", "else", "for", "while", "return", "new", "import", "from", "class", "try", "catch" };
    
        // Highlight keywords
        highlightKeywords(code, doc, keywords, keywordStyle);
        highlightJavaScriptComments(code, doc, commentStyle);
        highlightJavaScriptStrings(code, doc, stringStyle);
    }
    
    private void highlightKeywords(String code, StyledDocument doc, String[] keywords, SimpleAttributeSet keywordStyle) {
        for (String keyword : keywords) {
            int index = code.indexOf(keyword);
            while (index >= 0) {
                doc.setCharacterAttributes(index, keyword.length(), keywordStyle, false);
                index = code.indexOf(keyword, index + keyword.length());
            }
        }
    }
    
    private void highlightJavaComments(String code, StyledDocument doc, SimpleAttributeSet commentStyle) {
        int index = code.indexOf("//");
        while (index >= 0) {
            int end = code.indexOf("\n", index);
            if (end == -1) end = code.length();
            doc.setCharacterAttributes(index, end - index, commentStyle, false);
            index = code.indexOf("//", end);
        }
    
        index = code.indexOf("/*");
        while (index >= 0) {
            int end = code.indexOf("*/", index);
            if (end == -1) end = code.length();
            doc.setCharacterAttributes(index, end - index + 2, commentStyle, false);
            index = code.indexOf("/*", end);
        }
    }
    
    private void highlightPythonComments(String code, StyledDocument doc, SimpleAttributeSet commentStyle) {
        int index = code.indexOf("#");
        while (index >= 0) {
            int end = code.indexOf("\n", index);
            if (end == -1) end = code.length();
            doc.setCharacterAttributes(index, end - index, commentStyle, false);
            index = code.indexOf("#", end);
        }
    }
    
    private void highlightCppComments(String code, StyledDocument doc, SimpleAttributeSet commentStyle) {
        int index = code.indexOf("//");
        while (index >= 0) {
            int end = code.indexOf("\n", index);
            if (end == -1) end = code.length();
            doc.setCharacterAttributes(index, end - index, commentStyle, false);
            index = code.indexOf("//", end);
        }
    
        index = code.indexOf("/*");
        while (index >= 0) {
            int end = code.indexOf("*/", index);
            if (end == -1) end = code.length();
            doc.setCharacterAttributes(index, end - index + 2, commentStyle, false);
            index = code.indexOf("/*", end);
        }
    }
    
    private void highlightJavaStrings(String code, StyledDocument doc, SimpleAttributeSet stringStyle) {
        int index = code.indexOf("\"");
        while (index >= 0) {
            int end = code.indexOf("\"", index + 1);
            if (end == -1) break;
            doc.setCharacterAttributes(index, end - index + 1, stringStyle, false);
            index = code.indexOf("\"", end + 1);
        }
    }
    
    private void highlightPythonStrings(String code, StyledDocument doc, SimpleAttributeSet stringStyle) {
        int index = code.indexOf("\"");
        while (index >= 0) {
            int end = code.indexOf("\"", index + 1);
            if (end == -1) break;
            doc.setCharacterAttributes(index, end - index + 1, stringStyle, false);
            index = code.indexOf("\"", end + 1);
        }
    }
    
    private void highlightCppStrings(String code, StyledDocument doc, SimpleAttributeSet stringStyle) {
        int index = code.indexOf("\"");
        while (index >= 0) {
            int end = code.indexOf("\"", index + 1);
            if (end == -1) break;
            doc.setCharacterAttributes(index, end - index + 1, stringStyle, false);
            index = code.indexOf("\"", end + 1);
        }
    }
    
    private void highlightJavaScriptStrings(String code, StyledDocument doc, SimpleAttributeSet stringStyle) {
        int index = code.indexOf("\"");
        while (index >= 0) {
            int end = code.indexOf("\"", index + 1);
            if (end == -1) break;
            doc.setCharacterAttributes(index, end - index + 1, stringStyle, false);
            index = code.indexOf("\"", end + 1);
        }
    }
    
    private void highlightJavaScriptComments(String code, StyledDocument doc, SimpleAttributeSet commentStyle) {
        int index = code.indexOf("//");
        while (index >= 0) {
            int end = code.indexOf("\n", index);
            if (end == -1) end = code.length();
            doc.setCharacterAttributes(index, end - index, commentStyle, false);
            index = code.indexOf("//", end);
        }
    
        index = code.indexOf("/*");
        while (index >= 0) {
            int end = code.indexOf("*/", index);
            if (end == -1) end = code.length();
            doc.setCharacterAttributes(index, end - index + 2, commentStyle, false);
            index = code.indexOf("/*", end);
        }
    }
    public static void main(String[] args) {
        //SwingUtilities.invokeLater(CodeEditor::new);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CodeEditor editor = new CodeEditor();
                editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                editor.setSize(800, 600);
                editor.setVisible(true);
            }
        });
        
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        // Add any action handling if necessary
    }
}
