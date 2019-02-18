package com.mflechl.mfchess;

public class IBoard {

    IBoard(){ }

    IBoard( IBoard in ){
        setup=DeepCopy.deepCopyInt(in.setup);
    }
    int[][] setup=new int[8][8];
}
