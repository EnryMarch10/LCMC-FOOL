import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class STTest {
    private static final String ST = "sample_programs/tests/st/";

    @Test
    void testMultiplyDeclaredFun() {
        assertEquals(
                1, ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "prova1_multiply_declared_fun.fool")));
    }

    @Test
    void testMultiplyDeclaredPar() {
        assertEquals(
                1, ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "prova1_multiply_declared_par.fool")));
    }

    @Test
    void testMultiplyDeclaredVar() {
        assertEquals(
                1, ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "prova1_multiply_declared_var.fool")));
    }

    @Test
    void testUndeclaredFun() {
        assertEquals(1, ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "prova1_undeclared_fun.fool")));
    }

    @Test
    void testUndeclaredVar() {
        assertEquals(2, ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "prova1_undeclared_var.fool")));
    }

    @Test
    void testMultiplyDeclaredClass() {
        assertEquals(
                4,
                ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "vector_multiply_declared_class.fool")));
    }

    @Test
    void testMultiplyDeclaredField() {
        assertEquals(
                1,
                ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "vector_multiply_declared_field.fool")));
    }

    @Test
    void testMultiplyDeclaredMethod() {
        assertEquals(
                1,
                ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "vector_multiply_declared_method.fool")));
    }

    @Test
    void testMultiplyDeclaredMethodParAndReferenceIdIsNotAReferenceType() {
        assertEquals(
                3,
                ProgramsTest.getSymbolTableErrors(
                        ProgramsTest.fromFile(ST + "vector_multiply_declared_method_par.fool")));
    }

    @Test
    void testUndeclaredReferenceId() {
        assertEquals(
                1,
                ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "vector_undeclared_reference_id.fool")));
    }

    @Test
    void testUndeclaredMethod() {
        assertEquals(2, ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "vector_undeclared_method.fool")));
    }

    @Test
    void testUndeclaredClass() {
        assertEquals(5, ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(ST + "vector_undeclared_class.fool")));
    }
}
