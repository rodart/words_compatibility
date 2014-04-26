package org.rodart.minhash;

public class SkipCurrentBookException extends Exception {
    public SkipType getType() {
        return type;
    }

    private SkipType type;

    public enum SkipType {
        PROCESS_NEXT_BOOK,
        PROCESS_NEXT_ZIP,
        FINISH_PROCESSING
    }

    public SkipCurrentBookException(SkipType type) {
        this.type = type;
    }
}
