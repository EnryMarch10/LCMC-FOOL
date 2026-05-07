import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CodeGenerationTest {
    @ParameterizedTest
    @CsvSource({"integer, 4", "quicksort, 122345", "vector, 1426", "vector_equals_test, 101"
        // "bankloan, 50_000" TODO: uncomment when implementing hereditariness
    })
    void testCorrectOutput(String fileName, String expectedResult) {
        ProgramsTest.testResultFromFile(ProgramsTest.SAMPLES + fileName + ".fool", expectedResult);
    }
}
