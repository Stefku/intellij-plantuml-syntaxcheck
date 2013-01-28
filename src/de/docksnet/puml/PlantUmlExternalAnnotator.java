package de.docksnet.puml;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import net.sourceforge.plantuml.syntax.SyntaxChecker;
import net.sourceforge.plantuml.syntax.SyntaxResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This ExternalAnnotator uses the SyntaxChecker by PlantUML to annotate PUML files
 */
public class PlantUmlExternalAnnotator extends ExternalAnnotator<String, SyntaxResult> {

    public static final int SIZE_OF_EOL_CHAR = 1;
    public static final String TAG_STARTUML = "@startuml";

    @Nullable
    @Override
    public String collectionInformation(@NotNull PsiFile file) {
        return file.getText();
    }

    @Nullable
    @Override
    public SyntaxResult doAnnotate(String collectedInfo) {
        return SyntaxChecker.checkSyntax(collectedInfo);
    }

    @Override
    public void apply(@NotNull PsiFile file, SyntaxResult annotationResult, @NotNull AnnotationHolder holder) {
        if (!annotationResult.isError()) {
            return;
        }

        String errorText = annotationResult.getErrors().iterator().next();
        if ("No @startuml found".equals(errorText)) {
            holder.createErrorAnnotation(TextRange.create(0, 1), "tag @startuml not found");
            return;
        } else if ("No @enduml found".equals(errorText)) {
            holder.createErrorAnnotation(TextRange.create(0, 1), "tag @enduml not found");
            return;
        }

        Segment errorSegment = calculateSegmentByLineOfError(file.getText(), annotationResult.getErrorLinePosition());

        String errorMessage = generateErrorMessage(annotationResult);

        Annotation annotation = holder.createErrorAnnotation(TextRange.create(errorSegment), errorMessage);
        annotation.setTooltip(errorMessage);

        for (int i = 1; i < annotationResult.getSuggest().size(); i++) {
            annotation.registerFix(new MyIntentionAction(errorSegment, annotationResult.getSuggest().get(i)));
        }
    }

    static Segment calculateSegmentByLineOfError(String fileText, int errorLinePosition) {
        PumlScanner pumlScanner;
        pumlScanner = new PumlScanner(fileText, errorLinePosition).invoke();

        int errorLineStart = pumlScanner.getErrorLineStart();
        int errorLineEnd = pumlScanner.getErrorLineEnd();

        final int finalStart = errorLineStart;
        final int finalEnd = errorLineEnd;

        return new Segment() {
            @Override
            public int getStartOffset() {
                return finalStart;
            }

            @Override
            public int getEndOffset() {
                return finalEnd;
            }
        };
    }

    private String generateErrorMessage(SyntaxResult annotationResult) {
        StringBuilder result = new StringBuilder();
        for (String error : annotationResult.getErrors()) {
            result.append(error).append('\n');
        }
        for (String suggestion : annotationResult.getSuggest()) {
            result.append(suggestion).append('\n');
        }
        return result.toString();
    }

    private static class MyIntentionAction implements IntentionAction {
        private final int startOffset;
        private final int endOffset;
        private final String suggestion;

        public MyIntentionAction(Segment errorSegment, String suggestion) {
            this.startOffset = errorSegment.getStartOffset();
            this.endOffset = errorSegment.getEndOffset();
            this.suggestion = suggestion;
        }

        @NotNull
        @Override
        public String getText() {
            return "change to '" + suggestion + "'";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return "";
        }

        @Override
        public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
            return true;
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            editor.getDocument().replaceString(startOffset, endOffset - SIZE_OF_EOL_CHAR, suggestion);
        }

        @Override
        public boolean startInWriteAction() {
            return true;
        }
    }

}
