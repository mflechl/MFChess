package com.mflechl.mfchess;

public final class EvaluateBoard {
    EvaluateBoard() {
        //no instance
    }

    //                              {"", "K", "Q", "R", "B", "N", "P"};
    static final int[] VALUE_OF = {0, 0, 900, 500, 300, 300, 100};
    //bishop pair
    static final int VALUE_BISHOP_PAIR = 50;
    //check
    static final int VALUE_CHECK = 10;

    static final int[] indexToSign = {-1, +1};

    //    static Random random = new Random(131737); //for benchmark
    //static Random random = new Random(); //for benchmark

    /*
    static float eval( IBoardState board ) {
        return eval( board, board.state);
    }
    */

    //@SuppressWarnings("unused")
    static int eval(IBoard board, BState state) {
        int val = 0;

        if (state.mate) return -99999 * state.turnOf; //no moves - check mate or remis!
        else if (state.remis) return 0;  //means: pick this move if you are in a bad position

        val += getValSingle(board);
        val += getValCombi(board);

        if (state.check) val += VALUE_CHECK * state.turnOf * -1;

        return val;
    }

    //Functions below should be fast, so use mainly plain arrays
    static int getValSingle(IBoard board) { //value for single pieces (i.e. not combinations)
        int val = 0;
        int piece;
        for (int i = 0; i < board.setup.length; i++) {
            for (int j = 0; j < board.setup[i].length; j++) {
                piece = board.setup[i][j];
                if (piece == 0) continue; //just to speed up
                val += VALUE_OF[Math.abs(piece)] * Math.signum(piece);
            }
        }
        return val;
    }

    static int getValCombi(IBoard board) { //value for combinations of pieces, e.g. bishop pairs
        int val = 0;
        int[][] nPieces = new int[2][7]; //0-black, 1-white
        int piece;
        for (int i = 0; i < board.setup.length; i++) {
            for (int j = 0; j < board.setup[i].length; j++) {
                piece = board.setup[i][j];
                nPieces[((int) Math.signum(piece) + 1) / 2][Math.abs(piece)]++;
            }
        }

        //double bishop
        for (int i = 0; i < 2; i++) {
            if (nPieces[i][ChessBoard.BISHOP] >= 2) val += VALUE_BISHOP_PAIR * indexToSign[i];
        }

        return val;
    }


}
