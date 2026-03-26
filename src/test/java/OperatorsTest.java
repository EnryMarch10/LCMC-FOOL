import static org.junit.jupiter.api.Assertions.assertEquals;

import compiler.lib.FOOLlib;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class OperatorsTest {
    private static final String OPS = "sample_programs/tests/ops/";
    private static final String OPS_LE_OR_DIV_EQ = OPS + "le-or-div-not/";

    @BeforeEach
    void init() { // LCMC compiler is though to run on a new JVM every time
        FOOLlib.typeErrors = 0;
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
    void testOr() {
        ProgramsTest.testResultTrueFromFile(OPS_LE_OR_DIV_EQ + "or.fool");
    }

    @Test
    void testDiv() {
        ProgramsTest.testResultTrueFromFile(OPS_LE_OR_DIV_EQ + "div.fool");
        ProgramsTest.testResultTrueFromFile(OPS_LE_OR_DIV_EQ + "div-bool.fool");
        assertEquals(1, ProgramsTest.getTypeErrors(ProgramsTest.fromFile(OPS_LE_OR_DIV_EQ + "div-by0.fool")));
    }

    @Test
    void testNot() {
        assertEquals(1, ProgramsTest.getSyntaxErrors(ProgramsTest.fromFile(OPS_LE_OR_DIV_EQ + "not.fool")));
    }

    @Test
    void testLeOrDivNot() {
        ProgramsTest.testResultFromFile(OPS_LE_OR_DIV_EQ + "all.fool", "2");
    }
}
