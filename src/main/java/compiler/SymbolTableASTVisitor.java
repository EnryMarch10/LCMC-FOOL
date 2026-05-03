package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;
import compiler.lib.TypeNode;
import java.util.*;

/**
 * Class that represents a visitor of an Abstract Syntax Tree and transforms it into an Enriched Abstract Syntax Tree
 * (AST -> EAST), where at some node of the AST is attached a Symbol Table Entry.
 *
 * <p>Performs the first step of the Checker (3-rd component of the Compiler).
 *
 * <p>During this process, the Symbol Table is used to:
 *
 * <ul>
 *   <li>Detect multiple declarations of the same identifier within the same scope.
 *   <li>Resolve identifier usages according to the "most closely nested" scope rule (detects undeclared variables).
 * </ul>
 *
 * <p>Each identifier node ({@link VarNode}, {@link FunNode}, and {@link ParNode}) in the AST is linked to its
 * corresponding {@link STentry}.
 */
public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {
    public int stErrors = 0;
    private final List<Map<String, STentry>> symTable = new ArrayList<>();
    private final Map<String, Map<String, STentry>> classTable = new HashMap<>();
    private int nestingLevel = 0; // current nesting level
    private int decOffset = -2; // counter for offset of local declarations at current nesting level
    private final Stack<Integer> prevNLDecOffset = new Stack<>();

    public SymbolTableASTVisitor() {}

    public SymbolTableASTVisitor(boolean debug) {
        super(debug);
    }

    private STentry stLookup(String id) {
        int j = nestingLevel;
        STentry entry = null;
        if (symTable.size() > j) {
            while (j >= 0 && entry == null) {
                entry = symTable.get(j--).get(id);
            }
        }
        return entry;
    }

    /**
     * Enters inner nesting level and puts the new hash map in the current Symbol Table scope.
     *
     * @return the decOffset value of the previous nesting level
     */
    private Map<String, STentry> enterScope() {
        // Creation of a new hashmap for the SymTable
        nestingLevel++;
        Map<String, STentry> hashTableNested = new HashMap<>();
        symTable.add(hashTableNested);
        prevNLDecOffset.push(decOffset); // stores counter for offset of declarations at previous nesting level
        decOffset = -2;
        return hashTableNested;
    }

    /**
     * Exits the current scope, removing the corresponding Symbol Table map and restoring the decOffset to the previous
     * value.
     */
    private void exitScope() {
        symTable.remove(nestingLevel--); // removing current hashmap because exiting scope
        decOffset = prevNLDecOffset.pop(); // restores counter for offset of declarations at previous nesting level
    }

    /**
     * Signals a new Symbol Table error with the given message.
     *
     * @param msg the error message
     */
    private void registerSTError(String msg) {
        System.out.println(msg);
        stErrors++;
    }

    @Override
    public Void visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = new HashMap<>();
        symTable.add(hm);
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        symTable.removeFirst();
        return null;
    }

    @Override
    public Void visitNode(ProgNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode n) {
        if (print) printNode(n);
        var hashTable = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());
        if (hashTable.put(n.id, new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.retType), decOffset--))
                != null) {
            registerSTError("Function id " + n.id + " at line " + n.getLine() + " already declared");
        }
        var hashTableNested = enterScope();
        int parOffset = 1;
        for (ParNode par : n.parlist) {
            if (hashTableNested.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                registerSTError("Function Par id " + par.id + " at line " + n.getLine() + " already declared");
            }
        }
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        exitScope();
        return null;
    }

    @Override
    public Void visitNode(VarNode n) {
        if (print) printNode(n);
        visit(n.exp);
        var hashTable = symTable.get(nestingLevel);
        if (hashTable.put(n.id, new STentry(nestingLevel, n.getType(), decOffset--)) != null) {
            registerSTError("Var id " + n.id + " at line " + n.getLine() + " already declared");
        }
        return null;
    }

    @Override
    public Void visitNode(PrintNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode n) {
        if (print) printNode(n);
        visit(n.cond);
        visit(n.th);
        visit(n.el);
        return null;
    }

    @Override
    public Void visitNode(EqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(GreaterEqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(LessEqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(AndNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(OrNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(NotNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(TimesNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(DivNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(PlusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(MinusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(CallNode n) {
        if (print) printNode(n);
        var entry = stLookup(n.id);
        if (entry == null) {
            registerSTError("Fun id " + n.id + " at line " + n.getLine() + " not declared");
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(IdNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            registerSTError("Var or Par id " + n.id + " at line " + n.getLine() + " not declared");
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        return null;
    }

    @Override
    public Void visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(ClassNode n) {
        if (print) printNode(n);
        var hashTable = symTable.get(nestingLevel);
        List<TypeNode> fieldTypes = new ArrayList<>();
        List<ArrowTypeNode> methodTypes = new ArrayList<>();
        if (hashTable.put(n.id, new STentry(nestingLevel, new ClassTypeNode(fieldTypes, methodTypes), decOffset--))
                != null) {
            registerSTError("ClassId " + n.id + " at line " + n.getLine() + " already declared");
        }
        var virtualTable = enterScope();
        classTable.put(n.id, virtualTable);
        int fieldOffset = -1;
        for (FieldNode field : n.fields) {
            if (virtualTable.put(field.id, new STentry(nestingLevel, field.getType(), fieldOffset)) != null) {
                registerSTError("FieldId " + field.id + " at line " + n.getLine() + " already declared");
            }
            fieldTypes.add(-fieldOffset - 1, field.getType());
            fieldOffset--;
        }
        int methodOffset = 0;
        for (MethodNode method : n.methods) {
            method.offset = methodOffset++;
            visit(method);
            // After its visit, we can get the method's type directly from the virtualTable
            methodTypes.add(method.offset, (ArrowTypeNode) virtualTable.get(method.id).type);
        }
        exitScope();
        return null;
    }

    @Override
    public Void visitNode(MethodNode n) {
        if (print) printNode(n);
        var virtualTable = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.pars) parTypes.add(par.getType());
        if (virtualTable.put(n.id, new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.returnType), n.offset))
                != null) {
            registerSTError("Method id " + n.id + " at line " + n.getLine() + " already declared");
        }
        var hashTableNested = enterScope();
        int parOffset = 1;
        for (ParNode par : n.pars) {
            if (hashTableNested.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                registerSTError("Method Par id " + par.id + " at line " + n.getLine() + " already declared");
            }
        }
        for (Node dec : n.decs) visit(dec);
        visit(n.exp);
        exitScope();
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode n) {
        if (print) printNode(n);
        var refEntry = stLookup(n.refId);
        if (refEntry == null) {
            registerSTError("Reference id " + n.refId + " at line " + n.getLine() + " not declared");
        } else {
            // refEntry type check done here, a small violation to SRP (single responsibility principle)
            if (refEntry.type instanceof RefTypeNode refType) {
                n.refEntry = refEntry;
                var virtualTable = classTable.get(refType.classId);
                if (virtualTable == null) {
                    registerSTError("Class id " + refType.classId + " of reference identifier " + n.refId + " at line "
                            + n.getLine() + " not declared");
                } else {
                    var methodEntry = virtualTable.get(n.methodId);
                    if (methodEntry == null) {
                        registerSTError("Method id " + n.methodId + "() at line " + n.getLine()
                                + " not declared for class " + refType.classId);
                    } else {
                        n.methodEntry = methodEntry;
                        n.nl = nestingLevel;
                    }
                    for (Node arg : n.args) visit(arg);
                }
            } else {
                registerSTError("Id " + n.refId + " at line " + n.getLine() + " is not a reference identifier");
            }
        }
        return null;
    }

    @Override
    public Void visitNode(NewNode n) {
        if (!classTable.containsKey(n.id)) {
            registerSTError("Class id " + n.id + " at line " + n.getLine() + " not declared");
        } else {
            // Gets the class entry by performing a lookup. Reads every level for extendability
            // (in case of nested classes in the future)
            STentry entry = stLookup(n.id);
            if (entry == null) {
                throw new IllegalStateException(
                        "Class id " + n.id + " is in the class table but not in the symbol table");
            }
            n.entry = entry;
            n.nl = nestingLevel;
        }
        for (Node arg : n.args) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) {
        if (print) printNode(n);
        return null;
    }
}
