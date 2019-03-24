package com.mflechl.mfchess;

public class IBoardState extends IBoard {

    IBoardState() {
        this(new IBoard(), new IState(), "", "");
    }

    IBoardState(IBoard in) {
        this(in, new IState(), "", "");
    }

    IBoardState(IBoard in, IState state) {
        this(in, state, "", "");
    }

    IBoardState(IBoardState in_state) {
        this(in_state, in_state.state, in_state.getNotation(), in_state.getNextMovesNotation(), in_state.getEval());
    }

    IBoardState(IBoard in, IState state, String notation, String nextMoveNotation) {
        this(in, state, notation, nextMoveNotation, -999111);
    }

    IBoardState(IBoard in, IState state, String notation, String nextMoveNotation, int eval) {
        super(in);
        if (state == null) state = new IState();
        this.state = new IState(state); //makes a deep copy
        setEval(eval);
        setNotation(notation);
        setNextMoveNotation(nextMoveNotation);
    }

    IState state;
    String notation;
    String nextMoveNotation;
    int eval;

    public int getEval() {
        return eval;
    }

    public void setEval(int eval) {
        this.eval = eval;
    }

    public String getNextMovesNotation() {
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
