package compiler.lib;

/** Class that manages utility methods for the FOOL programming language. */
public class FOOLlib {
    public static int typeErrors = 0;
    private static int labCount = 0;
    private static int funlabCount = 0;
    private static String funCode = null;

    /**
     * Extracts name from {@code Node} class.
     *
     * @param s is in the form {@code compiler.AST$NameNode}.
     * @return Extracts {@code Name} from {@code compiler.AST$NameNode}.
     */
    public static String extractNodeName(String s) { // s is in the form compiler.AST$NameNode
        return s.substring(s.lastIndexOf('$') + 1, s.length() - 4);
    }

    /**
     * Extracts name from {@code TypeNode} class.
     *
     * @param s is in the form {@code compiler.AST$NameTypeNode}.
     * @return Extracts {@code Name} from {@code compiler.AST$NameTypeNode}.
     */
    public static String extractTypeNodeName(String s) { // s is in the form compiler.AST$NameTypeNode
        return s.substring(s.lastIndexOf('$') + 1, s.length() - 8);
    }

    /**
     * Extracts name from {@code Context} class.
     *
     * @param s is in the form {@code compiler.FOOLParser$NameContext}.
     * @return Extracts {@code Name} from {@code compiler.FOOLParser$NameContext}.
     */
    public static String extractCtxName(String s) { // s is in the form compiler.FOOLParser$NameContext
        return s.substring(s.lastIndexOf('$') + 1, s.length() - 7);
    }

    public static String lowerizeFirstChar(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Joins a sequence of strings using newline {@code "\n"} separator. Ignores {@code null} arguments.
     *
     * @param lines the sequence of strings.
     * @return the joined string.
     */
    public static String nlJoin(String... lines) {
        String code = null;
        for (String line : lines) {
            if (line != null) {
                code = (code == null ? "" : code + "\n") + line;
            }
        }
        return code;
    }

    public static String freshLabel() {
        return "label" + (labCount++);
    }

    public static String freshFunLabel() {
        return "function" + (funlabCount++);
    }

    /**
     * Puts an empty line at the beginning of the function code string.
     *
     * @param c the function code string.
     */
    public static void putCode(String c) {
        funCode = nlJoin(funCode, "", c);
    }

    /**
     * Getter for the function code string.
     *
     * @return the function code string set with {@link #putCode(String)}.
     */
    public static String getCode() {
        return funCode;
    }

    public static void reset() {
        typeErrors = 0;
        labCount = 0;
        funlabCount = 0;
        funCode = null;
    }
}
