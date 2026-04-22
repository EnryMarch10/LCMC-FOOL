import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class STTest {
    private static final String ST = "sample_programs/tests/st/";

    @Test
    void testMultiplyDeclaredFun() {
        assertEquals(1,
            ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "prova1_multiply_declared_fun.fool")));
    }

    @Test
    void testMultiplyDeclaredPar() {
        assertEquals(1,
            ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "prova1_multiply_declared_par.fool")));
    }

    @Test
    void testMultiplyDeclaredVar() {
        assertEquals(1,
            ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "prova1_multiply_declared_var.fool")));
    }

    @Test
    void testUndeclaredFun() {
        assertEquals(1,
            ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "prova1_undeclared_fun.fool")));
    }

    @Test
    void testUndeclaredVar() {
        assertEquals(2,
            ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "prova1_undeclared_var.fool")));
    }

    @Test
    void testMultiplyDeclaredClass() {
        // TODO: test
    }
}
