import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import compiler.*;
import compiler.exc.IncomplException;
import compiler.exc.TypeException;
import compiler.lib.FOOLlib;
import compiler.lib.Node;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
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
    public static final String SAMPLES = "samples/";

    public static CharStream fromFile(String fileName) {
        final var chars = new AtomicReference<CharStream>();
        assertDoesNotThrow(() -> chars.set(CharStreams.fromFileName(fileName)));
        return chars.get();
    }

    public static int getScanningErrors(final CharStream input) {
        final var lexer = new FOOLLexer(input);
        final var tokens = new CommonTokenStream(lexer);
        final var parser = new FOOLParser(tokens);
        parser.prog();
        return lexer.lexicalErrors;
    }

    public static int getParsingErrors(final CharStream input) {
        final var lexer = new FOOLLexer(input);
        final var tokens = new CommonTokenStream(lexer);
        final var parser = new FOOLParser(tokens);
        parser.prog();
        assertEquals(0, lexer.lexicalErrors);
        return parser.getNumberOfSyntaxErrors();
    }

    public static int getSymbolTableErrors(final CharStream input) {
        final var lexer = new FOOLLexer(input);
        final var tokens = new CommonTokenStream(lexer);
        final var parser = new FOOLParser(tokens);
        final ParseTree st = parser.prog();
        assertEquals(0, lexer.lexicalErrors);
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        final var visitor = new ASTGenerationSTVisitor();
        final Node ast = visitor.visit(st);
        final var symtableVisitor = new SymbolTableASTVisitor();
        symtableVisitor.visit(ast);
        return symtableVisitor.stErrors;
    }

    public static int getTypeErrors(final CharStream input) {
        // Static fields must be always reset
        FOOLlib.reset();

        final var lexer = new FOOLLexer(input);
        final var tokens = new CommonTokenStream(lexer);
        final var parser = new FOOLParser(tokens);
        final ParseTree st = parser.prog();
        assertEquals(0, lexer.lexicalErrors);
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        final var visitor = new ASTGenerationSTVisitor();
        final Node ast = visitor.visit(st);
        final var symtableVisitor = new SymbolTableASTVisitor();
        symtableVisitor.visit(ast);
        assertEquals(0, symtableVisitor.stErrors);
        try {
            final var typeCheckVisitor = new TypeCheckEASTVisitor();
            typeCheckVisitor.visit(ast);
            // Doesn't check return type correctness of FOOL language
        } catch (IncomplException _) {
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

        final var lexer = new FOOLLexer(input);
        final var tokens = new CommonTokenStream(lexer);
        final var parser = new FOOLParser(tokens);

        // Generating ST via lexer and parser
        final ParseTree st = parser.prog();
        assertEquals(0, lexer.lexicalErrors);
        assertEquals(0, parser.getNumberOfSyntaxErrors());

        // Generating AST
        final var visitor = new ASTGenerationSTVisitor();
        final Node ast = visitor.visit(st);

        // Enriching AST via symbol table
        final var symtableVisitor = new SymbolTableASTVisitor();
        symtableVisitor.visit(ast);
        assertEquals(0, symtableVisitor.stErrors);

        // Checking Types
        try {
            final var typeCheckVisitor = new TypeCheckEASTVisitor();
            typeCheckVisitor.visit(ast);
            // NOTE: does not check return type correctness of FOOL language
        } catch (IncomplException _) {
            System.err.println(
                    "Could not determine main program expression type due to errors detected before type checking.");
        } catch (TypeException e) {
            System.err.println("Type checking error in main program expression: " + e.text);
        }
        assertEquals(0, FOOLlib.typeErrors);

        // lexer.lexicalErrors + parser.getNumberOfSyntaxErrors() + symtableVisitor.stErrors + FOOLlib.typeErrors = 0

        // Generating code.
        final String code = new CodeGenerationASTVisitor().visit(ast);
        final var charsASM = new AtomicReference<CharStream>();
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
        final var lexerASM = new SVMLexer(charsASM.get());
        final var tokensASM = new CommonTokenStream(lexerASM);
        final var parserASM = new SVMParser(tokensASM);

        parserASM.assembly();

        // No errors should occur if MIPS ISA is respected
        assertEquals(0, lexerASM.lexicalErrors);
        assertEquals(0, parserASM.getNumberOfSyntaxErrors());

        // creating Stack Virtual Machine
        final var vm = new ExecuteVM(parserASM.code);

        final PrintStream originalOut = System.out;
        final var baos = new ByteArrayOutputStream();
        final var ps = new PrintStream(baos);
        System.setOut(ps);

        // Running generated code via Stack Virtual Machine
        vm.cpu();

        System.setOut(originalOut);

        // The FOOL program should give expected output
        // NOTE: .trim() could be avoided in generalization
        assertEquals(result, baos.toString().trim().replace("\n", "").replace("\r", ""));
    }

    public static void testResultFromString(final String input, final String result) {
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
        fileName = SAMPLES + fileName + ".fool";
        testResultFromFile(fileName, result);
    }
}
