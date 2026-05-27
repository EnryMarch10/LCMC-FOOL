package compiler;

import compiler.exc.IncomplException;
import compiler.exc.TypeException;
import compiler.lib.FOOLlib;
import compiler.lib.Node;
import compiler.lib.TypeNode;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import svm.ExecuteVM;
import svm.SVMLexer;
import svm.SVMParser;

public class Test {
    static void main(final String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("ERROR: Fool program directory must be passed as input");
            System.exit(1);
        }
        final String fileName = args[0];

        final CharStream chars = CharStreams.fromFileName(fileName);
        final var lexer = new FOOLLexer(chars);
        final var tokens = new CommonTokenStream(lexer);
        final var parser = new FOOLParser(tokens);

        IO.println("Generating ST via lexer and parser.");
        final ParseTree st = parser.prog();
        IO.println("You had "
                + lexer.lexicalErrors
                + " lexical errors and "
                + parser.getNumberOfSyntaxErrors()
                + " syntax errors.\n");

        IO.println("Generating AST.");
        final var visitor = new ASTGenerationSTVisitor(); // use true to visualize the ST
        final Node ast = visitor.visit(st);
        IO.println();

        IO.println("Enriching AST via symbol table.");
        final var symtableVisitor = new SymbolTableASTVisitor();
        symtableVisitor.visit(ast);
        IO.println("You had " + symtableVisitor.stErrors + " symbol table errors.\n");

        IO.println("Visualizing Enriched AST.");
        new PrintEASTVisitor().visit(ast);
        IO.println();

        IO.println("Checking Types.");
        try {
            final var typeCheckVisitor = new TypeCheckEASTVisitor();
            final TypeNode mainType = typeCheckVisitor.visit(ast);
            IO.print("Type of main program expression is: ");
            new PrintEASTVisitor().visit(mainType);
        } catch (IncomplException _) {
            IO.println("Could not determine main program expression type due to errors detected before type checking.");
        } catch (TypeException e) {
            IO.println("Type checking error in main program expression: " + e.text);
        }
        IO.println("You had " + FOOLlib.typeErrors + " type checking errors.\n");

        final var frontEndErrors =
                lexer.lexicalErrors + parser.getNumberOfSyntaxErrors() + symtableVisitor.stErrors + FOOLlib.typeErrors;
        IO.println("You had a total of " + frontEndErrors + " front-end errors.\n");

        if (frontEndErrors > 0) {
            System.exit(1);
        }

        IO.println("Generating code.");
        final String code = new CodeGenerationASTVisitor().visit(ast);
        final var out = new BufferedWriter(new FileWriter(fileName + ".asm"));
        out.write(code);
        out.close();
        IO.println();

        IO.println("Assembling generated code.");
        final CharStream charsASM = CharStreams.fromFileName(fileName + ".asm");
        final var lexerASM = new SVMLexer(charsASM);
        final var tokensASM = new CommonTokenStream(lexerASM);
        final var parserASM = new SVMParser(tokensASM);

        parserASM.assembly();

        // needed only for debug
        IO.println("You had: "
                + lexerASM.lexicalErrors
                + " lexical errors and "
                + parserASM.getNumberOfSyntaxErrors()
                + " syntax errors.\n");
        if (lexerASM.lexicalErrors + parserASM.getNumberOfSyntaxErrors() > 0) {
            System.exit(1);
        }

        IO.println("Running generated code via Stack Virtual Machine.");
        final var vm = new ExecuteVM(parserASM.code);
        vm.cpu();
    }
}
