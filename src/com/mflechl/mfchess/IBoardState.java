package com.mflechl.mfchess;

public class IBoardState extends IBoard {

    State state;

    IBoardState(IBoard in, State state) {
        super(in);
        this.state = new State(state); //makes a deep copy
    }

}
