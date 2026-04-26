import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeCheckingTest {
    private static final String TYPE_CHECKING = ProgramsTest.SAMPLES + "tests/type_checking/";

    @Test
    void testWrongMethodReturnType() {
        assertEquals(1,
            ProgramsTest.getTypeErrors(ProgramsTest.fromFile(TYPE_CHECKING + "vector_wrong_method_return_type.fool")));
    }

    @Test
    void testWrongArgNumInMethodCall() {
        assertEquals(1,
            ProgramsTest.getTypeErrors(ProgramsTest.fromFile(TYPE_CHECKING + "vector_wrong_arg_num_method_call.fool")));
    }

    @Test
    void testWrongArgTypeInMethodCall() {
        assertEquals(1,
            ProgramsTest.getTypeErrors(ProgramsTest.fromFile(TYPE_CHECKING + "vector_wrong_arg_type_method_call.fool")));
    }

    @Test
    void testWrongArgNumInNew() {
        assertEquals(1,
            ProgramsTest.getTypeErrors(ProgramsTest.fromFile(TYPE_CHECKING + "vector_wrong_arg_num_new.fool")));
    }

    @Test
    void testWrongArgTypeInNew() {
        assertEquals(1,
            ProgramsTest.getTypeErrors(ProgramsTest.fromFile(TYPE_CHECKING + "vector_wrong_arg_type_new.fool")));
    }

    @Test
    void testFieldInvocationOutsideClass() {
        assertEquals(1,
            ProgramsTest.getTypeErrors(ProgramsTest.fromFile(TYPE_CHECKING + "vector_field_invocation_outside_class.fool")));
    }

    @Test
    void testIdIsAClassName() {
        assertEquals(1,
            ProgramsTest.getTypeErrors(ProgramsTest.fromFile(TYPE_CHECKING + "vector_id_is_a_class_name.fool")));
    }
}
