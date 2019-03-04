package com.mflechl.mfchess;

import java.util.ArrayList;

public final class EvaluateBoard {
    EvaluateBoard () {
        //no instance
    }

    /*
    static float eval( IBoardState board ) {
        return eval( board, board.state);
    }
    */

    static float eval( IBoard board, State state ){
        float val=0;
        return val;
    }

    static IBoardState getMaxMove(ArrayList<IBoardState> list){
        float maxEval=list.get(0).getEval();
        IBoardState maxBoard = new IBoardState( list.get(0) );
        for ( IBoardState board: list.subList( 1, list.size() ) ){
            if ( board.getEval() > maxEval ){
                maxEval = board.getEval();
                maxBoard = new IBoardState(board);
            }
        }
        return maxBoard;
    }

}
