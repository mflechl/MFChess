package com.mflechl.mfchess;

public class Ply {

    public Ply() {
    }

    public Ply(int fromLine, int fromRow, int toLine, int toRow, int toPiece, boolean enPassant, boolean castlingPossKW, boolean castlingPossQW, boolean castlingPossKB, boolean castlingPossQB, int moverColor) {
        this(fromLine, fromRow, toLine, toRow, toPiece, enPassant, new boolean[]{castlingPossKW, castlingPossKB}, new boolean[]{castlingPossQW, castlingPossQB},moverColor);
    }

    public Ply(int fromLine, int fromRow, int toLine, int toRow, int toPiece, boolean enPassant, boolean[] castlingPossK, boolean[] castlingPossQ, int moverColor) {
        this.fromLine = fromLine;
        this.fromRow = fromRow;
        this.toLine = toLine;
        this.toRow = toRow;
        this.toPiece = toPiece;
        this.castlingPossK = new boolean[]{castlingPossK[0],castlingPossK[1]};
        this.castlingPossQ = new boolean[]{castlingPossQ[0],castlingPossQ[1]};
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
        this.castlingPossK = new boolean[]{p.castlingPossK[0],p.castlingPossK[1]};
        this.castlingPossQ = new boolean[]{p.castlingPossQ[0],p.castlingPossQ[1]};
        this.moverColor = p.moverColor;
    }

    int fromLine;
    int fromRow;
    int toLine;
    int toRow;

    int toPiece;
    int moverColor;           //b or w

    boolean enPassant = false;
    boolean[] castlingPossK = {true, true};
    boolean[] castlingPossQ = {true, true};
    ;  //true-castling possible, false-castling not possible anymore or done, for white/black, for Queen-side

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

    public void setToPiece(int toPiece) {
        this.toPiece = toPiece;
    }

    public int getMoverColor() {
        return moverColor;
    }

    public void setMoverColor(int moverColor) {
        this.moverColor = moverColor;
    }

    public void setCastlingColIndex(int colIndex, boolean castlingPossible){
        castlingPossK[colIndex]=castlingPossible;
        castlingPossQ[colIndex]=castlingPossible;
    }

}
