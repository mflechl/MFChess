package com.mflechl.mfchess;

public class IBoardState extends IBoard {

    IBoardState() {
        this(new IBoard(), new State(), "");
    }

    IBoardState(IBoard in) {
        this(in, new State(), "");
    }

    IBoardState(IBoard in, State state) {
        this(in, state, "");
    }

    IBoardState(IBoardState in_state) {
        this(in_state, in_state.state);
    }

    IBoardState(IBoard in, State state, String notation) {
        super(in);
        this.state = new State(state); //makes a deep copy
        setNotation(notation);
    }

    State state;
    String notation;

    public String getNotation() {
        return notation;
    }
    public void setNotation(String notation) {
        this.notation = notation;
    }

}
