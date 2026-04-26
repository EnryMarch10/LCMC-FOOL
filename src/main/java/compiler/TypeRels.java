package compiler;

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
     * @param a First base type.
     * @param b Second base type.
     * @return If {@code a} is subtype of {@code b}.
     */
    public static boolean isSubtype(TypeNode a, TypeNode b) {
        if ((a instanceof AST.RefTypeNode aRefType) && (b instanceof AST.RefTypeNode bRefType)) {
            return aRefType.classId.equals(bRefType.classId);
        }
        return a.getClass().equals(b.getClass())
                || ((a instanceof AST.BoolTypeNode) && (b instanceof AST.IntTypeNode))
                || ((a instanceof AST.EmptyTypeNode) && (b instanceof AST.RefTypeNode));
    }
}
