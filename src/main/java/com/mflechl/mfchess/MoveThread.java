package com.mflechl.mfchess;

public class MoveThread extends Thread {

    IBoard board;
    BState state;

    MoveThread() {
        this(ChessBoard.iBoard, ChessBoard.currentStaticState);
    }

        MoveThread(IBoard board, BState state){
        this.board = board;
        this.state = state;
    }

    private ThreadListener listener = null;
    public void addListener(ThreadListener listener) {
        this.listener = listener;
    }

    private void informListener(IBoardState chosenMove) {
        if (listener != null) {
//            listener.onMoveDone("############ Hello from " + this.getName()+" ###################");
            listener.onMoveDone( chosenMove );
        }
    }

    @Override
    public void run() {
        super.run();

        IBoardState chosenMove=new IBoardState();
        try {
            Move move=new Move();
            long startTime = System.currentTimeMillis();
            chosenMove = move.bestMove(board, state);
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
