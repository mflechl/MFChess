package com.mflechl.mfchess;

class State {
    State() {
    }

    public State(State st) {
        this.turnOf = st.turnOf;
        this.moveNumber = st.moveNumber; //one move=one each black and white
        this.nMoves = st.nMoves; //number of completed moves so far, black and white
        this.castlingPossibleQ = st.castlingPossibleQ.clone();
        this.castlingPossibleK = st.castlingPossibleK.clone();
        this.enPassantPossible = st.enPassantPossible;
        this.check = st.check;
    }

    void update(int movingPiece, int fromLine, int toLine, int aRow) {
        turnOf *= -1;
        if (turnOf == ChessBoard.WHITE) moveNumber++;
        nMoves++;
        if (Math.abs(movingPiece) == ChessBoard.PAWN && Math.abs(fromLine - toLine) == 2) enPassantPossible = aRow;
        else enPassantPossible = -1;
    }

    int turnOf = ChessBoard.WHITE;
    int nMoves = 0;
    int moveNumber = 1;
    boolean[] castlingPossibleQ = {true, true};  //true-castling possible, false-castling not possible anymore or done, for white/black, for Queen-side
    boolean[] castlingPossibleK = {true, true};  //same for king-side
    int enPassantPossible = -1;
    boolean check = false; //currently, is opponent in check?
}
