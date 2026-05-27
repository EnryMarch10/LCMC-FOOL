package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseEASTVisitor;
import compiler.lib.Node;

/** Class that visits and prints an Enriched Abstract Syntax Tree (EAST). */
public class PrintEASTVisitor extends BaseEASTVisitor<Void, VoidException> {
    PrintEASTVisitor() {
        super(false, true);
    }

    @Override
    public Void visitNode(final ProgLetInNode n) {
        printNode(n);
        for (Node dec : n.declist) {
            visit(dec);
        }
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(final ProgNode n) {
        printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(final FunNode n) {
        printNode(n, n.id);
        visit(n.retType, "return type: ");
        for (ParNode par : n.parlist) {
            visit(par);
        }
        for (Node dec : n.declist) {
            visit(dec);
        }
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(final ParNode n) {
        printNode(n, n.id);
        visit(n.getType());
        return null;
    }

    @Override
    public Void visitNode(final VarNode n) {
        printNode(n, n.id);
        visit(n.getType());
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(final PrintNode n) {
        printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(final IfNode n) {
        printNode(n);
        visit(n.cond);
        visit(n.th);
        visit(n.el);
        return null;
    }

    @Override
    public Void visitNode(final EqualNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(final GreaterEqualNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(final LessEqualNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(final AndNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(final OrNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(final NotNode n) {
        printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(final TimesNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(final DivNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(final PlusNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(final MinusNode n) {
        printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(final CallNode n) {
        printNode(n, n.id + " at nestinglevel " + n.nl);
        visit(n.entry);
        for (Node arg : n.arglist) {
            visit(arg);
        }
        return null;
    }

    @Override
    public Void visitNode(final IdNode n) {
        printNode(n, n.id + " at nestinglevel " + n.nl);
        visit(n.entry);
        return null;
    }

    @Override
    public Void visitNode(final BoolNode n) {
        printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(final IntNode n) {
        printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(final ArrowTypeNode n) {
        printNode(n);
        for (Node par : n.parlist) {
            visit(par);
        }
        visit(n.ret, "->"); // marks return type
        return null;
    }

    @Override
    public Void visitNode(final BoolTypeNode n) {
        printNode(n);
        return null;
    }

    @Override
    public Void visitNode(final IntTypeNode n) {
        printNode(n);
        return null;
    }

    @Override
    public Void visitNode(final ClassNode n) {
        printNode(n, n.id);
        n.fields.forEach(this::visit);
        n.methods.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(final FieldNode n) {
        printNode(n, n.id);
        visit(n.getType());
        return null;
    }

    @Override
    public Void visitNode(final MethodNode n) {
        printNode(n, n.id + "() with offset " + n.offset);
        visit(n.returnType, "return type: ");
        n.pars.forEach(this::visit);
        n.decs.forEach(this::visit);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(final ClassCallNode n) {
        printNode(n, n.refId + "." + n.methodId + "() at nestinglevel " + n.nl);
        visit(n.refEntry);
        visit(n.methodEntry);
        n.args.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(final NewNode n) {
        printNode(n, n.id + " at nestinglevel " + n.nl);
        visit(n.entry);
        n.args.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(final EmptyNode n) {
        printNode(n);
        return null;
    }

    @Override
    public Void visitNode(final ClassTypeNode n) {
        printNode(n);
        n.fields.forEach(this::visit);
        n.methods.forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNode(final RefTypeNode n) {
        printNode(n, n.classId);
        return null;
    }

    @Override
    public Void visitNode(final EmptyTypeNode n) {
        printNode(n);
        return null;
    }

    @Override
    public Void visitSTentry(final STentry entry) {
        printSTentry("nestlev " + entry.nl);
        printSTentry("type");
        visit(entry.type);
        printSTentry("offset " + entry.offset);
        return null;
    }
}
