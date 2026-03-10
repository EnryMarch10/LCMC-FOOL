package compiler.lib;

/** Interface for a visitable item of the Visitor Pattern. */
public interface Visitable {
    <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E;
}
