package compiler;

import compiler.AST.BoolTypeNode;
import compiler.AST.EmptyTypeNode;
import compiler.AST.IntTypeNode;
import compiler.AST.RefTypeNode;
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
     * @param a First base type ({@link IntTypeNode}, {@link BoolTypeNode}, {@link RefTypeNode} or
     *     {@link EmptyTypeNode}).
     * @param b Second base type ({@link IntTypeNode}, {@link BoolTypeNode}, {@link RefTypeNode} or
     *     {@link EmptyTypeNode}).
     * @return Whether {@code a} is subtype of {@code b}.
     */
    public static boolean isSubtype(TypeNode a, TypeNode b) {
        // We suppose that EmptyTypeNode is a subtype of itself (null is subtype of null)
        return (!(a instanceof RefTypeNode) && a.getClass().equals(b.getClass()))
                || ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode))
                || ((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode))
                // This check assumes that, if "a" and "b" are both RefTypeNode with the same class ID, then "a"
                // is a subtype of "b" (Vector is a subtype of Vector).
                || (((a instanceof RefTypeNode refA) && (b instanceof RefTypeNode refB))
                        && refA.classId.equals(refB.classId));
    }
}
