package test;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import parser.Python3Lexer;
import parser.Python3Parser;
import java.io.*;
import java.nio.file.*;

public class PythonRepoParser {
    
    static class ErrorListener extends BaseErrorListener {
        public boolean hasErrors = false;
        
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                int line, int charPositionInLine, String msg, RecognitionException e) {
            hasErrors = true;
            System.err.println("  ERROR at line " + line + ":" + charPositionInLine + " - " + msg);
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
            
            ParseTree tree = parser.file_input();
            
            return !errorListener.hasErrors;
            
        } catch (Exception e) {
            System.err.println("  EXCEPTION: " + e.getMessage());
            return false;
        }
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
                    System.out.println("SUCCESS");
                } else {
                    System.out.println("FAILED");
                }
            });
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java PythonRepoParser <path-to-repo>");
            System.exit(1);
        }
        
        String repoPath = args[0];
        System.out.println("Parsing Python repository: " + repoPath);
        System.out.println("=".repeat(50));
        
        try {
            parseRepository(repoPath);
        } catch (IOException e) {
            System.err.println("Error reading repository: " + e.getMessage());
            e.printStackTrace();
        }
    }
}