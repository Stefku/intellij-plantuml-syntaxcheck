package extern.plantuml;

import net.sourceforge.plantuml.syntax.SyntaxChecker;
import net.sourceforge.plantuml.syntax.SyntaxResult;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class is a friendly unit test to check the SyntaxChecker by plantuml
 */
public class SyntaxCheckerTest {

    @Test
    public void noError() throws Exception {
        // given
        String source = "@startuml\n" +
                "actor User\n" +
                "@enduml\n";

        // when
        SyntaxResult syntaxResult = SyntaxChecker.checkSyntax(source);

        // then
        assertFalse(syntaxResult.isError());
    }

    @Test
    public void isErrorWhenOmitStartTag() throws Exception {
        // given
        String source = "\n" +
                "actor User\n" +
                "@enduml\n";

        // when
        SyntaxResult syntaxResult = SyntaxChecker.checkSyntax(source);

        // then
        assertTrue(syntaxResult.isError());
    }

    @Test
    public void isErrorWhenOmitEndTag() throws Exception {
        // given
        String source = "@startuml\n" +
                "actor User\n" +
                "\n";

        // when
        SyntaxResult syntaxResult = SyntaxChecker.checkSyntax(source);

        // then
        assertTrue(syntaxResult.isError());
    }

}
