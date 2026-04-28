package compiler;

import static compiler.TypeRels.isSubtype;

import compiler.AST.*;
import compiler.exc.IncomplException;
import compiler.exc.TypeException;
import compiler.lib.BaseEASTVisitor;
import compiler.lib.Node;
import compiler.lib.TypeNode;

/**
 * Class that represents a visitor of an Enriched Abstract Syntax Tree (EAST) and performs the bottom-up Type Checking
 * of the FOOL programming language.
 *
 * <p>Performs the second step of the Checker (3-rd component of the Compiler).
 *
 * <p>The method {@code visitNode(n)} performs Type Checking on a given node {@code n}:
 *
 * <ul>
 *   <li>For expression nodes, it returns the node's type (an instance of {@link BoolTypeNode}, {@link IntTypeNode},
 *       {@link RefTypeNode} or {@link EmptyTypeNode}).
 *   <li>For declaration nodes, it returns {@code null} and checks the declaration's internal type correctness.
 *   <li>For type nodes, it returns {@code null} and verifies that the type is complete.
 * </ul>
 *
 * <p>The method {@code visitSTentry(s)} returns the type associated with the symbol table entry {@code s}.
 */
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode, TypeException> {

    public TypeCheckEASTVisitor() {
        super(true);
    } // enables incomplete tree exceptions

    public TypeCheckEASTVisitor(boolean debug) {
        super(true, debug);
    } // enables print for debugging

    // checks that a type object is visitable (not incomplete)
    private TypeNode ckvisit(TypeNode t) throws TypeException {
        visit(t);
        return t;
    }

    @Override
    public TypeNode visitNode(ProgLetInNode n) throws TypeException {
        if (print) printNode(n);
        for (Node dec : n.declist) {
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        }
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(ProgNode n) throws TypeException {
        if (print) printNode(n);
        return visit(n.exp);
    }

    /**
     * Performs type checking for a function declaration. Visits also the function declarations list and checks the
     * function return type.
     *
     * @param n the function declaration.
     * @return {@code null}.
     * @throws TypeException if a type checking error in a declaration occurs or the type object is not visitable.
     */
    @Override
    public TypeNode visitNode(FunNode n) throws TypeException {
        if (print) printNode(n, n.id);
        for (Node dec : n.declist) {
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        }
        if (!isSubtype(visit(n.exp), ckvisit(n.retType)))
            throw new TypeException("Wrong return type for function " + n.id, n.getLine());
        return null;
    }

    /**
     * Performs type checking for a variable declaration and its initialization.
     *
     * @param n the variable declaration.
     * @return {@code null}.
     * @throws TypeException if the type object is not visitable.
     */
    @Override
    public TypeNode visitNode(VarNode n) throws TypeException {
        if (print) printNode(n, n.id);
        if (!isSubtype(visit(n.exp), ckvisit(n.getType())))
            throw new TypeException("Incompatible value for variable " + n.id, n.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(PrintNode n) throws TypeException {
        if (print) printNode(n);
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(IfNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.cond), new BoolTypeNode())))
            throw new TypeException("Non boolean condition in if", n.getLine());
        TypeNode t = visit(n.th);
        TypeNode e = visit(n.el);
        if (isSubtype(t, e)) return e;
        if (isSubtype(e, t)) return t;
        throw new TypeException("Incompatible types in then-else branches", n.getLine());
    }

    @Override
    public TypeNode visitNode(EqualNode n) throws TypeException {
        if (print) printNode(n);
        TypeNode l = visit(n.left);
        TypeNode r = visit(n.right);
        if (!(isSubtype(l, r) || isSubtype(r, l))) throw new TypeException("Incompatible types in equal", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(GreaterEqualNode n) throws TypeException {
        if (print) printNode(n);
        TypeNode l = visit(n.left);
        TypeNode r = visit(n.right);
        if (!((isSubtype(l, r) || isSubtype(r, l)) && isSubtype(l, new IntTypeNode()))) {
            throw new TypeException("Incompatible types in greater equal", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(LessEqualNode n) throws TypeException {
        if (print) printNode(n);
        TypeNode l = visit(n.left);
        TypeNode r = visit(n.right);
        if (!((isSubtype(l, r) || isSubtype(r, l)) && isSubtype(l, new IntTypeNode()))) {
            throw new TypeException("Incompatible types in less equal", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(AndNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new BoolTypeNode()) && isSubtype(visit(n.right), new BoolTypeNode()))) {
            throw new TypeException("Non-boolean operands in and", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(OrNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new BoolTypeNode()) && isSubtype(visit(n.right), new BoolTypeNode()))) {
            throw new TypeException("Non-boolean operands in or", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(NotNode n) throws TypeException {
        if (print) printNode(n);
        if (!isSubtype(visit(n.exp), new BoolTypeNode())) {
            throw new TypeException("Non-boolean expression in not", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(TimesNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode()) && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in multiplication", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(DivNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode()) && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in division", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(PlusNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode()) && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in sum", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(MinusNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode()) && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in subtraction", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(CallNode n) throws TypeException {
        if (print) printNode(n, n.id);
        TypeNode t = visit(n.entry);
        if (!(t instanceof ArrowTypeNode)) throw new TypeException("Invocation of a non-function " + n.id, n.getLine());
        ArrowTypeNode at = (ArrowTypeNode) t;
        if (!(at.parlist.size() == n.arglist.size()))
            throw new TypeException("Wrong number of parameters in the invocation of " + n.id + "()", n.getLine());
        for (int i = 0; i < n.arglist.size(); i++) {
            if (!(isSubtype(visit(n.arglist.get(i)), at.parlist.get(i))))
                throw new TypeException(
                        "Wrong type for " + (i + 1) + "-th parameter in the invocation of " + n.id + "()", n.getLine());
        }
        return at.ret;
    }

    @Override
    public TypeNode visitNode(IdNode n) throws TypeException {
        if (print) printNode(n, n.id);
        TypeNode t = visit(n.entry);
        if (t instanceof ArrowTypeNode)
            throw new TypeException("Wrong usage of function identifier " + n.id, n.getLine());
        if (t instanceof ClassTypeNode) throw new TypeException("Wrong usage of class identifier " + n.id, n.getLine());
        return t;
    }

    @Override
    public TypeNode visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(ClassNode n) {
        if (print) printNode(n);
        for (MethodNode method : n.methods) {
            try {
                visit(method);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a method declaration: " + e.text);
            }
        }
        return null;
    }

    @Override
    public TypeNode visitNode(MethodNode n) throws TypeException {
        if (print) printNode(n, n.id);
        for (Node dec : n.decs) {
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        }
        if (!isSubtype(visit(n.exp), ckvisit(n.returnType)))
            throw new TypeException("Wrong return type for method " + n.id, n.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(ClassCallNode n) throws TypeException {
        if (print) printNode(n, n.refId + "." + n.methodId);
        TypeNode methodType = visit(n.methodEntry);
        if (!(methodType instanceof ArrowTypeNode arrowMethodType))
            throw new TypeException("Invocation of a non-method " + n.methodId, n.getLine());
        if (arrowMethodType.parlist.size() != n.args.size())
            throw new TypeException(
                    "Wrong number of arguments in the invocation of " + n.refId + "." + n.methodId + "()", n.getLine());
        for (int i = 0; i < n.args.size(); i++) {
            if (!isSubtype(visit(n.args.get(i)), arrowMethodType.parlist.get(i)))
                throw new TypeException(
                        "Wrong type for " + (i + 1) + "-th parameter in the invocation of " + n.refId + "." + n.methodId
                                + "()",
                        n.getLine());
        }
        return arrowMethodType.ret;
    }

    @Override
    public TypeNode visitNode(NewNode n) throws TypeException {
        if (print) printNode(n, n.id);
        TypeNode t = visit(n.entry);
        if (!(t instanceof ClassTypeNode classType))
            throw new TypeException("Instantiation of a non-class " + n.id, n.getLine());
        if (classType.fields.size() != n.args.size())
            throw new TypeException(
                    "Wrong number of arguments in the invocation of the constructor of " + n.id, n.getLine());
        for (int i = 0; i < n.args.size(); i++) {
            if (!isSubtype(visit(n.args.get(i)), classType.fields.get(i)))
                throw new TypeException(
                        "Wrong type for " + (i + 1) + "-th argument in the invocation of the constructor of " + n.id,
                        n.getLine());
        }
        return new RefTypeNode(n.id);
    }

    @Override
    public TypeNode visitNode(EmptyNode n) {
        if (print) printNode(n);
        return new EmptyTypeNode();
    }

    // incomplete types management (if they are incomplete throws exception)

    /**
     * Checks whether the class type is incomplete or not. Visits the type of every field and every method in the class.
     *
     * @param n the class type.
     * @return {@code null}.
     * @throws TypeException if the type is incomplete.
     */
    @Override
    public TypeNode visitNode(ClassTypeNode n) throws TypeException {
        if (print) printNode(n);
        for (Node field : n.fields) visit(field);
        for (ArrowTypeNode method : n.methods) visit(method);
        return null;
    }

    /**
     * Checks if the function type is not incomplete. Visits every parameter type of the function and the function
     * return type.
     *
     * @param n the function type.
     * @return {@code null}.
     * @throws TypeException if the type is incomplete.
     */
    @Override
    public TypeNode visitNode(ArrowTypeNode n) throws TypeException {
        if (print) printNode(n);
        for (Node par : n.parlist) visit(par);
        visit(n.ret, "->"); // marks return type
        return null;
    }

    /**
     * Checks if the bool type is not incomplete. In this case it does nothing, it only prints if necessary.
     *
     * @param n the bool type.
     * @return {@code null}.
     */
    @Override
    public TypeNode visitNode(BoolTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    /**
     * Checks if the int type is not incomplete. In this case it does nothing, it only prints if necessary.
     *
     * @param n the int type.
     * @return {@code null}.
     */
    @Override
    public TypeNode visitNode(IntTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public TypeNode visitNode(RefTypeNode n) {
        if (print) printNode(n, n.classId);
        return null;
    }

    @Override
    public TypeNode visitNode(EmptyTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    // STentry (returns type field)

    /**
     * Visits the Symbol Table Entry and retrieves its type field.
     *
     * @param entry the Symbol Table Entry.
     * @return the type contained in the {@link STentry}.
     * @throws TypeException if a type error occurs.
     */
    @Override
    public TypeNode visitSTentry(STentry entry) throws TypeException {
        if (print) printSTentry("type");
        return ckvisit(entry.type);
    }
}
