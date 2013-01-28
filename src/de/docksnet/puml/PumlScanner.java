package de.docksnet.puml;

import java.util.Scanner;

class PumlScanner {
    private final String fileText;
    private final int errorLinePosition;
    private int errorLineStart;
    private int errorLineEnd;

    public PumlScanner(String fileText, int errorLinePosition) {
        this.fileText = fileText;
        this.errorLinePosition = errorLinePosition;
    }

    public int getErrorLineStart() {
        return errorLineStart;
    }

    public int getErrorLineEnd() {
        return errorLineEnd;
    }

    public PumlScanner invoke() {
        Scanner scanner = new Scanner(fileText);

        errorLineStart = -1;
        errorLineEnd = -1;

        State state = State.BEFORE_START;
        int charCountToCurrentLine = 0;
        int currentLineNumber = 0;
        int lineWithTagStartuml = -1;
        while (scanner.hasNext()) {
            String currentLine = scanner.nextLine();
            int currentLineLength = currentLine.length() + PlantUmlExternalAnnotator.SIZE_OF_EOL_CHAR;
            if (state != State.WITHIN_START && currentLine.startsWith(PlantUmlExternalAnnotator.TAG_STARTUML)) {
                state = State.WITHIN_START;
                lineWithTagStartuml = currentLineNumber;
            }
            // do something
            if (state == State.WITHIN_START) {
                if (lineWithTagStartuml + errorLinePosition == currentLineNumber) {
                    errorLineStart = charCountToCurrentLine;
                    errorLineEnd = errorLineStart + currentLineLength;
                    break;
                }
            }
            charCountToCurrentLine += currentLineLength;
            currentLineNumber += 1;
        }

        if (errorLineStart == -1) {
            throw new IllegalStateException("Could not calculate error line");
        }
        return this;
    }

    static enum State {BEFORE_START, WITHIN_START}
}
