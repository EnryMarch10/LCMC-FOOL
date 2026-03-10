package compiler.lib;

import compiler.STentry;
import compiler.exc.UnimplException;

/**
 * Class that implements a visitor item (of the Visitor Patter) for an Enriched Abstract Syntax Tree (EAST).
 *
 * @param <S> Visitor return type.
 * @param <E> Exception type.
 */
public class BaseEASTVisitor<S, E extends Exception> extends BaseASTVisitor<S, E> {

    protected BaseEASTVisitor() {}

    protected BaseEASTVisitor(boolean ie) {
        super(ie);
    }

    protected BaseEASTVisitor(boolean ie, boolean p) {
        super(ie, p);
    }

    protected void printSTentry(String s) {
        System.out.println(indent + "STentry: " + s);
    }

    public S visitSTentry(STentry s) throws E {
        throw new UnimplException();
    }
}
