package compiler;

import static compiler.lib.FOOLlib.*;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for generating code for a Stack Virtual Machine (SVM). It targets the MIPS ISA (Microprocessor
 * without Interlocked Pipeline Stages Instruction Set Architecture), a RISC architecture.
 *
 * <p>Performs the code generation phase of the compiler, translating stack‑machine instructions into MIPS assembly.
 * This class acts as the Code Generator (4-th and last component of the Compiler).
 */
public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

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
                "cfp", // set $fp to $sp value (saving reference position in AR)
                "lra", // push $ra on the stack
                declCode, // generate code for local declarations (they use the new $fp!!!)
                visit(n.exp), // generate code for function body expression
                "stm", // set $tm to popped value (saving the function result for later)
                popDecl, // remove local declarations from stack
                "sra", // set $ra to popped value (storing the return address for later)
                "pop", // remove Access Link from stack
                popParl, // remove parameters from stack
                "sfp", // set $fp to popped value (Control Link)
                "ltm", // load $tm value (leaving the function result on the stack)
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

    /**
     * Generates the code for the given list of arguments, pushing them in the stack in reversed order.
     * @param arglist the list of arguments.
     * @return the generated code.
     */
    private String generateArgCode(List<Node> arglist) {
        String argCode = null;
        for (int i = arglist.size() - 1; i >= 0; i--) argCode = nlJoin(argCode, visit(arglist.get(i)));
        return argCode;
    }

    /**
     * Generates the code for retrieving the address of the frame containing the declaration of a given ID.
     * This is done by following the static chain of access links, starting from the current AR.
     * @param usageNl the nesting level of the ID's usage.
     * @param entryNl the nesting level of the ID's declaration.
     * @return the generated code.
     */
    private String getDeclarationAR(int usageNl, int entryNl) {
        String getAR = null;
        for (int i = 0; i < usageNl - entryNl; i++) getAR = nlJoin(getAR, "lw");
        return getAR;
    }

    @Override
    public String visitNode(CallNode n) {
        if (print) printNode(n, n.id);
        // TODO: Modify according to instructions
        //String argCode = null; TODO: remove
        //for (int i = n.arglist.size() - 1; i >= 0; i--) argCode = nlJoin(argCode, visit(n.arglist.get(i)));
        return nlJoin(
                "lfp", // load Control Link (pointer to frame of function "id" caller)
                generateArgCode(n.arglist), // generate code for argument expressions in reversed order
                "lfp",
                getDeclarationAR(n.nl, n.entry.nl), // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                "ltm", // load Access Link (pointer to frame of function "id" declaration)
                "ltm", // duplicate top of stack
                "push " + n.entry.offset,
                "add", // compute address of "id" declaration
                "lw", // load address of "id" function
                "js" // jump to popped address (saving address of subsequent instruction in $ra)
                );
    }

    @Override
    public String visitNode(IdNode n) {
        if (print) printNode(n, n.id);
        return nlJoin(
                "lfp",
                getDeclarationAR(n.nl, n.entry.nl), // retrieve address of frame containing "id" declaration
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

    public String visitNode(ClassNode n) {
        if (print) printNode(n, n.id);
        List<String> dispatchTable = new ArrayList<>();
        for (MethodNode method : n.methods) {
            visit(method);
            dispatchTable.add(method.offset, method.label);
        }
        String dispatchTableAllocation = null;
        for (String label : dispatchTable) {
            // Saves the label in the heap and increments $hp
            dispatchTableAllocation = nlJoin(
                dispatchTableAllocation,
                "push " + label, // pushes the label on the stack
                "lhp", // loads the current $hp value
                "sw", // saves the pushed label in the heap (at address $hp)
                "lhp", // loads the current $hp value
                "push 1", // pushes increment value
                "add", // increments the current value of $hp on the stack
                "shp" // modifies $hp according to the new value
                );
        }
        return nlJoin(
            "lhp", // saves dispatch pointer on the stack (pointing to the bottom of the dispatch table)
            dispatchTableAllocation
        );
    }

    @Override
    public String visitNode(MethodNode n) {
        if (print) printNode(n, n.id);
        n.label = freshFunLabel();
        String declCode = null, popDecl = null, popParl = null;
        for (Node dec: n.decs) {
            declCode = nlJoin(declCode, visit(dec));
            popDecl = nlJoin(popDecl, "pop");
        }
        for (Node par : n.pars) popParl = nlJoin(popParl, "pop");
        putCode(nlJoin(
            n.label + ":",
            "cfp",  // set $fp to $sp value (saving reference position in AR)
            "lra", // push $ra on the stack
            declCode, // generate code for local declarations (they use the new $fp!!!)
            visit(n.exp), // generate code for function body expression
            "stm", // set $tm to popped value (saving the function result for later)
            popDecl, // remove local declarations from stack
            "sra", // set $ra to popped value (storing the return address for later)
            "pop", // remove Access Link from stack
            popParl, // remove parameters from stack
            "sfp", // set $fp to popped value (Control Link)
            "ltm", // load $tm value (leaving the function result on the stack)
            "lra", // load $ra value
            "js" // jump to popped address
        ));
        return null;
    }

    public String visitNode(ClassCallNode n) {
        if (print) printNode(n);
        return nlJoin(
            "lfp", // load Control Link (pointer to frame of function "id" caller)
            generateArgCode(n.args), // generate code for the method call's arguments
            "lfp", // load the address of the current frame's Access Link
            getDeclarationAR(n.nl, n.refEntry.nl), // retrieve the access of the frame containing the reference ID's
            // declaration
            "push " + n.refEntry.offset, // push the reference ID's offset on the stack
            "add", // compute the address of reference ID's declaration
            "lw", // load the reference ID's object pointer on the stack. This will be the value of the method's AL.
            // This makes it impossible to access IDs that are declared in the global scope, since the AL chain of a
            // method ends with the dispatch pointer of its class. This is fine, however, since FOOL's syntax only
            // allows to declare classes at the top of the scope, so it will never be possible to declare a function
            // or a variable before a class.
            "stm", // set $tm to popped value (with the aim of duplicating top of stack)
            "ltm", // load Access Link (object pointer)
            "ltm", // duplicate the top of the stack
            "lw", // load dispatch pointer on the stack (by dereferencing the object pointer)
            "push " + n.methodEntry.offset, // push the method's offset
            "add", // compute the method's position in the dispatch table
            "lw", // loads the method's address
            "js" // jump to popped address
        );
    }

    public String visitNode(NewNode n) {
        if (print) printNode(n);
        return null; // TODO: Implement
    }

    @Override
    public String visitNode(EmptyNode n) {
        if (print) printNode(n);
        return "push -1";
    }
}
