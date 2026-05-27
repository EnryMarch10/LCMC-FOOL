package compiler.lib;

import static compiler.lib.FOOLlib.extractNodeName;

import compiler.AST.*;
import compiler.exc.IncomplException;
import compiler.exc.UnimplException;

/**
 * Class that implements a visitor item (of the Visitor Patter) for an Abstract Syntax Tree (AST).
 *
 * @param <S> Visitor return type.
 * @param <E> Exception type.
 */
public class BaseASTVisitor<S, E extends Exception> {
    protected boolean print; // enables printing
    protected String indent;
    private boolean incomplExc; // enables throwing IncomplException

    protected BaseASTVisitor() { }

    protected BaseASTVisitor(final boolean ie) {
        incomplExc = ie;
    }

    protected BaseASTVisitor(final boolean ie, final boolean p) {
        incomplExc = ie;
        print = p;
    }

    protected void printNode(final Node n) {
        IO.println(indent + extractNodeName(n.getClass().getName()));
    }

    protected void printNode(final Node n, final String s) {
        IO.println(indent + extractNodeName(n.getClass().getName()) + ": " + s);
    }

    public S visit(final Visitable v) throws E {
        return visit(v, ""); // performs unmarked visit
    }

    public S visit(final Visitable v, final String mark) throws E { // when printing marks this visit with string mark
        if (v == null) {
            if (incomplExc) {
                throw new IncomplException();
            } else {
                return null;
            }
        }
        if (print) {
            final String temp = indent;
            indent = indent == null ? "" : indent + "  ";
            indent += mark; // inserts mark
            try {
                return visitByAcc(v);
            } finally {
                indent = temp;
            }
        } else {
            return visitByAcc(v);
        }
    }

    S visitByAcc(final Visitable v) throws E {
        return v.accept(this);
    }

    public S visitNode(final ProgLetInNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final ProgNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final FunNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final ParNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final VarNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final PrintNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final IfNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final EqualNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final TimesNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final PlusNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final CallNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final IdNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final BoolNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final IntNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final ArrowTypeNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final BoolTypeNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final IntTypeNode n) throws E {
        throw new UnimplException();
    }

    // OPERATOR EXTENSION

    public S visitNode(final DivNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final MinusNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final GreaterEqualNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final LessEqualNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final AndNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final OrNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final NotNode n) throws E {
        throw new UnimplException();
    }

    // OBJECT-ORIENTED EXTENSION

    public S visitNode(final ClassNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final FieldNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final MethodNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final ClassCallNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final NewNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final EmptyNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final ClassTypeNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final RefTypeNode n) throws E {
        throw new UnimplException();
    }

    public S visitNode(final EmptyTypeNode n) throws E {
        throw new UnimplException();
    }
}
