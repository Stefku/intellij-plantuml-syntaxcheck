package de.docksnet.puml;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import net.sourceforge.plantuml.syntax.SyntaxResult;
import org.junit.Test;
import org.mockito.Matchers;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlantUmlExternalAnnotatorIT {

    private String source;
    private AnnotationHolder holder;
    private PsiFile psiFile;
    private PlantUmlExternalAnnotator annotator;

    public void setUpForSource(String source) {
        this.source = source;
        holder = mock(AnnotationHolder.class);
        when(holder.createErrorAnnotation(Matchers.<TextRange>any(), anyString())).thenReturn(new Annotation(0, 0,
                HighlightSeverity.ERROR, null, null));

        psiFile = mock(PsiFile.class);
        when(psiFile.getText()).thenReturn(source);
        annotator = new PlantUmlExternalAnnotator();
    }

    @Test
    public void applyWithErrorMissingStartumlTag() throws Exception {
        // given
        setUpForSource("\nfoo\nerror\n@enduml\n");

        // when
        SyntaxResult syntaxResult = annotator.doAnnotate(source);
        annotator.apply(psiFile, syntaxResult, holder);

        // then
        verify(holder).createErrorAnnotation(TextRange.create(0, 1), "tag @startuml not found");
    }

    @Test
    public void applyWithErrorMissingEndumlTag() throws Exception {
        // given
        setUpForSource("@startuml\nactor User\n");

        // when
        SyntaxResult syntaxResult = annotator.doAnnotate(source);
        annotator.apply(psiFile, syntaxResult, holder);

        // then
        verify(holder).createErrorAnnotation(TextRange.create(0, 1), "tag @enduml not found");
    }
}
