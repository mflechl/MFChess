package com.mflechl.mfchess;

public class Ply {

    public Ply() {
    }

    /*
    public Ply(int fromLine, int fromRow, int toLine, int toRow, int toPiece, boolean enPassant, boolean toggleCastlingPossKW,
               boolean toggleCastlingPossQW, boolean toggleCastlingPossKB, boolean toggleCastlingPossQB, boolean togglePromotion,
               int moverColor, int prevEnpassantPossible) {
        this(fromLine, fromRow, toLine, toRow, toPiece, enPassant, new boolean[]{toggleCastlingPossKW, toggleCastlingPossKB},
                new boolean[]{toggleCastlingPossQW, toggleCastlingPossQB},togglePromotion, moverColor,prevEnpassantPossible);
    }
    */

    public Ply(int fromLine, int fromRow, int toLine, int toRow, int toPiece, boolean enPassant, int moverColor, int prevEnpassantPossible) {
        this(fromLine, fromRow, toLine, toRow, toPiece, enPassant,
                new boolean[]{false, false}, new boolean[]{false, false}, false, moverColor, prevEnpassantPossible);
    }

    public Ply(int fromLine, int fromRow, int toLine, int toRow, int toPiece, boolean enPassant, boolean[] toggleCastlingPossK,
               boolean[] toggleCastlingPossQ, boolean togglePromotion, int moverColor, int prevEnpassantPossible) {
        this.fromLine = fromLine;
        this.fromRow = fromRow;
        this.toLine = toLine;
        this.toRow = toRow;
        this.toPiece = toPiece;
        this.toggleCastlingPossK = new boolean[]{toggleCastlingPossK[0], toggleCastlingPossK[1]};
        this.toggleCastlingPossQ = new boolean[]{toggleCastlingPossQ[0], toggleCastlingPossQ[1]};
        this.togglePromotion = togglePromotion;
        this.moverColor = moverColor;
        this.enPassant = enPassant;
        this.prevEnpassantPossible = prevEnpassantPossible;
    }

    public Ply(Ply p) {
        this.fromLine = p.fromLine;
        this.fromRow = p.fromRow;
        this.toLine = p.toLine;
        this.toRow = p.toRow;
        this.toPiece = p.toPiece;
        this.moverColor = p.moverColor;
        this.prevEnpassantPossible = p.prevEnpassantPossible;
        this.enPassant = p.enPassant;
        this.toggleCastlingPossK = new boolean[]{p.toggleCastlingPossK[0],p.toggleCastlingPossK[1]};
        this.toggleCastlingPossQ = new boolean[]{p.toggleCastlingPossQ[0],p.toggleCastlingPossQ[1]};
        this.togglePromotion = p.togglePromotion;
    }

    int fromLine=-1;
    int fromRow=-1;
    int toLine=-1;
    int toRow=-1;

    int toPiece;
    int moverColor;           //b or w

    int prevEnpassantPossible = -11;

    boolean enPassant; // = false (default)

    boolean[] toggleCastlingPossK = {false, false};
    boolean[] toggleCastlingPossQ = {false, false}; //true-castling possible, false-castling not possible anymore or done, for white/black, for Queen-side


    boolean togglePromotion; // = false (default);

    public int getMoverColor() {
        return moverColor;
    }

    /*
    public boolean isTogglePromotion() {
        return togglePromotion;
    }
    */

    public void togglePromotion() {
        this.togglePromotion = true;
    }

    public void toggleCastlingPossK(int colIndex) {
        this.toggleCastlingPossK[colIndex] = true;
    }
    public void toggleCastlingPossQ(int colIndex) {
        this.toggleCastlingPossQ[colIndex] = true;
    }
    /*
    public boolean getToggleCastlingPossK(int colIndex) {
        return this.toggleCastlingPossK[colIndex];
    }
    public boolean getToggleCastlingPossQ(int colIndex) {
        return this.toggleCastlingPossQ[colIndex];
    }
    */

    public int getPrevEnpassantPossible() {
        return prevEnpassantPossible;
    }

    /*
    public void setPrevEnpassantPossible(int prevEnpassantPossible) {
        this.prevEnpassantPossible = prevEnpassantPossible;
    }
    */

    public int getFromLine() {
        return fromLine;
    }

    /*
    public void setFromLine(int fromLine) {
        this.fromLine = fromLine;
    }
    */

    public int getFromRow() {
        return fromRow;
    }

    /*
    public void setFromRow(int fromRow) {
        this.fromRow = fromRow;
    }
    */

    public int getToLine() {
        return toLine;
    }

    /*
    public void setToLine(int toLine) {
        this.toLine = toLine;
    }
    */

    public int getToRow() {
        return toRow;
    }

    /*
    public void setToRow(int toRow) {
        this.toRow = toRow;
    }
    public void setTo(int toLine, int toRow) {
        setToLine(toLine);
        setToRow(toRow);
    }
    */

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

    public boolean equals(Ply p){
        return fromLine == p.fromLine && fromRow == p.fromRow && toLine == p.toLine && toRow == p.toRow;
    }

    public String toString() {
        return  " moverColor=" + moverColor +
                " enPassant=" + enPassant + " prevEnpassantPossible=" + prevEnpassantPossible +
//                " check=" + check + " mate=" + mate + " remis=" + remis +
                " toggleCastlingPossibleQ b/w=" + toggleCastlingPossQ[0] + "/" + toggleCastlingPossQ[1] +
                " toggleCastlingPossibleK b/w=" + toggleCastlingPossK[0] + "/" + toggleCastlingPossK[1] +
                " fromLine=" + fromLine + " fromRow=" + fromRow +
                " toLine=" + toLine + " toRow=" + toRow +
                " toPiece=" + toPiece;
    }

}
