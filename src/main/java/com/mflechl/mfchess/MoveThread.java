package com.mflechl.mfchess;

public class MoveThread extends Thread {
    private ThreadListener listener = null;

    public void addListener(ThreadListener listener) {
        this.listener = listener;
    }

    private void informListener() {
        if (listener != null) {
            listener.onMoveDone("############ Hello from " + this.getName()+" ###################");
        }
    }

    @Override
    public void run() {
        System.out.println("############# Start running... #################");
        super.run();

        try {
            Move move=new Move();
            sleep(5000);
            IBoardState chosenMove = move.bestMove(ChessBoard.iBoard, ChessBoard.currentStaticState );
            Chess.notation.updateText(chosenMove.getNotation(), chosenMove.state.nMoves);
            ChessBoard.pastMoves.add(chosenMove.state.nMoves, chosenMove);
            ChessBoard.setActiveState(chosenMove, chosenMove.state.nMoves);
        } catch (Exception e) {
            e.printStackTrace();
        }
        informListener();
    }
}
