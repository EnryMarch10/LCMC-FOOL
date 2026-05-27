import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CodeGenerationTest {
    @ParameterizedTest
    @CsvSource({"integer, 4", "quicksort, 122345", "vector, 1426", "vector_equals_test, 101"
        // NOTE: Uncomment when implementing hereditariness
        // "bankloan, 50_000"
    })
    void testCorrectOutput(final String fileName, final String expectedResult) {
        ProgramsTest.testResultFromFile(ProgramsTest.SAMPLES + fileName + ".fool", expectedResult);
    }
}
