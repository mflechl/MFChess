package com.mflechl.mfchess;

public class IBoardState extends IBoard {

    State state;

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    String notation;

    IBoardState(IBoard in) {
        this(in, new State(), "");
    }

    IBoardState(IBoard in, State state) {
        this(in, state, "");
    }

    IBoardState(IBoard in, State state, String notation) {
        super(in);
        this.state = new State(state); //makes a deep copy
        setNotation(notation);
    }

}
