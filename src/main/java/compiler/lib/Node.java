package compiler.lib;

/** Abstract Class that represents a node of a visitable (Visitor Pattern) tree. */
public abstract class Node implements Visitable {
    int line = -1; // line -1 means unset

    public int getLine() {
        return line;
    }

    public void setLine(final int l) {
        line = l;
    }
}
