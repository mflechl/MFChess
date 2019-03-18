package com.mflechl.mfchess;

public class MoveThread extends Thread {

    IBoardState boardState;
    boolean executeNow;
    Move move = new Move();

    int depth = Move.DEFAULT_START_DEPTH;
    boolean goDeeper = false;

    public void setDepth(int depth) {
        this.depth = depth;
    }
    public int getDepth() {
        return depth;
    }

    public boolean isGoDeeper() {
        return goDeeper;
    }
    public void setGoDeeper(boolean goDeeper) {
        this.goDeeper = goDeeper;
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

        IBoardState chosenMove;
        try {
            while (true) {
                move = new Move();
                move.setStartDepth(depth);
                long startTime = System.currentTimeMillis();
                chosenMove = move.bestMove(boardState);

                long finishTime = System.currentTimeMillis();
                if ( move.stopBestMove ) break;
                informListener(chosenMove);
                String gNMN="Could not find next move";
                if (chosenMove != null){
                    gNMN=chosenMove.getNextMovesNotation();
                    ChessBoard.setLabelNextMoves( chosenMove.getEval(), gNMN );
                }
                System.out.println("MOVETHREAD "+getDepth()+"    "+ gNMN + "    That took: " + (finishTime - startTime) + " ms");
                if ( !isGoDeeper() || executeNow || getDepth()>=Move.MAX_DEPTH ) break;
                setDepth( getDepth() + 1 );
            }
            //Chess.notation.updateText(chosenMove.getNotation(), chosenMove.state.nMoves);
            //ChessBoard.pastMoves.add(chosenMove.state.nMoves, chosenMove);
            //ChessBoard.setActiveState(chosenMove, chosenMove.state.nMoves);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
