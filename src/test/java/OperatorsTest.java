import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;

public class OperatorsTest {
    private static final String OPS = ProgramsTest.SAMPLES + "tests/operators/";
    private static final String OPS_LE_OR_DIV_EQ = OPS + "le-or-div-not/";
    private static final String OPS_GE_AND_MINUS_NOT = OPS + "ge-and-minus-not/";

    @Test
    void testGe() {
        ProgramsTest.testResultFromFile(OPS_GE_AND_MINUS_NOT + "ge.fool", "1100");
    }

    @Test
    void testLe() {
        ProgramsTest.testResultFalseFromString("print(if (10 <= 9) then { true } else { false });");
        ProgramsTest.testResultTrueFromString("print(if (7 <= 9) then { true } else { false });");
        ProgramsTest.testResultTrueFromString("print(if (4 <= 5) then { 7 <= 7 } else { false });");
        ProgramsTest.testResultFromString("print(if (true <= 5) then { 5 } else { false });", "5");
        ProgramsTest.testResultFalseFromFile(OPS_LE_OR_DIV_EQ + "le.fool");
    }

    @Test
    void testAnd() {
        ProgramsTest.testResultFromFile(OPS_GE_AND_MINUS_NOT + "and.fool", "1000");
        ProgramsTest.testResultFromFile(OPS_GE_AND_MINUS_NOT + "and_typing.fool", "5");
        assertEquals(
                1, // Should be 2, but the compiler only finds the first error
                ProgramsTest.getTypeErrors(ProgramsTest.fromFile(OPS_GE_AND_MINUS_NOT + "and_wrong_typing.fool")));
    }

    @Test
    void testOr() {
        ProgramsTest.testResultTrueFromFile(OPS_LE_OR_DIV_EQ + "or.fool");
    }

    @Test
    void testDiv() {
        ProgramsTest.testResultTrueFromFile(OPS_LE_OR_DIV_EQ + "div.fool");
        ProgramsTest.testResultTrueFromFile(OPS_LE_OR_DIV_EQ + "div-bool.fool");
        assertThrows(
                ArithmeticException.class,
                () -> ProgramsTest.testResultTrueFromFile(OPS_LE_OR_DIV_EQ + "div-by0.fool"));
    }

    @Test
    void testMinus() {
        ProgramsTest.testResultFromFile(OPS_GE_AND_MINUS_NOT + "minus.fool", "2-60");
    }

    @Test
    void testNot() {
        assertEquals(1, ProgramsTest.getSymbolTableErrors(ProgramsTest.fromFile(OPS_LE_OR_DIV_EQ + "not.fool")));
        ProgramsTest.testResultFromFile(OPS_GE_AND_MINUS_NOT + "not.fool", "01");
        assertEquals(
                1, ProgramsTest.getTypeErrors(ProgramsTest.fromFile(OPS_GE_AND_MINUS_NOT + "not_wrong_typing.fool")));
    }

    @Test
    void testLeOrDivNot() {
        ProgramsTest.testResultFromFile(OPS_LE_OR_DIV_EQ + "all.fool", "2");
    }
}
