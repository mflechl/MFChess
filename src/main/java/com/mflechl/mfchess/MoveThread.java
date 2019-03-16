package com.mflechl.mfchess;

public class MoveThread extends Thread {

    IBoardState boardState;
    boolean executeNow;
    Move move = new Move();
    int depth = Move.MAX_DEPTH;

    public void setDepth(int depth) {
        this.depth = depth;
    }


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
//            listener.onBestMoveAvailable("############ Hello from " + this.getName()+" ###################");
            listener.onBestMoveAvailable( chosenMove, executeNow );
        }
    }

    @Override
    public void run() {
        super.run();

        IBoardState chosenMove=new IBoardState();
        try {
            move=new Move();
            move.setMaxDepth(depth);
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
