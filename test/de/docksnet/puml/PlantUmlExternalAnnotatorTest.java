package de.docksnet.puml;

import com.intellij.openapi.util.Segment;
import org.junit.Test;

import static de.docksnet.puml.PlantUmlExternalAnnotator.*;
import static org.junit.Assert.assertEquals;

public class PlantUmlExternalAnnotatorTest {
    @Test
    public void test() throws Exception {
        // given
        String source = "\nfoo\n@startuml\nerror\n@enduml\n";

        // when
        Segment segment = calculateSegmentByLineOfError(source, 1);

        // then
        assertEquals(15, segment.getStartOffset());
        assertEquals(15 + "error\n".length(), segment.getEndOffset());
    }
}
