package com.mflechl.mfchess;

import java.util.ArrayList;

public final class EvaluateBoard {
    EvaluateBoard () {
        //no instance
    }

    //                               {"", "K", "Q", "R", "B", "N", "P"};
    static final int[] valueOf = {0, 0, 9, 5, 3, 3, 1};
//    static final int[] valueOf = { 0,   0,   9,   5,   3,   3, 1  };

    /*
    static float eval( IBoardState board ) {
        return eval( board, board.state);
    }
    */

    static float eval( IBoard board, State state ){
        float val=0;
        val += getValSingle(board);
        return val;
    }

    static IBoardState getMaxMove(ArrayList<IBoardState> list){
        float maxEval=list.get(0).getEval();
        IBoardState maxBoard = new IBoardState( list.get(0) );

        //for WHITE: maximize; BLACK: minimize. turnOf holds who will play the *next* turn,hence *-1
        int turn = list.get(0).state.turnOf * -1;

        float eval;
        for ( IBoardState board: list.subList( 1, list.size() ) ){
            eval = board.getEval() * turn;
            if (eval > maxEval) {
                maxEval = eval;
                maxBoard = new IBoardState(board);
            }
        }
        return maxBoard;
    }

    static float getValSingle(IBoard board) { //value for single pieces (i.e. not combinations)
        float val = 0;
        int piece = 0;
        for (int i = 0; i < board.setup.length; i++) {
            for (int j = 0; j < board.setup[i].length; j++) {
                piece = board.setup[i][j];
                if (piece == 0) continue; //just to speed up
                val += valueOf[Math.abs(piece)] * Math.signum(piece);
            }
        }
        return val;
    }


}
