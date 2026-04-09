import static org.junit.jupiter.api.Assertions.*;

import compiler.*;
import compiler.exc.IncomplException;
import compiler.exc.TypeException;
import compiler.lib.FOOLlib;
import compiler.lib.Node;
import compiler.lib.TypeNode;
import java.io.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import svm.ExecuteVM;
import svm.SVMLexer;
import svm.SVMParser;

class ProgramsTest {
    public static CharStream fromFile(String fileName) {
        AtomicReference<CharStream> chars = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            chars.set(CharStreams.fromFileName(fileName));
        });
        return chars.get();
    }

    public static int getScanningErrors(final CharStream input) {
        FOOLLexer lexer = new FOOLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FOOLParser parser = new FOOLParser(tokens);
        parser.prog();
        return lexer.lexicalErrors;
    }

    public static int getParsingErrors(final CharStream input) {
        FOOLLexer lexer = new FOOLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FOOLParser parser = new FOOLParser(tokens);
        parser.prog();
        assertEquals(0, lexer.lexicalErrors);
        return parser.getNumberOfSyntaxErrors();
    }

    public static int getSyntaxErrors(final CharStream input) {
        FOOLLexer lexer = new FOOLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FOOLParser parser = new FOOLParser(tokens);
        ParseTree st = parser.prog();
        assertEquals(0, lexer.lexicalErrors);
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        ASTGenerationSTVisitor visitor = new ASTGenerationSTVisitor();
        Node ast = visitor.visit(st);
        SymbolTableASTVisitor symtableVisitor = new SymbolTableASTVisitor();
        symtableVisitor.visit(ast);
        return symtableVisitor.stErrors;
    }

    public static int getTypeErrors(final CharStream input) {
        // Static fields must be always reset
        FOOLlib.reset();

        FOOLLexer lexer = new FOOLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FOOLParser parser = new FOOLParser(tokens);
        ParseTree st = parser.prog();
        assertEquals(0, lexer.lexicalErrors);
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        ASTGenerationSTVisitor visitor = new ASTGenerationSTVisitor();
        Node ast = visitor.visit(st);
        SymbolTableASTVisitor symtableVisitor = new SymbolTableASTVisitor();
        symtableVisitor.visit(ast);
        assertEquals(0, symtableVisitor.stErrors);
        try {
            TypeCheckEASTVisitor typeCheckVisitor = new TypeCheckEASTVisitor();
            TypeNode mainType = typeCheckVisitor.visit(ast);
            // Checks that return type of FOOL language is int or subtype
            assertTrue(TypeRels.isSubtype(mainType, new AST.IntTypeNode()));
        } catch (IncomplException e) {
            System.err.println(
                    "Could not determine main program expression type due to errors detected before type checking.");
        } catch (TypeException e) {
            System.err.println("Type checking error in main program expression: " + e.text);
        }
        return FOOLlib.typeErrors;
    }

    public static void testResult(String fileName, final CharStream input, String result) {
        // Static fields must be always reset
        FOOLlib.reset();

        FOOLLexer lexer = new FOOLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FOOLParser parser = new FOOLParser(tokens);

        // Generating ST via lexer and parser
        ParseTree st = parser.prog();
        assertEquals(0, lexer.lexicalErrors);
        assertEquals(0, parser.getNumberOfSyntaxErrors());

        // Generating AST
        ASTGenerationSTVisitor visitor = new ASTGenerationSTVisitor();
        Node ast = visitor.visit(st);

        // Enriching AST via symbol table
        SymbolTableASTVisitor symtableVisitor = new SymbolTableASTVisitor();
        symtableVisitor.visit(ast);
        assertEquals(0, symtableVisitor.stErrors);

        // Checking Types
        try {
            TypeCheckEASTVisitor typeCheckVisitor = new TypeCheckEASTVisitor();
            TypeNode mainType = typeCheckVisitor.visit(ast);
            // Checks that return type of FOOL language is int or subtype
            // TODO: type check could be generalized
            assertTrue(TypeRels.isSubtype(mainType, new AST.IntTypeNode()));
        } catch (IncomplException e) {
            System.err.println(
                    "Could not determine main program expression type due to errors detected before type checking.");
        } catch (TypeException e) {
            System.err.println("Type checking error in main program expression: " + e.text);
        }
        assertEquals(0, FOOLlib.typeErrors);

        // lexer.lexicalErrors + parser.getNumberOfSyntaxErrors() + symtableVisitor.stErrors + FOOLlib.typeErrors = 0

        // Generating code.
        String code = new CodeGenerationASTVisitor().visit(ast);
        AtomicReference<CharStream> charsASM = new AtomicReference<>();
        if (!Objects.equals(fileName, "")) {
            assertDoesNotThrow(() -> {
                final var out = new BufferedWriter(new FileWriter(fileName + ".asm"));
                out.write(code);
                out.close();
            });
            // Assembling generated code
            assertDoesNotThrow(() -> {
                charsASM.set(CharStreams.fromFileName(fileName + ".asm"));
            });
        } else {
            charsASM.set(CharStreams.fromString(code));
        }
        SVMLexer lexerASM = new SVMLexer(charsASM.get());
        CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
        SVMParser parserASM = new SVMParser(tokensASM);

        parserASM.assembly();

        // No errors should occur if MIPS ISA is respected
        assertEquals(0, lexerASM.lexicalErrors);
        assertEquals(0, parserASM.getNumberOfSyntaxErrors());

        // creating Stack Virtual Machine
        ExecuteVM vm = new ExecuteVM(parserASM.code);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);

        // Running generated code via Stack Virtual Machine
        vm.cpu();

        System.setOut(originalOut);

        // The FOOL program should give expected output
        // TODO: .trim() could be avoided in generalization
        assertEquals(result, baos.toString().trim().replace("\n", "").replace("\r", ""));
    }

    public static void testResultFromString(final String input, String result) {
        testResult("", CharStreams.fromString(input), result);
    }

    public static void testResultFromFile(final String fileName, String result) {
        testResult(fileName, fromFile(fileName), result);
    }

    public static void testResultTrue(String fileName, final CharStream input) {
        testResult(fileName, input, "1");
    }

    public static void testResultTrueFromString(final String input) {
        testResultTrue("", CharStreams.fromString(input));
    }

    public static void testResultTrueFromFile(String fileName) {
        testResultTrue(fileName, fromFile(fileName));
    }

    public static void testResultFalse(String fileName, final CharStream input) {
        testResult(fileName, input, "0");
    }

    public static void testResultFalseFromString(final String input) {
        testResultFalse("", CharStreams.fromString(input));
    }

    public static void testResultFalseFromFile(String fileName) {
        testResultFalse(fileName, fromFile(fileName));
    }

    @ParameterizedTest
    @CsvSource({"prova1, 10", "prova2, 7", "prova3, 8"})
    protected void testPrograms(String fileName, String result) {
        fileName = "sample_programs/" + fileName + ".fool";
        testResultFromFile(fileName, result);
    }
}
