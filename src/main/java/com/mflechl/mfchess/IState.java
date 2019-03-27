package com.mflechl.mfchess;

class IState {
    IState() {
    }

    public IState(IState st) {
        this.turnOf = st.turnOf;
        this.moveNumber = st.moveNumber;
        this.nMoves = st.nMoves; //number of completed moves so far, black and white
        this.castlingPossibleQ = st.castlingPossibleQ.clone();
        this.castlingPossibleK = st.castlingPossibleK.clone();
        this.enPassantPossible = st.enPassantPossible;
        this.check = st.check;
        this.remis = st.remis;
        this.mate = st.mate;
    }

    void update(int movingPiece, int fromLine, int toLine, int fromRow) {
        turnOf *= -1;
        if (turnOf == ChessBoard.WHITE) moveNumber++;
        nMoves++;
        if (Math.abs(movingPiece) == ChessBoard.PAWN && Math.abs(fromLine - toLine) == 2) enPassantPossible = fromRow;
        else enPassantPossible = -11;
    }

    void undoUpdate(int prevEnPassantPossible) {
        turnOf *= -1;
        if (turnOf == ChessBoard.BLACK) moveNumber--;
        nMoves--;
        enPassantPossible = prevEnPassantPossible;
        //if (Math.abs(movingPiece) == ChessBoard.PAWN && Math.abs(fromLine - toLine) == 2) enPassantPossible = fromRow;
        //else enPassantPossible = -1;
    }


    public String toString() {
        return "turnOf=" + turnOf + " nMoves=" + nMoves + " moveNumber=" + moveNumber + " enPassantPossible=" + enPassantPossible +
                " check=" + check + " mate=" + mate + " remis=" + remis +
                " castlingPossibleQ b/w=" + castlingPossibleQ[0] + "/" + castlingPossibleQ[1] +
                " castlingPossibleK b/w=" + castlingPossibleK[0] + "/" + castlingPossibleK[1];

    }

    int turnOf = ChessBoard.WHITE;
    int nMoves = 0; //number of moves that have happened so far, counting b and w separately
    int moveNumber = 1; ////one move=one each black and white, starting with 1 after first white move.
    boolean[] castlingPossibleQ = {true, true};  //true-castling possible, false-castling not possible anymore or done, for white/black, for Queen-side
    boolean[] castlingPossibleK = {true, true};  //same for king-side
    int enPassantPossible = -11;
    boolean check = false; //currently, is opponent in check?
    boolean remis = false; //currently, no more moves, remis?
    boolean mate = false; //currently, no more moves, mate?
}
