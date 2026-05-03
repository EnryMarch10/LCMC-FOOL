package compiler;

import static compiler.lib.FOOLlib.*;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;
import java.util.ArrayList;
import java.util.List;
import svm.ExecuteVM;

/**
 * Class responsible for generating code for a Stack Virtual Machine (SVM). It targets the MIPS ISA (Microprocessor
 * without Interlocked Pipeline Stages Instruction Set Architecture), a RISC architecture.
 *
 * <p>Performs the code generation phase of the compiler, translating stack‑machine instructions into MIPS assembly.
 * This class acts as the Code Generator (4-th and last component of the Compiler).
 */
public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {
    private final List<List<String>> dispatchTables = new ArrayList<>();

    public CodeGenerationASTVisitor() {}

    public CodeGenerationASTVisitor(boolean debug) {
        super(false, debug);
    } // enables print for debugging

    @Override
    public String visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        String declCode = null;
        for (Node dec : n.declist) declCode = nlJoin(declCode, visit(dec));
        return nlJoin(
                "push 0", // fake return address, could be any value, used for consistency (offset -2 for declarations
                // inside a function in symbol table)
                declCode, // generate code for declarations (allocation)
                visit(n.exp),
                "halt",
                getCode());
    }

    @Override
    public String visitNode(ProgNode n) {
        if (print) printNode(n);
        return nlJoin(visit(n.exp), "halt");
    }

    @Override
    public String visitNode(FunNode n) {
        if (print) printNode(n, n.id);
        String declCode = null, popDecl = null, popParl = null;
        for (Node dec : n.declist) {
            declCode = nlJoin(declCode, visit(dec));
            popDecl = nlJoin(popDecl, "pop");
        }
        for (int i = 0; i < n.parlist.size(); i++) popParl = nlJoin(popParl, "pop");
        String funl = freshFunLabel();
        putCode(nlJoin(
                funl + ":",
                "cfp", // set $fp to $sp value
                "lra", // load $ra value
                declCode, // generate code for local declarations (they use the new $fp!!!)
                visit(n.exp), // generate code for function body expression
                "stm", // set $tm to popped value (function result)
                popDecl, // remove local declarations from stack
                "sra", // set $ra to popped value
                "pop", // remove Access Link from stack
                popParl, // remove parameters from stack
                "sfp", // set $fp to popped value (Control Link)
                "ltm", // load $tm value (function result)
                "lra", // load $ra value
                "js" // jump to popped address
                ));
        return "push " + funl;
    }

    // Step in creating AR (activation record)
    @Override
    public String visitNode(VarNode n) {
        if (print) printNode(n, n.id);
        return visit(n.exp);
    }

    @Override
    public String visitNode(PrintNode n) {
        if (print) printNode(n);
        return nlJoin(visit(n.exp), "print"); // prints top of the stack
    }

    @Override
    public String visitNode(IfNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.cond),
                "push 1", // push true to check if cond is true
                "beq " + l1, // checks if cond is true
                visit(n.el),
                "b " + l2,
                l1 + ":",
                visit(n.th),
                l2 + ":");
    }

    @Override
    public String visitNode(EqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "beq " + l1, // checks if left equals right
                "push 0", // return false
                "b " + l2,
                l1 + ":",
                "push 1", // return true
                l2 + ":");
    }

    @Override
    public String visitNode(GreaterEqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                // Pushes the right operand first, since l >= r is equivalent to r <= l
                visit(n.right),
                visit(n.left),
                "bleq " + l1, // checks if right <= left
                "push 0", // return false
                "b " + l2,
                l1 + ":",
                "push 1", // return true
                l2 + ":");
    }

    @Override
    public String visitNode(LessEqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "bleq " + l1, // checks if left <= right
                "push 0", // return false
                "b " + l2,
                l1 + ":",
                "push 1", // return true
                l2 + ":");
    }

    @Override
    public String visitNode(AndNode n) {
        if (print) printNode(n);
        String end = freshLabel();
        String leftTrue = freshLabel();
        String rightTrue = freshLabel();
        return nlJoin(
                // Evaluates the left operand
                visit(n.left),
                "push 1",
                "beq " + leftTrue, // If it's true, jumps to the evaluation of right operand
                // If it's false, returns "false" as a result and jumps directly to the end (short-circuit evaluation)
                "push 0",
                "b " + end,
                // Evaluates the right operand
                leftTrue + ":",
                visit(n.right),
                "push 1",
                "beq " + rightTrue,
                // If it's false, returns "false" and jumps to the end
                "push 0",
                "b " + end,
                // If it's true, returns "true"
                rightTrue + ":",
                "push 1",
                end + ":");
    }

    @Override
    public String visitNode(OrNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin( // Does SHORT CIRCUIT EVALUATION
                visit(n.left),
                "push 1", // push true to check if left cond is true
                "beq " + l1, // checks if left cond is true
                visit(n.right),
                "push 1", // push true to check if right cond is true
                "beq " + l1, // checks if right cond is true
                "push 0", // return false
                "b " + l2,
                l1 + ":",
                "push 1", // return true
                l2 + ":");
    }

    public String visitNode(NotNode n) {
        if (print) printNode(n);
        String expressionTrue = freshLabel();
        String end = freshLabel();
        return nlJoin(
                // Evaluates expression
                visit(n.exp),
                "push 1", // push true
                "beq " + expressionTrue, // If the expression is true, jumps and pushes 0 (false)
                // Else, pushes 1 (true)
                "push 1", // return true
                "b " + end,
                expressionTrue + ":",
                "push 0", // return false
                end + ":");
    }

    @Override
    public String visitNode(TimesNode n) {
        if (print) printNode(n);
        return nlJoin(visit(n.left), visit(n.right), "mult");
    }

    @Override
    public String visitNode(DivNode n) {
        if (print) printNode(n);
        return nlJoin(visit(n.left), visit(n.right), "div");
    }

    @Override
    public String visitNode(PlusNode n) {
        if (print) printNode(n);
        return nlJoin(visit(n.left), visit(n.right), "add");
    }

    @Override
    public String visitNode(MinusNode n) {
        if (print) printNode(n);
        return nlJoin(visit(n.left), visit(n.right), "sub");
    }

    @Override
    public String visitNode(CallNode n) {
        if (print) printNode(n, n.id + "()");
        String argCode = null, getAR = null;
        for (int i = n.arglist.size() - 1; i >= 0; i--) argCode = nlJoin(argCode, visit(n.arglist.get(i)));
        for (int i = 0; i < n.nl - n.entry.nl; i++) getAR = nlJoin(getAR, "lw");
        if (n.entry.offset < 0) {
            return nlJoin(
                    "lfp", // load Control Link (pointer to frame of function "id" caller)
                    argCode, // generate code for argument expressions in reversed order
                    "lfp",
                    getAR, // retrieve address of frame containing "object" declaration
                    // by following the static chain (of Access Links)
                    "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                    "ltm", // load Access Link (pointer to frame of function "id" declaration)
                    "ltm", // duplicate top of stack
                    "push " + n.entry.offset,
                    "add", // compute address of "id" declaration
                    "lw", // load address of "id" function
                    "js" // jump to popped address, function execution (saving address of subsequent instruction in $ra)
                    );
        }
        return nlJoin(
                "lfp", // load Control Link (pointer to frame of function "id" caller)
                argCode, // generate code for argument expressions in reversed order
                "lfp",
                getAR, // retrieve address of frame containing the object pointer
                // by following the static chain (of Access Links)
                "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                "ltm", // load Access Link (object pointer, according to ClassCallNode implementation)
                "ltm", // duplicate top of stack
                "lw", // replaces last object pointer with dispatch pointer
                "push " + n.entry.offset,
                "add", // compute address of method in dispatch table
                "lw", // load method address
                "js" // jump to popped address, method execution (saving address of subsequent instruction in $ra)
                );
    }

    @Override
    public String visitNode(IdNode n) {
        if (print) printNode(n, n.id);
        String getAR = null;
        for (int i = 0; i < n.nl - n.entry.nl; i++) getAR = nlJoin(getAR, "lw");
        return nlJoin(
                "lfp",
                getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "push " + n.entry.offset,
                "add", // compute address of "id" declaration
                "lw" // load value of "id" variable
                );
    }

    @Override
    public String visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return "push " + (n.val ? 1 : 0);
    }

    @Override
    public String visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return "push " + n.val;
    }

    @Override
    public String visitNode(ClassNode n) {
        if (print) printNode(n, n.id);
        dispatchTables.add(n.methods.stream()
                .map(method -> {
                    method.label = freshFunLabel();
                    visit(method);
                    return method.label;
                })
                .toList());
        // codice che alloca su heap la dispatch table della classe e lascia il dispatch pointer sullo stack
        String allocCode = null;
        for (String methodLabel : dispatchTables.getLast()) {
            allocCode = nlJoin(
                    allocCode,
                    "push " + methodLabel, // method address
                    "lhp", // load $hp
                    "sw", // writes method address at $hp
                    "lhp", // load $hp
                    "push 1", // load 1, used to make an increment
                    "add", // increments $hp
                    "shp" // updates $hp with incremented value
                    );
        }
        return nlJoin(
                "lhp", // load the dispatch pointer to " + n.id + " class"
                allocCode // creates the DISPATCH TABLE LAYOUT in heap
                );
    }

    @Override
    public String visitNode(MethodNode n) {
        String declCode = null, popDecl = null, popParl = null;
        for (Node dec : n.decs) {
            declCode = nlJoin(declCode, visit(dec));
            popDecl = nlJoin(popDecl, "pop");
        }
        for (int i = 0; i < n.pars.size(); i++) popParl = nlJoin(popParl, "pop");
        putCode(nlJoin(
                n.label + ":",
                "cfp", // set $fp to $sp value
                "lra", // load $ra value
                declCode, // generate code for local declarations (they use the new $fp!!!)
                visit(n.exp), // generate code for method body expression
                "stm", // set $tm to popped value (method result)
                popDecl, // remove local declarations from stack
                "sra", // set $ra to popped value
                "pop", // remove Access Link from stack
                popParl, // remove parameters from stack
                "sfp", // set $fp to popped value (Control Link)
                "ltm", // load $tm value (method result)
                "lra", // load $ra value
                "js" // jump to popped address
                ));
        return null;
    }

    @Override
    public String visitNode(EmptyNode n) {
        if (print) printNode(n);
        return "push -1";
    }

    @Override
    public String visitNode(ClassCallNode n) {
        if (print) printNode(n, n.refId + "." + n.methodId + "()");
        String argCode = null, getAR = null;
        for (int i = n.args.size() - 1; i >= 0; i--) argCode = nlJoin(argCode, visit(n.args.get(i)));
        for (int i = 0; i < n.nl - n.refEntry.nl; i++) getAR = nlJoin(getAR, "lw");
        return nlJoin(
                "lfp", // load Control Link (pointer to frame of function "id" caller)
                argCode, // generate code for argument expressions in reversed order
                // Retrieves ID1 value
                "lfp",
                getAR, // retrieve address of frame containing "object" declaration
                // by following the static chain (of Access Links)
                "push " + n.refEntry.offset,
                "add", // compute address of "object" declaration
                "lw", // load object pointer TODO: error!! Offset is wrong here
                "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                "ltm", // load Access Link (object pointer)
                "ltm", // duplicate top of stack
                "lw", // load dispatch pointer
                // Add method offset to access method address
                "push " + n.methodEntry.offset,
                "add",
                "lw", // load address of method from dispatch table
                "js" // jump to popped address, method execution (saving address of subsequent instruction in $ra)
                );
    }

    @Override
    public String visitNode(NewNode n) {
        if (print) printNode(n, n.id);
        String argCode = null, moveArgCode = null, getAR = null;
        for (var arg : n.args) {
            argCode = nlJoin(argCode, visit(arg));
            moveArgCode = nlJoin(
                    moveArgCode,
                    "lhp", // load $hp
                    "sw", // goes at $hp and writes argument value/address there (reverse order)
                    "lhp", // load $hp"
                    "push 1", // load 1, used to make an increment
                    "add", // increments $hp
                    "shp" // updates $hp with incremented value
                    );
        }
        // for (int i = 0; i < n.nl - n.entry.nl; i++) getAR = nlJoin(getAR, "lw");
        return nlJoin(
                argCode, // generate code for argument expressions
                moveArgCode, // creates the OBJECT LAYOUT in heap
                // TODO: rifare con nesting level? Decommentare sopra?
                "push " + (ExecuteVM.MEMSIZE + n.entry.offset), // class address in global environment
                "lw", // load dispatch pointer
                "lhp", // load $hp
                "sw", // writes at $hp the dispatch pointer
                "lhp", // load $hp
                "lhp", // duplicates $hp in stack to return it as result address
                "push 1", // load 1, used to make an increment
                "add", // increments $hp
                "shp" // updates $hp with incremented value
                );
    }
}
