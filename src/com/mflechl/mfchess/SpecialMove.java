package com.mflechl.mfchess;

public class SpecialMove {
    SpecialMove() {
    }

    SpecialMove(SpecialMove s_in) {
        castling = s_in.castling;
        enPassant = s_in.enPassant;
    }
    boolean castling = false;
    boolean enPassant = false;
}
