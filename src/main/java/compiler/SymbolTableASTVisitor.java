package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;
import compiler.lib.TypeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 *   <li>Resolve identifier usages according to the "most closely nested" scope rule.
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

    public SymbolTableASTVisitor() {}

    public SymbolTableASTVisitor(boolean debug) {
        super(debug);
    } // enables print for debugging

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
     * @param hmn the hash map to put in the ST
     * @return the decOffset value of the previous nesting level
     */
    private int enterScope(Map<String, STentry> hmn) {
        // Creation of a new hashmap for the SymTable
        nestingLevel++;
        symTable.add(hmn);
        int prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2;
        return prevNLDecOffset;
    }

    /**
     * Exits the current scope, removing the corresponding Symbol Table map and restoring the decOffset to the previous
     * value.
     *
     * @param prevNLDecOffset the decOffset value of the previous nesting level
     */
    private void exitScope(int prevNLDecOffset) {
        symTable.remove(nestingLevel--); // removing current hashmap because exiting scope
        decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting
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
        Map<String, STentry> hm = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.retType), decOffset--);
        // Putting ID into the SymTable
        if (hm.put(n.id, entry) != null) {
            registerSTError("Fun id " + n.id + " at line " + n.getLine() + " already declared");
        }
        // Creation of a new hashmap for the SymTable
        Map<String, STentry> hmn = new HashMap<>();
        int prevNLDecOffset = enterScope(hmn);
        int parOffset = 1;
        for (ParNode par : n.parlist) {
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                registerSTError("Par id " + par.id + " at line " + n.getLine() + " already declared");
            }
        }
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        exitScope(prevNLDecOffset);
        // level
        return null;
    }

    @Override
    public Void visitNode(VarNode n) {
        if (print) printNode(n);
        visit(n.exp);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        STentry entry = new STentry(nestingLevel, n.getType(), decOffset--);
        // inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
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
        STentry entry = stLookup(n.id);
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
        Map<String, STentry> hm = symTable.get(nestingLevel);
        List<TypeNode> allFields = new ArrayList<>();
        List<ArrowTypeNode> allMethods = new ArrayList<>();
        if (hm.put(n.id, new STentry(nestingLevel, new ClassTypeNode(allFields, allMethods), decOffset--)) != null) {
            registerSTError("Class id " + n.id + " at line " + n.getLine() + " already declared");
        }
        Map<String, STentry> virtualTable = new HashMap<>();
        classTable.put(n.id, virtualTable);
        int prevNLDecOffset = enterScope(virtualTable);
        int fieldOffset = -1;
        for (FieldNode field : n.fields) {
            if (virtualTable.put(field.id, new STentry(nestingLevel, field.getType(), fieldOffset)) != null) {
                registerSTError("Field id " + field.id + " at line " + field.getLine() + " already declared");
            }
            allFields.add(-fieldOffset - 1, field.getType());
            fieldOffset--;
        }
        int methodOffset = 0;
        for (MethodNode method : n.methods) {
            method.offset = methodOffset++;
            visit(method);
            // After its visit, we can get the method's type directly from the virtualTable
            allMethods.add(method.offset, (ArrowTypeNode) virtualTable.get(method.id).type);
        }
        exitScope(prevNLDecOffset);
        return null;
    }

    @Override
    public Void visitNode(MethodNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.pars) parTypes.add(par.getType());
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.returnType), n.offset);
        if (hm.put(n.id, entry) != null) {
            registerSTError("Method id " + n.id + " at line " + n.getLine() + " already declared");
        }
        Map<String, STentry> hmn = new HashMap<>();
        int prevNLDecOffset = enterScope(hmn);
        int parOffset = 1;
        for (ParNode par : n.pars) {
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                registerSTError("Par id " + par.id + " at line " + par.getLine() + " already declared");
            }
        }
        for (Node dec : n.decs) {
            visit(dec);
        }
        visit(n.exp);
        exitScope(prevNLDecOffset);
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.refId);
        if (entry == null) {
            registerSTError("Reference id " + n.refId + " at line " + n.getLine() + " not declared");
        } else {
            if (!(entry.type instanceof RefTypeNode)) {
                registerSTError("Id " + n.refId + " at line " + n.getLine() + " is not a reference identifier");
            } else {
                n.refEntry = entry;
                String classId = ((RefTypeNode) entry.type).classId;
                Map<String, STentry> virtualTable = classTable.get(classId);
                if (virtualTable == null) {
                    registerSTError("Class id " + classId + " of reference identifier " + n.refId + " at line "
                            + n.getLine() + " not declared");
                } else {
                    STentry methodEntry = virtualTable.get(n.methodId);
                    if (methodEntry == null) {
                        registerSTError("Method id " + n.methodId + " at line " + n.getLine() + " not declared");
                    } else {
                        n.methodEntry = methodEntry;
                        n.nl = nestingLevel;
                    }
                }
            }
        }
        for (Node arg : n.args) {
            visit(arg);
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
                        "Class ID " + n.id + " is in the class table but not in the symbol table");
            }
            n.entry = entry;
            n.nl = nestingLevel;
        }
        for (Node arg : n.args) {
            visit(arg);
        }
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) {
        if (print) printNode(n);
        return null;
    }
}
