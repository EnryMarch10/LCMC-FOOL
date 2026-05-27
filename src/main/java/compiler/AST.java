package compiler;

import compiler.lib.BaseASTVisitor;
import compiler.lib.DecNode;
import compiler.lib.Node;
import compiler.lib.TypeNode;
import java.util.Collections;
import java.util.List;

/**
 * Class that represents an Abstract Syntax Tree (AST) for the FOOL programming language, defining its various
 * <b>node</b> types and their structure.
 *
 * <p>Each static nested class represents a node of the AST, ordered from root to leaves.
 */
public class AST {
    public static class ProgLetInNode extends Node {
        final List<DecNode> declist;
        final Node exp;

        ProgLetInNode(final List<DecNode> d, final Node e) {
            declist = Collections.unmodifiableList(d);
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ProgNode extends Node {
        final Node exp;

        ProgNode(final Node e) {
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class FunNode extends DecNode {
        final String id;
        final TypeNode retType;
        final List<ParNode> parlist;
        final List<DecNode> declist;
        final Node exp;

        FunNode(final String i, final TypeNode rt, final List<ParNode> pl, final List<DecNode> dl, final Node e) {
            id = i;
            retType = rt;
            parlist = Collections.unmodifiableList(pl);
            declist = Collections.unmodifiableList(dl);
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ParNode extends DecNode {
        final String id;

        ParNode(final String i, final TypeNode t) {
            id = i;
            type = t;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class VarNode extends DecNode {
        final String id;
        final Node exp;

        VarNode(final String i, final TypeNode t, final Node v) {
            id = i;
            type = t;
            exp = v;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class PrintNode extends Node {
        final Node exp;

        PrintNode(final Node e) {
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IfNode extends Node {
        final Node cond;
        final Node th;
        final Node el;

        IfNode(final Node c, final Node t, final Node e) {
            cond = c;
            th = t;
            el = e;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class EqualNode extends Node {
        final Node left;
        final Node right;

        EqualNode(final Node l, final Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class GreaterEqualNode extends Node {
        final Node left;
        final Node right;

        GreaterEqualNode(final Node l, final Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class LessEqualNode extends Node {
        final Node left;
        final Node right;

        LessEqualNode(final Node l, final Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class AndNode extends Node {
        final Node left;
        final Node right;

        AndNode(final Node l, final Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class OrNode extends Node {
        final Node left;
        final Node right;

        OrNode(final Node l, final Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class NotNode extends Node {
        final Node exp;

        NotNode(final Node e) {
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class TimesNode extends Node {
        final Node left;
        final Node right;

        TimesNode(final Node l, final Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class DivNode extends Node {
        final Node left;
        final Node right;

        DivNode(final Node l, final Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class PlusNode extends Node {
        final Node left;
        final Node right;

        PlusNode(final Node l, final Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class MinusNode extends Node {
        final Node left;
        final Node right;

        MinusNode(final Node l, final Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class CallNode extends Node {
        final String id;
        final List<Node> arglist;
        STentry entry;
        int nl;

        CallNode(final String i, final List<Node> p) {
            id = i;
            arglist = Collections.unmodifiableList(p);
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IdNode extends Node {
        final String id;
        STentry entry;
        int nl;

        IdNode(final String i) {
            id = i;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class BoolNode extends Node {
        final Boolean val;

        BoolNode(final boolean n) {
            val = n;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IntNode extends Node {
        final Integer val;

        IntNode(final Integer n) {
            val = n;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ArrowTypeNode extends TypeNode {
        final List<TypeNode> parlist;
        final TypeNode ret;

        ArrowTypeNode(final List<TypeNode> p, final TypeNode r) {
            parlist = Collections.unmodifiableList(p);
            ret = r;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class BoolTypeNode extends TypeNode {
        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IntTypeNode extends TypeNode {
        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ClassNode extends DecNode {
        final String id;
        final List<FieldNode> fields;
        final List<MethodNode> methods;

        ClassNode(final String id, final List<FieldNode> fields, final List<MethodNode> methods) {
            this.id = id;
            this.fields = Collections.unmodifiableList(fields);
            this.methods = Collections.unmodifiableList(methods);
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class FieldNode extends DecNode {
        final String id;

        FieldNode(final String id, final TypeNode type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class MethodNode extends DecNode {
        final String id;
        final TypeNode returnType;
        final List<ParNode> pars;
        final List<DecNode> decs;
        final Node exp;
        int offset;
        String label;

        MethodNode(
                final String id,
                final TypeNode returnType,
                final List<ParNode> pars,
                final List<DecNode> decs,
                final Node exp) {
            this.id = id;
            this.returnType = returnType;
            this.pars = Collections.unmodifiableList(pars);
            this.decs = Collections.unmodifiableList(decs);
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ClassCallNode extends Node {
        final String refId;
        final String methodId;
        final List<Node> args;
        STentry refEntry;
        STentry methodEntry;
        int nl;

        ClassCallNode(final String refId, final String methodId, final List<Node> args) {
            this.refId = refId;
            this.methodId = methodId;
            this.args = Collections.unmodifiableList(args);
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class NewNode extends Node {
        final String id;
        final List<Node> args;
        STentry entry;
        int nl; // NOTE: Not used during code generation.
        // The class's declaration is taken directly from MEMSIZE + n.entry.offset, so this field is never accessed.
        // However, it might be useful in case of nested classes in the future.

        NewNode(final String id, final List<Node> args) {
            this.id = id;
            this.args = Collections.unmodifiableList(args);
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class EmptyNode extends Node {
        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ClassTypeNode extends TypeNode {
        final List<TypeNode> fields;
        final List<ArrowTypeNode> methods;

        ClassTypeNode(final List<TypeNode> fields, final List<ArrowTypeNode> methods) {
            this.fields = fields;
            this.methods = methods;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class RefTypeNode extends TypeNode {
        final String classId;

        RefTypeNode(final String classId) {
            this.classId = classId;
        }

        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class EmptyTypeNode extends TypeNode {
        @Override
        public <S, E extends Exception> S accept(final BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }
}
