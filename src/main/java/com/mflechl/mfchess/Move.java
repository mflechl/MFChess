package com.mflechl.mfchess;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
//import java.util.concurrent.FutureTask;

public final class Move {
    Move() {
        if (USE_THREADS) {
            if (ncores > 1) nthreads = ncores - 1;
            executorService = Executors.newFixedThreadPool(nthreads);
        }
    }

    static final boolean PICK_RANDOM = false;

    static boolean USE_ALPHABETA = false;
    static boolean MINMAXTEST = true; //TEST
    static ArrayList<Integer> tree = new ArrayList<>(); //TEST
    static ArrayList<Double> vals = new ArrayList<>(); //TEST
    static int nMaxBranch = 20; //TEST

    private static final boolean USE_THREADS = false;
    private static final float INF = 100000;

    static int nALMCalls = 0;

    //    public static SpecialMove sDummy = new SpecialMove();
    public final static SpecialMove SDUMMY = new SpecialMove();
    //public final static int NTHREADS = 1;

    private static int ncores = Runtime.getRuntime().availableProcessors();
    private static int nthreads = 1;

    ExecutorService executorService;

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
    public class test {
        public Future<Boolean> futureSubList(IBoardState _iBoardState, State _state, boolean stopAfterFirst, int depth, boolean isPlyOne) {
            return executorService.submit(() -> {
                ArrayList<IBoardState> subList = allLegalMoves(_iBoardState, _state, stopAfterFirst, depth, isPlyOne);
                selectMaxFromList(_iBoardState, subList);
                return true;
            });
        }
    }
    */

    public Future<Boolean> futureSubList(IBoardState _iBoardState, State _state, boolean stopAfterFirst, int depth, boolean isPlyOne) {
        return executorService.submit(() -> {
            ArrayList<IBoardState> subList = allLegalMoves(_iBoardState, _state, stopAfterFirst, depth, isPlyOne, -INF, +INF);
            selectMaxFromList(_iBoardState, subList);
            return true;
        });
    }

    Boolean noLegalMoves(IBoard _iBoard, State _state) {
        ArrayList<IBoardState> moveList = allLegalMoves(_iBoard, _state, true, 1, false, -INF, +INF);
        return moveList.isEmpty();
    }

    IBoardState bestMove(IBoard _iBoard, State _state, boolean stopAfterFirst, int depth, boolean isPlyOne) {
        ArrayList<IBoardState> allMoves = allLegalMoves(_iBoard, _state, stopAfterFirst, depth, isPlyOne, -INF, +INF);
        //for (IBoardState board : allMoves) System.out.println("### VALUE: " + board.getEval() + " " + board.getNotation());
        System.out.println("nALMCalls = " + nALMCalls);
        return EvaluateBoard.getMaxMove(allMoves, PICK_RANDOM, true);
        //System.out.println("chosenMove =\n" + chosenMove + "nLegalMoves=" + allMoves.size() + " val=" + chosenMove.getEval() + " M=" + chosenMove.state.turnOf);
    }

