package com.mflechl.mfchess;

public class MoveThread extends Thread {

    IBoardState boardState;
    boolean executeNow;
    Move move = new Move();

    MoveThread() {
//        this( new IBoardState(ChessBoard.iBoard, ChessBoard.currentStaticState), false);
        this( new IBoardState(), false);
    }

    MoveThread(IBoardState boardState, boolean executeNow){
        this.boardState = boardState;
        this.executeNow = executeNow;
    }

    private ThreadListener listener = null;
    public void addListener(ThreadListener listener) {
        this.listener = listener;
    }

    private void informListener(IBoardState chosenMove) {
        if (listener != null) {
//            listener.onMoveDone("############ Hello from " + this.getName()+" ###################");
            listener.onMoveDone( chosenMove, executeNow );
        }
    }

    @Override
    public void run() {
        super.run();

        IBoardState chosenMove=new IBoardState();
        try {
            move=new Move();
            long startTime = System.currentTimeMillis();
            chosenMove = move.bestMove(boardState);

            long finishTime = System.currentTimeMillis();
            System.out.println("That took: " + (finishTime - startTime) + " ms");

            //Chess.notation.updateText(chosenMove.getNotation(), chosenMove.state.nMoves);
            //ChessBoard.pastMoves.add(chosenMove.state.nMoves, chosenMove);
            //ChessBoard.setActiveState(chosenMove, chosenMove.state.nMoves);
        } catch (Exception e) {
            e.printStackTrace();
        }

        informListener(chosenMove);
    }
}
