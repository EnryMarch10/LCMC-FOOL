package compiler;

import compiler.AST.BoolTypeNode;
import compiler.AST.IntTypeNode;
import compiler.lib.TypeNode;

/**
 * Class that verifies relationships between types of the FOOL programming language.
 *
 * <p>Type relationships are necessary to apply the <b>Liskov substitution principle</b> (subtypes can be used in place
 * of supertypes) and to verify the <b>soundness</b> of the Type Checking System.
 */
public class TypeRels {
    /**
     * Evaluates if {@code a} is subtype of {@code b}.
     *
     * @param a First base type ({@link IntTypeNode} or {@link BoolTypeNode}).
     * @param b Second base type ({@link IntTypeNode} or {@link BoolTypeNode}).
     * @return If {@code a} is subtype of {@code b}.
     */
    public static boolean isSubtype(TypeNode a, TypeNode b) {
        return a.getClass().equals(b.getClass()) || ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode));
    }
}