    //stopAfterFirst: to check only if any legal move exists, i.e. no check mate or remis
    ArrayList<IBoardState> allLegalMoves(IBoard _iBoard, State _state, boolean stopAfterFirst, int depth, boolean isPlyOne, float alpha, float beta) {
        //throws InterruptedException {
        ArrayList<IBoardState> list = new ArrayList<>();

        nALMCalls++;
        //System.out.println("################################### IN ALM ############################## " + depth + "   " + _state.moveNumber + "   " + _state.nMoves);

        if (MINMAXTEST) {
            list = getMinMaxList(depth, _state.nMoves);
        } else {
            for (int il = 0; il < 8; il++) {
                for (int ir = 0; ir < 8; ir++) {
                    if (_iBoard.setup[il][ir] * _state.turnOf > 0) {
//                        ArrayList<IBoardState> listPiece = pieceLegalMove(_iBoard, il, ir, _state, stopAfterFirst, true, true);
                        ArrayList<IBoardState> listPiece = pieceLegalMove(_iBoard, il, ir, _state, stopAfterFirst, true, (depth == 0));
                        list.addAll(listPiece);
                        if (stopAfterFirst && !list.isEmpty()) return list;
                    }
                }
            }
        }
        if (isPlyOne && !MINMAXTEST) { //set adaptive weight. Readjust in later move might bring bias, so should set isPlyOne to false for further moves
            int nLegalMoves = list.size();
            if (nLegalMoves > 25) depth = 2;          //maxDepth = 2 + 1 = 3 (since lowest index is 0)
            else if (nLegalMoves > 15) depth = 3;
            else if (nLegalMoves > 10) depth = 4;
            else depth = 4;
            depth=2;
            System.out.println("nLM=" + nLegalMoves + " DEPTH=" + depth);
        }

        if (depth > 0 && !list.isEmpty()) {
            ArrayList<Future<Boolean>> futureBooleans = new ArrayList<>(); // = new ArrayList<>(list.size());
            float value = -1 * _state.turnOf * INF; //-inf for white, +inf +inf for black

//            for (IBoardState boardState : list) {
            for (int ib = 0; ib < list.size(); ib++) {
                IBoardState boardState = list.get(ib);

                if (USE_THREADS && isPlyOne) {
                    try {
                        //DOES NOT WORK
                        Future<Boolean> fb = futureSubList(boardState, boardState.state, false, depth - 1, false);
                        //Future<Boolean> fb = new test().futureSubList(boardState, boardState.state, false, depth - 1, isPlyOne);
                        futureBooleans.add(fb);
                        //success = futureBooleans.get(ib).get();
                    } catch (Exception e) {
                        System.out.println("A problem when setting the future... " + ib);
                    }
                } else {
                    ArrayList<IBoardState> subList = allLegalMoves(boardState, boardState.state, false, depth - 1, false, alpha, beta);
                    selectMaxFromList(boardState, subList);

                    //ALPHABETA
                    if (USE_ALPHABETA) {
                        if (_state.turnOf == ChessBoard.WHITE) {
                            value = Math.max(value, boardState.getEval());
                            alpha = Math.max(alpha, value);
                            if (alpha >= beta) {
                                list = new ArrayList<IBoardState>(list.subList(0, ib)); //or ib+1 ?
                                break;
                            }
                        } else {
                            value = Math.min(value, boardState.getEval());
                            beta = Math.min(beta, value);
                            if (alpha >= beta) {
                                list = new ArrayList<IBoardState>(list.subList(0, ib)); //or ib+1 ?
                                break;
                            }
                        }
                    }
                    //ALPHABETA

                    int ind = tree.indexOf(boardState.state.nMoves);
                    //System.out.println("IND " + boardState.state.nMoves + "    " + tree.indexOf(boardState.state.nMoves));
                    if (ind >= 0) vals.set(ind, (double)boardState.getEval());
                }

            } //end loop over list of boardStates

            if (USE_THREADS && isPlyOne) {
                System.out.println("A "+executorService+"   "+list.size()+"    ");
                for ( Future<Boolean> futureBoolean: futureBooleans  ) {
                    try {
                        futureBoolean.get(); //wait for thread to finish
                    } catch (Exception e) {
                        System.out.println("A problem when getting the future... ");
                    }
                }
                System.out.println("B "+executorService+"   "+list.size()+"    ");
            }


        }

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
            maxMove = EvaluateBoard.getMaxMove(subList, PICK_RANDOM, true);
            //X if ( maxMove.getNextMoveNotation().matches(".*1-0 .*") ) System.out.println( "ABC: "+boardState.state.mate + " "+maxMove.state.mate +" "+maxMove.getNextMoveNotation() + " ! "+maxMove.getNotation() );
            val = maxMove.getEval();
            moveN = maxMove.getNextMoveNotation();
            //System.out.println(depth+": "+moveN+"  XXXXXX  "+maxMove.getNotation());
        }
        boardState.setEval(val);
        boardState.setNextMoveNotation(boardState.getNotation() + " " + moveN);

    }


    ArrayList<IBoardState> pieceLegalMove(IBoard _iBoard, int fromLine, int fromRow, State _state, boolean stopAfterFirst, boolean updateNotation, boolean doEval) {
        ArrayList<IBoardState> list = new ArrayList<>();
        float eval = -9998 * _state.turnOf;
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
                            if (doEval) eval = EvaluateBoard.eval(hypo_iBoard, updatedState);
                            list.add(new IBoardState(hypo_iBoard, updatedState, moveNotation, moveNotation, eval));
                            break; //stop here if it is not a promotion case
                        }

                        hypo_iBoard.setup[toLine][toRow] = (byte) (_state.turnOf * i); //2=queen, 3=rook, 4=bishop, 5=knight
                        String adaptedMoveNotation = moveNotation.replaceAll("Q$", ChessBoard.lpieces[i] + " ");
                        if (doEval) eval = EvaluateBoard.eval(hypo_iBoard, updatedState);
                        list.add(new IBoardState(hypo_iBoard, updatedState, adaptedMoveNotation, adaptedMoveNotation,
                                eval));
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

    ArrayList<IBoardState> getMinMaxList(int depth, int nMoves) {
        //System.out.println("gMML: " + depth + " X " + nMoves);
        ArrayList<IBoardState> list = new ArrayList<>();
        double[] ival = new double[nMaxBranch];
        for (int i=0; i<nMaxBranch; i++) ival[i]=+99999999;

        int nbranch = nMaxBranch;

        final boolean wikitest = false;

        if ( wikitest ) {
            if (depth == 3) nbranch = 3;
            if (depth == 2) nbranch = 2;
            if (depth == 1) {
                if (nMoves == 11) nbranch = 2;
                if (nMoves == 12) nbranch = 1;
                if (nMoves == 21) nbranch = 2;
                if (nMoves == 22) nbranch = 1;
                if (nMoves == 31) nbranch = 1;
                if (nMoves == 32) nbranch = 2;
            }
            if (depth == 0) {
                if (nMoves == 111) nbranch = 2;
                if (nMoves == 112) nbranch = 3;
                if (nMoves == 121) nbranch = 1;
                if (nMoves == 211) nbranch = 1;
                if (nMoves == 212) nbranch = 2;
                if (nMoves == 221) nbranch = 1;
                if (nMoves == 311) nbranch = 1;
                if (nMoves == 321) nbranch = 2;
                if (nMoves == 322) nbranch = 1;
            }

            if (depth == 0) {
//            ival[0]=1; ival[1]=2; ival[2]=3;
                if (nMoves == 111) {
                    ival[0] = 5;
                    ival[1] = 6;
                }
                if (nMoves == 112) {
                    ival[0] = 7;
                    ival[1] = 4;
                    ival[2] = 5;
                }
                if (nMoves == 121) {
                    ival[0] = 3;
                }
                if (nMoves == 211) {
                    ival[0] = 6;
                }
                if (nMoves == 212) {
                    ival[0] = 6;
                    ival[1] = 9;
                }
                if (nMoves == 221) {
                    ival[0] = 7;
                }
                if (nMoves == 311) {
                    ival[0] = 5;
                }
                if (nMoves == 321) {
                    ival[0] = 9;
                    ival[1] = 8;
                }
                if (nMoves == 322) {
                    ival[0] = 6;
                }
            }
        } else { //random tree
            if (depth==0) {
                Random rnd = new Random(nMoves);
                for (int i = 0; i < ival.length; i++) {
//                    ival[i] = rnd.nextDouble() * 10 - 5;
                    ival[i] = rnd.nextInt(10) - 5;
                }
                //if (rnd.nextInt(10)<3) nbranch-=1;
            }
            nbranch = nMaxBranch;
            Random rnd = new Random(nMoves);

            if (rnd.nextInt(10)<3) nbranch-=1;

        }


        for (int i = 1; i <= nbranch; i++) {
            IBoardState _iBS = new IBoardState();
            _iBS.state.moveNumber = i;
            _iBS.state.nMoves = nMoves * 10 + i;
            _iBS.setEval((float)ival[i-1]);
            _iBS.state.turnOf = ChessBoard.WHITE * (int) Math.pow(-1, depth);
            list.add(_iBS);
            tree.add(_iBS.state.nMoves);
            vals.add( (double) _iBS.getEval() );
        }
        return list;
    }

    void doMinMaxTest() {
        IBoardState _iBoard = new IBoardState();
        nMaxBranch = 3;
        ArrayList<IBoardState> allMoves = allLegalMoves(_iBoard, _iBoard.state, false, 3, true, -INF, +INF);
        //for (IBoardState board : allMoves) System.out.println("### VALUE: " + board.getEval() + " " + board.getNotation());
        IBoardState maxMove = EvaluateBoard.getMaxMove(allMoves, false, false);
        tree.add(0, 0);
        vals.add(0,  (double)maxMove.getEval() );

        System.out.println("nALMCalls = " + nALMCalls);
        System.out.println("Move chosen: " + maxMove.state.nMoves + "   with path: " + maxMove.getEval() );
        //System.out.println("chosenMove =\n" + chosenMove + "nLegalMoves=" + allMoves.size() + " val=" + chosenMove.getEval() + " M=" + chosenMove.state.turnOf);
    }

}
