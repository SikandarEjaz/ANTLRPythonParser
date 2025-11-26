package test;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.TreeViewer;
import parser.Python3Lexer;
import parser.Python3Parser;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class PythonRepoParser {
    
    static class ErrorListener extends BaseErrorListener {
        public boolean hasErrors = false;
        
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                int line, int charPositionInLine, String msg, RecognitionException e) {
            hasErrors = true;
            System.err.println("ERROR at line " + line + ":" + charPositionInLine + " - " + msg);
        }
    }
    
    public static boolean parseFile(String filePath) {
        try {
            CharStream input = CharStreams.fromFileName(filePath);
            Python3Lexer lexer = new Python3Lexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Python3Parser parser = new Python3Parser(tokens);
            
            ErrorListener errorListener = new ErrorListener();
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
                        
            return !errorListener.hasErrors;
            
        } catch (Exception e) {
            System.err.println("EXCEPTION: " + e.getMessage());
            return false;
        }
    }
    
    public static void saveTreeAsImage(String filePath, String outputDir) {
        try {
            CharStream input = CharStreams.fromFileName(filePath);
            Python3Lexer lexer = new Python3Lexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Python3Parser parser = new Python3Parser(tokens);
            
            ErrorListener errorListener = new ErrorListener();
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
            
            ParseTree tree = parser.file_input();
            
            if (errorListener.hasErrors) {
                System.out.println("File has parse errors, tree may be incomplete");
            }
            
            // Create tree viewer
            TreeViewer viewer = new TreeViewer(
                Arrays.asList(parser.getRuleNames()), 
                tree
            );
            viewer.setScale(1.2);
            
            // Get preferred size
            java.awt.Dimension size = viewer.getPreferredSize();
            viewer.setSize(size);
            
            // Create image
            BufferedImage image = new BufferedImage(
                size.width, 
                size.height, 
                BufferedImage.TYPE_INT_RGB
            );
            Graphics2D g2 = image.createGraphics();
            
            // Fill white background
            g2.setColor(java.awt.Color.WHITE);
            g2.fillRect(0, 0, size.width, size.height);
            
            // Paint the tree
            viewer.paint(g2);
            g2.dispose();
            
            // Generate output filename
            File file = new File(filePath);
            String fileName = file.getName().replace(".py", "_tree.png");
            File outputFile = new File(outputDir, fileName);
            
            // Save image
            ImageIO.write(image, "png", outputFile);
            
            System.out.println("Tree saved to: " + outputFile.getName());
            
        } catch (Exception e) {
            System.err.println("Error saving tree: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void parseAndSaveTreesForRepository(String repoPath) throws IOException {
        // Create output directory
        String outputDir = repoPath + "_parse_trees";
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }
        
        System.out.println("Parsing Python repository and saving trees...");
        System.out.println("Repository: " + repoPath);
        System.out.println("Output directory: " + outputDir);
        System.out.println("=".repeat(70));
        
        final int[] fileCount = {0};
        final int[] successCount = {0};
        final int[] failCount = {0};
        
        Files.walk(Paths.get(repoPath))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".py"))
            .forEach(path -> {
                fileCount[0]++;
                String filePath = path.toString();
                System.out.println("\n[" + fileCount[0] + "] Processing: " + filePath);
                
                try {
                    saveTreeAsImage(filePath, outputDir);
                    successCount[0]++;
                } catch (Exception e) {
                    System.err.println("FAILED: " + e.getMessage());
                    failCount[0]++;
                }
            });
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SUMMARY");
        System.out.println("=".repeat(70));
        System.out.println("Total files processed: " + fileCount[0]);
        System.out.println("Successfully saved: " + successCount[0]);
        System.out.println("Failed: " + failCount[0]);
        System.out.println("\n All parse trees saved to: " + outputDir);
    }
    
    public static void parseRepository(String repoPath) throws IOException {
       int successCount = 0;
       int failCount = 0;
        
        Files.walk(Paths.get(repoPath))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".py"))
            .forEach(path -> {
                String filePath = path.toString();
                System.out.println("Parsing: " + filePath);
                
                if (parseFile(filePath)) {
                    System.out.println(" SUCCESS");
                } else {
                    System.out.println(" FAILED");
                }
            });
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java PythonRepoParser <repo-path> [mode]");
            System.out.println("\nModes:");
            System.out.println("  images - Save all parse trees as PNG images (default)");
            System.out.println("  parse  - Just parse files without saving trees");
            System.out.println("\nExample:");
            System.out.println("  java PythonRepoParser C:\\MyPythonRepo");
            System.out.println("  java PythonRepoParser C:\\MyPythonRepo images");
            System.exit(1);
        }
        
        String repoPath = args[0];
        String mode = args.length > 1 ? args[1] : "parse";
        
        try {
            if (mode.equals("images")) {
                parseAndSaveTreesForRepository(repoPath);
            } else if (mode.equals("parse")) {
                System.out.println("Parsing Python repository: " + repoPath);
                System.out.println("=".repeat(50));
                parseRepository(repoPath);
            } else {
                System.err.println("Unknown mode: " + mode);
                System.err.println("Available modes: images, parse");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}