package compiler.exc;

import compiler.lib.FOOLlib;
import java.io.Serial;

public class TypeException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public String text;

    public TypeException(final String t, final int line) {
        FOOLlib.typeErrors++;
        text = t + " at line " + line;
    }
}
