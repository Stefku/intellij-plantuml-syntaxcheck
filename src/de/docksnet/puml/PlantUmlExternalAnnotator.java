package de.docksnet.puml;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
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
        int linePosition = annotationResult.getErrorLinePosition();
        String[] split = file.getText().split("\n");
        int start = 0;
        for (int i = 0; i < linePosition; i++) {
            String line = split[i];
            start += line.length() + SIZE_OF_EOL_CHAR;
        }
        int end = start + split[linePosition].length() + SIZE_OF_EOL_CHAR;
        String errorText = generateErrorMessage(annotationResult);
        Annotation annotation = holder.createErrorAnnotation(TextRange.create(start, end), errorText);
        annotation.setTooltip(errorText);

        for (int i = 1; i < annotationResult.getSuggest().size(); i++) {
            annotation.registerFix(new MyIntentionAction(start, end, annotationResult.getSuggest().get(i)));
        }
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

        private final int start;
        private final int end;
        private final String suggestion;

        public MyIntentionAction(int start, int end, String suggestion) {
            this.start = start;
            this.end = end;
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
            editor.getDocument().replaceString(start, end - SIZE_OF_EOL_CHAR, suggestion);
        }

        @Override
        public boolean startInWriteAction() {
            return true;
        }
    }
}
