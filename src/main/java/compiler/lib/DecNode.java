package compiler.lib;

/** Abstract Class that represents a special node of a declaration. */
public abstract class DecNode extends Node {
    protected TypeNode type;

    public TypeNode getType() {
        return type;
    }
}
