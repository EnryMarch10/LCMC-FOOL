package compiler;

import compiler.lib.BaseASTVisitor;
import compiler.lib.BaseEASTVisitor;
import compiler.lib.TypeNode;
import compiler.lib.Visitable;

/** Class that represents a Symbol Table Entry, used to Enrich the Abstract Syntax Tree. */
public class STentry implements Visitable {
    final int nl;
    final TypeNode type;
    final int offset;

    public STentry(final int n, final TypeNode t, final int o) {
        nl = n;
        type = t;
        offset = o;
    }

    @Override
    public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
        return ((BaseEASTVisitor<S, E>) visitor).visitSTentry(this);
    }
}
