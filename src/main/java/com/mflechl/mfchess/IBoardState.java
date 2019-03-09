package com.mflechl.mfchess;

public class IBoardState extends IBoard {

    IBoardState() {
        this(new IBoard(), new State(), "", "");
    }

    IBoardState(IBoard in) {
        this(in, new State(), "", "");
    }

    IBoardState(IBoard in, State state) {
        this(in, state, "", "");
    }

    IBoardState(IBoardState in_state) {
        this(in_state, in_state.state, in_state.getNotation(), in_state.getNextMoveNotation(), in_state.getEval());
    }

    IBoardState(IBoard in, State state, String notation, String nextMoveNotation) {
        this(in, state, notation, nextMoveNotation, -99);
    }

    IBoardState(IBoard in, State state, String notation, String nextMoveNotation, float eval) {
        super(in);
        this.state = new State(state); //makes a deep copy
        setEval(eval);
        setNotation(notation);
        setNextMoveNotation(nextMoveNotation);
    }

    State state;
    String notation;
    String nextMoveNotation;
    float eval;

    public float getEval() { return eval; }
    public void setEval(float eval) { this.eval = eval; }

    public String getNextMoveNotation() {
        return nextMoveNotation;
    }

    public void setNextMoveNotation(String nextMoveNotation) {
        this.nextMoveNotation = nextMoveNotation;
    }

    public String getNotation() {
        return notation;
    }
    public void setNotation(String notation) {
        this.notation = notation;
    }

}
