package com.mflechl.mfchess;

public class Ply {

    public Ply() {
    }

    public Ply(int fromLine, int fromRow, int toLine, int toRow, int toPiece, boolean enPassant, boolean castlingPossKW, boolean castlingPossQW, boolean castlingPossKB, boolean castlingPossQB, int moverColor) {
        this(fromLine, fromRow, toLine, toRow, toPiece, enPassant, new boolean[]{castlingPossKW, castlingPossKB}, new boolean[]{castlingPossQW, castlingPossQB},moverColor);
    }

    public Ply(int fromLine, int fromRow, int toLine, int toRow, int toPiece, boolean enPassant, int moverColor) {
        this(fromLine, fromRow, toLine, toRow, toPiece, enPassant, new boolean[]{false, false}, new boolean[]{false, false},moverColor);
    }

    public Ply(int fromLine, int fromRow, int toLine, int toRow, int toPiece, boolean enPassant, boolean[] toggleCastlingPossK, boolean[] toggleCastlingPossQ, int moverColor) {
        this.fromLine = fromLine;
        this.fromRow = fromRow;
        this.toLine = toLine;
        this.toRow = toRow;
        this.toPiece = toPiece;
        this.toggleCastlingPossK = new boolean[]{toggleCastlingPossK[0], toggleCastlingPossK[1]};
        this.toggleCastlingPossQ = new boolean[]{toggleCastlingPossQ[0], toggleCastlingPossQ[1]};
        this.moverColor = moverColor;
        this.enPassant = enPassant;
    }

    public Ply(Ply p) {
        this.fromLine = p.fromLine;
        this.fromRow = p.fromRow;
        this.toLine = p.toLine;
        this.toRow = p.toRow;
        this.toPiece = p.toPiece;
        this.enPassant = p.enPassant;
        this.toggleCastlingPossK = new boolean[]{p.toggleCastlingPossK[0],p.toggleCastlingPossK[1]};
        this.toggleCastlingPossQ = new boolean[]{p.toggleCastlingPossQ[0],p.toggleCastlingPossQ[1]};
        this.moverColor = p.moverColor;
    }

    int fromLine;
    int fromRow;
    int toLine;
    int toRow;

    int toPiece;
    int moverColor;           //b or w

    boolean enPassant = false;

    boolean[] toggleCastlingPossK = {false, false};
    boolean[] toggleCastlingPossQ = {false, false}; //true-castling possible, false-castling not possible anymore or done, for white/black, for Queen-side


    public int getMoverColor() {
        return moverColor;
    }

    public void toggleCastlingPossK(int colIndex) {
        this.toggleCastlingPossK[colIndex] = true;
    }
    public void toggleCastlingPossQ(int colIndex) {
        this.toggleCastlingPossQ[colIndex] = true;
    }
    public boolean getToggleCastlingPossK(int colIndex) {
        return this.toggleCastlingPossK[colIndex];
    }
    public boolean getToggleCastlingPossQ(int colIndex) {
        return this.toggleCastlingPossQ[colIndex];
    }

    public int getFromLine() {
        return fromLine;
    }

    public void setFromLine(int fromLine) {
        this.fromLine = fromLine;
    }

    public int getFromRow() {
        return fromRow;
    }

    public void setFromRow(int fromRow) {
        this.fromRow = fromRow;
    }

    public int getToLine() {
        return toLine;
    }

    public void setToLine(int toLine) {
        this.toLine = toLine;
    }

    public int getToRow() {
        return toRow;
    }

    public void setToRow(int toRow) {
        this.toRow = toRow;
    }

    public void setTo(int toLine, int toRow) {
        setToLine(toLine);
        setToRow(toRow);
    }

    public int getToPiece() {
        return toPiece;
    }

    /*
    public void setToPiece(int toPiece) {
        this.toPiece = toPiece;
    }

    public int getMoverColor() {
        return moverColor;
    }

    public void setMoverColor(int moverColor) {
        this.moverColor = moverColor;
    }
    */

    public String toString() {
        return  " moverColor=" + moverColor +
                " enPassant=" + enPassant +
//                " check=" + check + " mate=" + mate + " remis=" + remis +
                " toggleCastlingPossibleQ b/w=" + toggleCastlingPossQ[0] + "/" + toggleCastlingPossQ[1] +
                " toggleCastlingPossibleK b/w=" + toggleCastlingPossK[0] + "/" + toggleCastlingPossK[1] +
                " fromLine=" + fromLine + " fromRow=" + fromRow +
                " toLine=" + toLine + " toRow=" + toRow +
                " toPiece=" + toPiece;
    }

}
