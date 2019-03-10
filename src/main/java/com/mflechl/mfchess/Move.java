package com.mflechl.mfchess;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Move {
    Move() {
        //no instance
    }

    //    public static SpecialMove sDummy = new SpecialMove();
    public final static SpecialMove SDUMMY = new SpecialMove();
    //public final static int NTHREADS = 1;

    ExecutorService executorService = Executors.newFixedThreadPool(1);

    //move from fromLine, fromRow to toLine,toRow legal?
    static boolean isLegal(IBoard _iBoard, SpecialMove sMove, int fromLine, int fromRow, int toLine, int toRow) {
        return isLegal(_iBoard, sMove, fromLine, fromRow, toLine, toRow, ChessBoard.currentStaticState);
    }

    static boolean isLegal(IBoard _iBoard, SpecialMove sMove, int fromLine, int fromRow, int toLine, int toRow, State _state) {
        if (fromLine < 0 || fromLine > 7 || fromRow < 0 || fromRow > 7)
            throw new ArrayIndexOutOfBoundsException("fromLine: " + fromLine + " fromRow: " + fromRow);
        if (toLine < 0 || toLine > 7 || toRow < 0 || toRow > 7)
            throw new ArrayIndexOutOfBoundsException("toLine: " + toLine + " toRow: " + toRow);

        int col = Integer.signum(_iBoard.setup[fromLine][fromRow]);
        if (col == Integer.signum(_iBoard.setup[toLine][toRow]))
            return false; //occupied by own piece, including same tile

        //check if move per se is fine (way to move for a given piece, no obstacles in between)
        boolean isLegal = false;

        switch (Math.abs(_iBoard.setup[fromLine][fromRow])) {
            case ChessBoard.PAWN:
                isLegal = legalMovePawn(_iBoard, fromLine, fromRow, toLine, toRow, col, sMove, _state);
                break;
            case ChessBoard.ROOK:
                isLegal = legalMoveRook(_iBoard, fromLine, fromRow, toLine, toRow);
                break;
            case ChessBoard.KNIGHT:
                isLegal = legalMoveKnight(fromLine, fromRow, toLine, toRow);
                break;
            case ChessBoard.BISHOP:
                isLegal = legalMoveBishop(_iBoard, fromLine, fromRow, toLine, toRow);
                break;
            case ChessBoard.QUEEN:
                isLegal = legalMoveRook(_iBoard, fromLine, fromRow, toLine, toRow)
                        || legalMoveBishop(_iBoard, fromLine, fromRow, toLine, toRow);
                break;
            case ChessBoard.KING:
                isLegal = legalMoveKing(_iBoard, fromLine, fromRow, toLine, toRow, col, sMove, _state);
                break;
        }

        if (!isLegal) return false;

        //check if move does not result in check
        if (Math.abs(_iBoard.setup[toLine][toRow]) == ChessBoard.KING)
            return true; //if it is a test for check, do not need this!

        //make move on hypothetical tiles and test if this would mean "check"
        IBoard hypo_iBoard = new IBoard(_iBoard); //deep copy
        ChessBoard.processMove(hypo_iBoard, fromLine, fromRow, toLine, toRow, _iBoard.setup[fromLine][fromRow], true, sMove);
        //System.out.println("isLegal:\n" + hypo_iBoard);
        boolean isCheck = isChecked(hypo_iBoard, col);

        return (!isCheck);
    }


    private static boolean legalMovePawn(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow, int col, SpecialMove sMove, State _state) {
        //System.out.println("legalMovePawn "+fromLine+" "+fromRow+" "+toLine+"-"+toRow+" state="+_state+" sMove="+sMove+"\n"+_iBoard);
        if (fromRow == toRow) {
            //normal move
            if ((toLine - fromLine == col) && _iBoard.setup[toLine][toRow] == 0) return true;
            //two steps from starting line
            if (((col == 1 && fromLine == 1) || (col == -1 && fromLine == 6)) &&   //in starting line for white or black
                    (toLine - fromLine == 2 * col)) {                              //move two steps in the right direction
                //no piece in between
                return (_iBoard.setup[fromLine + col][fromRow] == 0 &&
                        _iBoard.setup[toLine][fromRow] == 0); //needs to be empty
            }
        }
        //capture piece
        else if (Math.abs(fromRow - toRow) == 1 && (toLine - fromLine) == col) {    //eliminate other piece?
            if (_iBoard.setup[toLine][toRow] * col < 0) return true; //to-field is not empty and has opposite sign
            if (_state.enPassantPossible == toRow) { //en passant?
                if ((fromLine == 4 && col == ChessBoard.WHITE) || (fromLine == 3 && col == ChessBoard.BLACK)) {
                    sMove.enPassant = true; //SETTING ENPASSANT
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean legalMoveRook(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow) {
        if ((fromRow == toRow) || (fromLine == toLine)) {
            return emptyBetween(_iBoard, fromLine, fromRow, toLine, toRow, false);
        }
        return false;
    }

    private static boolean legalMoveKnight(int fromLine, int fromRow, int toLine, int toRow) {
        return (Math.abs(fromRow - toRow) == 2 && Math.abs(fromLine - toLine) == 1) ||
                (Math.abs(fromRow - toRow) == 1 && Math.abs(fromLine - toLine) == 2);
    }

    private static boolean legalMoveBishop(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow) {
        if (Math.abs(fromRow - toRow) == Math.abs(fromLine - toLine)) {
            return emptyBetween(_iBoard, fromLine, fromRow, toLine, toRow, false);
        }
        return false;
    }

    private static boolean legalMoveKing(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow, int col, SpecialMove sMove, State _state) {
        //normal move
        if (Math.abs(fromRow - toRow) <= 1 && Math.abs(fromLine - toLine) <= 1) return true;

        //castling
        // System.out.println(_state.castlingPossibleQ[1] + " " + _state.castlingPossibleK[1] + " W  B " + _state.castlingPossibleQ[0] + " " + _state.castlingPossibleK[0]);
        if (fromLine != toLine) return false; //king must be on the starting position to fulfill later criteria
        if (_state.check) return false;

        int colIndex = (col + 1) / 2; //black=0, white=1
        if ((toRow == 2 && emptyBetween(_iBoard, fromLine, fromRow, toLine, 1, true, _state) && _state.castlingPossibleQ[colIndex]) ||
                (toRow == 6 && emptyBetween(_iBoard, fromLine, fromRow, toLine, 6, true, _state) && _state.castlingPossibleK[colIndex])) {
            sMove.castling = true;
            return true;
        }
        return false;
    }

    //overload for case where currentStaticState is not needed, i.e. when there is no castling being checked (checkCheck=false)
    private static boolean emptyBetween(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow, boolean checkCheck) {
        return emptyBetween(_iBoard, fromLine, fromRow, toLine, toRow, checkCheck, ChessBoard.currentStaticState);
    }

    private static boolean emptyBetween(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow, boolean checkCheck, State _state) {

        if (toRow == fromRow) { //rook-type move
            int direction = Integer.signum(toLine - fromLine);  //+1 if toLine is larger, -1 if fromLine
            for (int i = fromLine + direction; (i * direction) < (toLine * direction); i += direction) {
                if (_iBoard.setup[i][fromRow] != 0) return false;
            }
        } else if (toLine == fromLine) { //rook-type move
            int direction = Integer.signum(toRow - fromRow);  //+1 if toRow is larger, -1 if fromRow
            for (int j = fromRow + direction; (j * direction) < (toRow * direction); j += direction) {
                if (_iBoard.setup[fromLine][j] != 0) return false;
                if (checkCheck) { //only needed for castling: move on hypo board and then check for check
                    IBoard hypo_iBoard = new IBoard(_iBoard);
                    ChessBoard.processMove(hypo_iBoard, fromLine, fromRow, toLine, j, _iBoard.setup[fromLine][fromRow], true, SDUMMY);
                    if (isChecked(hypo_iBoard, _state.turnOf)) return false;
                }
            }
        } else {   //bishop-type move
            int dir1 = Integer.signum(toRow - fromRow);    //+1 if toRow  is larger, -1 if fromRow
            int dir2 = Integer.signum(toLine - fromLine);  //+1 if toLine is larger, -1 if fromLine
            int i = fromLine + dir2;
            for (int j = fromRow + dir1; (j * dir1) < (toRow * dir1); j += dir1) {
                if (_iBoard.setup[i][j] != 0) return false;
                i += dir2;
            }
        }

        return true;
    }

    //is col (+1 white, -1 black) in check in this tiles setup?
    static boolean isChecked(IBoard _iBoard, int col) {
        //find king
        int lineKing = -1, rowKing = -1;
        for (int il = 0; il < 8; il++) {
            for (int ir = 0; ir < 8; ir++) {
                if (_iBoard.setup[il][ir] == col * ChessBoard.KING) {
                    lineKing = il;
                    rowKing = ir;
                    break;
                }
            }
            if (lineKing > -1) break;
        }

        if (lineKing < 0) {
            System.out.println("No king found:" + _iBoard);
        }

        //check opponent pieces: can eliminate king=is in check
        for (int il = 0; il < 8; il++) {
            for (int ir = 0; ir < 8; ir++) {
                if (_iBoard.setup[il][ir] * col < 0) {
                    //		    if ( isLegal(_iBoard, il, ir, lineKing, rowKing) ) return true;
                    if (isLegal(_iBoard, new SpecialMove(), il, ir, lineKing, rowKing)) return true;
                }
            }
        }
        return false;
    }

    /*
    public class MoveThread implements Runnable {
        IBoard _iBoard;
        State _state;
        boolean stopAfterFirst;
        int depth, maxDepth;
        boolean adaptDepth;
        ArrayList<IBoardState> list;

        public MoveThread(IBoard _iBoard, State _state, boolean stopAfterFirst, int depth, int maxDepth, boolean adaptDepth){
            this._iBoard = _iBoard;
            this._state = _state;
            this.stopAfterFirst = stopAfterFirst;
            this.depth = depth;
            this.maxDepth = maxDepth;
            this.adaptDepth = adaptDepth;
        }

        public void run(){
            list = allLegalMoves(_iBoard, _state, stopAfterFirst, depth, maxDepth, adaptDepth);
//            System.out.println("MyRunnable running");
        }
    }
    */

    public void futureSubList(IBoardState _iBoard, State _state, boolean stopAfterFirst, int depth, int maxDepth, boolean adaptDepth) {
//        public Future<ArrayList<IBoardState>> futureSubList(IBoardState _iBoard, State _state, boolean stopAfterFirst, int depth, int maxDepth, boolean adaptDepth) {
//        return executorService.submit(() -> allLegalMoves(_iBoard, _state, stopAfterFirst, depth, maxDepth, adaptDepth));
        executorService.submit(() -> {
            ArrayList<IBoardState> subList = allLegalMoves(_iBoard, _state, stopAfterFirst, depth, maxDepth, adaptDepth);
            selectMaxFromList(_iBoard, subList);
        });

    }

    Boolean noLegalMoves(IBoard _iBoard, State _state) {
        ArrayList<IBoardState> moveList = allLegalMoves(_iBoard, _state, true, 1, 1, false);
        return moveList.isEmpty();
    }

    IBoardState bestMove(IBoard _iBoard, State _state, boolean stopAfterFirst, int maxDepth, boolean adaptDepth) {
        ArrayList<IBoardState> allMoves = allLegalMoves(_iBoard, _state, stopAfterFirst, 1, maxDepth, adaptDepth);
        //for (IBoardState board : allMoves) System.out.println("### VALUE: " + board.getEval() + " " + board.getNotation());
        return EvaluateBoard.getMaxMove(allMoves, true);
        //System.out.println("chosenMove =\n" + chosenMove + "nLegalMoves=" + allMoves.size() + " val=" + chosenMove.getEval() + " M=" + chosenMove.state.turnOf);
    }

    //stopAfterFirst: to check only if any legal move exists, i.e. no check mate or remis
    ArrayList<IBoardState> allLegalMoves(IBoard _iBoard, State _state, boolean stopAfterFirst, int depth, int maxDepth, boolean adaptDepth) {
        //throws InterruptedException {
        ArrayList<IBoardState> list = new ArrayList<>();

        //      System.out.println("################################### IN ALM ############################## " + depth);

        for (int il = 0; il < 8; il++) {
            for (int ir = 0; ir < 8; ir++) {
                if (_iBoard.setup[il][ir] * _state.turnOf > 0) {
//                    ArrayList<IBoardState> listPiece = pieceLegalMove(_iBoard, il, ir, _state, stopAfterFirst, (depth==1) ); //only get notation for first depth
                    ArrayList<IBoardState> listPiece = pieceLegalMove(_iBoard, il, ir, _state, stopAfterFirst, true);
                    list.addAll(listPiece);
                    if (stopAfterFirst && !list.isEmpty()) return list;
                }
            }
        }

        /*
        if (depth==maxDepth) {
            for (IBoardState boardState : list) {
                System.out.print(depth);
                for (int i = 0; i < depth; i++) System.out.print("      ");
                System.out.print(boardState.getNextMoveNotation());
                System.out.println("           "+boardState.getNextMoveNotation());
            }
        }
        */

        if (adaptDepth && depth == 1) { //set adaptive weight. Readjust in later move might bring bias, have to do this in a smarter way
            int nLegalMoves = list.size();
            if (nLegalMoves > 25) maxDepth = 3;
            else if (nLegalMoves > 15) maxDepth = 4;
            else if (nLegalMoves > 10) maxDepth = 5;
            else maxDepth = 5;
            //if (depth == 1) System.out.println("nLM=" + nLegalMoves + " DEPTH=" + maxDepth);
        }

        if (depth < maxDepth && !list.isEmpty()) {
            for (IBoardState boardState : list) {

                if (depth == 1) {
                    try {
//                        subList = futureSubList(boardState, boardState.state, false, depth + 1, maxDepth, adaptDepth).get();
                        futureSubList(boardState, boardState.state, false, depth + 1, maxDepth, adaptDepth);
                    } catch (Exception e) {
                        System.out.println("A problem when getting the future...");
                    }
                } else {
                    ArrayList<IBoardState> subList = allLegalMoves(boardState, boardState.state, false, depth + 1, maxDepth, adaptDepth);
                    selectMaxFromList(boardState, subList);
                }

//                selectMaxFromList(boardState, subList);

            }
        }

        /*
        //with nextMoves, inverse
        if (depth == maxDepth) {
            for (IBoardState boardState : list) {
                System.out.print(depth);
                for (int i = 0; i < depth; i++) System.out.print("      ");
                System.out.print(boardState.getNotation());
                System.out.println("           " + boardState.getNextMoveNotation() + "            val=" + boardState.getEval());
            }
        }
        */

        return list;
    }

    void selectMaxFromList(IBoardState boardState, ArrayList<IBoardState> subList) {
        float val;
        String moveN;
        IBoardState maxMove;

        if (subList.isEmpty()) {
            moveN = "";
            val = boardState.getEval();
        } else {
//                    val = EvaluateBoard.getMaxMove(subList).getEval();
            maxMove = EvaluateBoard.getMaxMove(subList);
            //X if ( maxMove.getNextMoveNotation().matches(".*1-0 .*") ) System.out.println( "ABC: "+boardState.state.mate + " "+maxMove.state.mate +" "+maxMove.getNextMoveNotation() + " ! "+maxMove.getNotation() );
            val = maxMove.getEval();
            moveN = maxMove.getNextMoveNotation();
            //System.out.println(depth+": "+moveN+"  XXXXXX  "+maxMove.getNotation());
        }
        boardState.setEval(val);
        boardState.setNextMoveNotation(boardState.getNotation() + " " + moveN);

    }

    ArrayList<IBoardState> pieceLegalMove(IBoard _iBoard, int fromLine, int fromRow, State _state, boolean stopAfterFirst, boolean updateNotation) {
        ArrayList<IBoardState> list = new ArrayList<>();
        for (int toLine = 0; toLine < 8; toLine++) {
            for (int toRow = 0; toRow < 8; toRow++) {
                SpecialMove sMove = new SpecialMove();
                if (isLegal(_iBoard, sMove, fromLine, fromRow, toLine, toRow, _state)) {

                    IBoard hypo_iBoard = new IBoard(_iBoard); //deep copy
                    boolean prom = ChessBoard.processMove(hypo_iBoard, fromLine, fromRow, toLine, toRow, _iBoard.setup[fromLine][fromRow], true, sMove);
                    //System.out.println(_iBoard.setup[fromLine][fromRow] + " from: " + CoordBoard.alpha[fromRow + 1] + " " + (fromLine + 1) + "   to: " + CoordBoard.alpha[toRow + 1] + " " + (toLine + 1));

                    if (stopAfterFirst) { //only about checking that a legal move exists?
                        list.add(new IBoardState(hypo_iBoard));
                        return list;
                    }

                    State updatedState = new State(_state);
                    updatedState.update(_iBoard.setup[fromLine][fromRow], fromLine, toLine, fromRow);
                    ChessBoard.updateCastlingState(updatedState, _iBoard.setup[fromLine][fromRow], fromLine, fromRow, toLine, toRow, sMove.castling);
                    updateCheckState(updatedState, hypo_iBoard);

                    for (int i = 2; i <= 5; i++) { //in case of promotion, write four possible moves; otherwise no real loop
                        //System.out.println("pieceLegalMove from l-random to l-random "+fromLine+"-"+fromRow+" to "+toLine+"-"+toRow+":\n"+_iBoard);
                        String moveNotation = "";
                        if (updateNotation)
                            moveNotation = Notation.getMoveNotation(_iBoard, updatedState, _state, fromLine, fromRow, toLine, toRow,
                                    _iBoard.setup[fromLine][fromRow], _iBoard.setup[toLine][toRow], sMove);

                        if (!prom) {
                            list.add(new IBoardState(hypo_iBoard, updatedState, moveNotation, moveNotation, EvaluateBoard.eval(hypo_iBoard, updatedState)));
                            break; //stop here if it is not a promotion case
                        }

                        hypo_iBoard.setup[toLine][toRow] = (byte) (_state.turnOf * i); //2=queen, 3=rook, 4=bishop, 5=knight
                        String adaptedMoveNotation = moveNotation.replaceAll("Q$", ChessBoard.lpieces[i] + " ");
                        list.add(new IBoardState(hypo_iBoard, updatedState, adaptedMoveNotation, adaptedMoveNotation,
                                EvaluateBoard.eval(hypo_iBoard, updatedState) ));
                    }
                }
            }
        }
        return list;
    }

    static ArrayList<int[]> legalDestination(IBoard _iBoard, int fromLine, int fromRow, State _state, boolean stopAfterFirst) {
        ArrayList<int[]> list = new ArrayList<>();
        for (int toLine = 0; toLine < 8; toLine++) {
            for (int toRow = 0; toRow < 8; toRow++) {
                SpecialMove sDummy = new SpecialMove();
                if (isLegal(_iBoard, sDummy, fromLine, fromRow, toLine, toRow, _state)) {
                    int[] dest = {toLine, toRow};
                    list.add(dest);
                    if (stopAfterFirst) return list;
                }
            }
        }
        return list;
    }

    void updateCheckState(State state, IBoard iBoard) {
        state.check = isChecked(iBoard, state.turnOf); //check of opponent result of the move?
        if (noLegalMoves(iBoard, state)) {
            if (state.check) state.mate = true;
            else state.remis = true;
        }
    }


}
