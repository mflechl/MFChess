package com.mflechl.mfchess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public final class Move {
    Move() {
    }

    //static final boolean PICK_RANDOM = false;

    static final boolean USE_ALPHABETA = true;
    static final boolean USE_NEGAMAX = false;        //if false, use regular alphabeta (if that is true), otherwise negamax instead.
    static final boolean USE_PVS = !USE_NEGAMAX;            //if false, use regular alphabeta (if that is true), otherwise principal variation search instead. NEGAMAX and PVS should not be true at the same time!
    static final boolean USE_ORDERING = true;
    static final int DEFAULT_START_DEPTH = 4;
    static final int MAX_DEPTH = 4;

    static final int[][] knightMoves = {{1, 2}, {2, 1}, {-1, 2}, {2, -1}, {-2, 1}, {1, -2}, {-1, -2}, {-2, -1}};
    static final int[][] rookMoves = {{-1, 0}, {1, 0}, {0, -1}, {0, +1}};

    private int startDepth = DEFAULT_START_DEPTH;

    private static final int INF = 1000000;
    static int nALMCalls = 0;

    //    public static SpecialMove sDummy = new SpecialMove();
    public final static SpecialMove SDUMMY = new SpecialMove();

    private IBoardState bestMove;

    private IBoard mBoard;
    private IState mState;

    volatile boolean stopBestMove = false;

    public void setStartDepth(int startDepth) {
        this.startDepth = startDepth;
    }

    //move from fromLine, fromRow to toLine,toRow legal?
    static boolean isLegal(IBoard _iBoard, SpecialMove sMove, int fromLine, int fromRow, int toLine, int toRow) {
        return isLegal(_iBoard, sMove, fromLine, fromRow, toLine, toRow, ChessBoard.currentStaticState);
    }

    static boolean isLegal(IBoard _iBoard, SpecialMove sMove, int fromLine, int fromRow, int toLine, int toRow, IState _state) {
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

    //this duplicates some code, but would otherwise need to get entire lists of moves => too slow
    static boolean underAttack(IBoard board, int col, int line, int row) {  //is square line/row under attack by player col

        //System.out.println("uA "+line+" "+row+" "+col+board);

        //rook moves
        int fromLine, fromRow;
        for (int[] rookMove : rookMoves) {
            for (int d = 1; d <= 8; d++) {
                fromLine = line + d * rookMove[0];
                fromRow = row + d * rookMove[1];
                if (fromLine < 0 || fromLine > 7 || fromRow < 0 || fromRow > 7) break;

                int toPieceCol = board.setup[fromLine][fromRow] * col;
                if (toPieceCol == ChessBoard.ROOK || toPieceCol == ChessBoard.QUEEN) return true;
                else if (toPieceCol != 0) break;

            }
        }
        //bishop moves
        for (int il = -1; il <= 1; il += 2) {
            for (int ir = -1; ir <= 1; ir += 2) {
                for (int d = 1; d <= 8; d++) {
                    fromLine = line + d * il;
                    fromRow = row + d * ir;
                    if (fromLine < 0 || fromLine > 7 || fromRow < 0 || fromRow > 7) break;

                    int toPieceCol = board.setup[fromLine][fromRow] * col;
                    if (toPieceCol == ChessBoard.BISHOP || toPieceCol == ChessBoard.QUEEN) return true;
                    else if (toPieceCol != 0) break;

                }
            }
        }
        //knight moves
        for (int[] knightMove : knightMoves) {
            fromLine = line + knightMove[0];
            fromRow = row + knightMove[1];
            if (fromLine >= 0 && fromLine <= 7 && fromRow >= 0 && fromRow <= 7) {
                if (board.setup[fromLine][fromRow] * col == ChessBoard.KNIGHT) return true;
            }
        }

        return false;
    }

    static ArrayList<Ply> listAllMoves(final IBoard board, final IState state) {
        nALMCalls++; //TEST

        ArrayList<Ply> plies = new ArrayList<>();

        for (int il = 0; il < 8; il++) {
            for (int ir = 0; ir < 8; ir++) {
                if (board.setup[il][ir] * state.turnOf > 0) {
                    plies.addAll(listAllMovesSquare(board, state, il, ir));
                }
            }
        }

        return plies;

    }

    static ArrayList<Ply> listAllMovesSquare(final IBoard board, final IState state, int fromLine, int fromRow) {
        ArrayList<Ply> plies = new ArrayList<>(); //TODO: check if it is faster to allocate a size here
        int piece = board.setup[fromLine][fromRow];
        int col = Integer.signum(piece);
        int colIndex = (col == ChessBoard.WHITE) ? 1 : 0; //black=0, white=1
        piece = Math.abs(piece);
        int kLine = (col == ChessBoard.WHITE) ? 0 : 7;

        int toPiece;
        int toLine, toRow;

        switch (piece) {

            case ChessBoard.PAWN:
                //move one square
                if (board.setup[fromLine + col][fromRow] == 0) {
                    plies.add(new Ply(fromLine, fromRow, fromLine + col, fromRow, 0, false, state.castlingPossibleK, state.castlingPossibleQ, col));
                }
                //move two squaes
                if (((col == ChessBoard.WHITE && fromLine == 1) || (col == ChessBoard.BLACK && fromLine == 6)) &&
                        (board.setup[fromLine + 2 * col][fromRow] == 0)) {
                    if (board.setup[fromLine + col][fromRow] == 0) //no piece in between
                        plies.add(new Ply(fromLine, fromRow, fromLine + 2 * col, fromRow, 0, false, state.castlingPossibleK, state.castlingPossibleQ, col));
                }
                //eliminate or en-passant
                for (int i = -1; i <= 1; i += 2) {
                    if (fromRow + i >= 0 && fromRow + i <= 7) {
                        toPiece = board.setup[fromLine + col][fromRow + i];
                        if (toPiece * col < 0) {
                            plies.add(new Ply(fromLine, fromRow, fromLine + col, fromRow + i, toPiece, false, state.castlingPossibleK, state.castlingPossibleQ, col));
                        } else if (state.enPassantPossible == fromRow + i) { //en passant?
                            if ((fromLine == 4 && col == ChessBoard.WHITE) || (fromLine == 3 && col == ChessBoard.BLACK)) {
                                //sMove.enPassant = true; //SETTING ENPASSANT
                                plies.add(new Ply(fromLine, fromRow, fromLine + col, fromRow + i, toPiece, true, state.castlingPossibleK, state.castlingPossibleQ, col));
                            }
                        }
                    }
                }
                break;

            case ChessBoard.KNIGHT:
                for (int[] knightMove : knightMoves) {
                    toLine = fromLine + knightMove[0];
                    toRow = fromRow + knightMove[1];
                    if (toLine >= 0 && toLine <= 7 && toRow >= 0 && toRow <= 7) {
                        toPiece = board.setup[toLine][toRow];
                        if (toPiece * col <= 0)
                            plies.add(new Ply(fromLine, fromRow, toLine, toRow, toPiece, false, state.castlingPossibleK, state.castlingPossibleQ, col));
                    }
                }
                break;

            case ChessBoard.BISHOP:
            case ChessBoard.QUEEN:
                for (int il = -1; il <= 1; il += 2) {
                    for (int ir = -1; ir <= 1; ir += 2) {
                        for (int d = 1; d <= 8; d++) {
                            toLine = fromLine + d * il;
                            toRow = fromRow + d * ir;
                            if (!(toLine >= 0 && toLine <= 7 && toRow >= 0 && toRow <= 7)) break;
                            toPiece = board.setup[toLine][toRow];
                            if (toPiece * col > 0) break; //own piece, give up that direction
                            plies.add(new Ply(fromLine, fromRow, toLine, toRow, toPiece, false, state.castlingPossibleK, state.castlingPossibleQ, col));
                            if (toPiece * col != 0) break; //enemy piece, cannot go farther
                        }
                    }

                }
                if (piece == ChessBoard.BISHOP) break; //continue with rook-moves if it is a queen!

            case ChessBoard.ROOK: //TODO: update castling state
                for (int[] rookMove : rookMoves) {
                    for (int d = 1; d <= 8; d++) {
                        toLine = fromLine + d*rookMove[0];
                        toRow = fromRow + d*rookMove[1];
                        if (!(toLine >= 0 && toLine <= 7 && toRow >= 0 && toRow <= 7)) break;
                        toPiece = board.setup[toLine][toRow];
                        if (toPiece * col > 0) break; //own piece, give up that direction
                        plies.add(new Ply(fromLine, fromRow, toLine, toRow, toPiece, false, state.castlingPossibleK, state.castlingPossibleQ, col));
                        if (toPiece * col != 0) break; //enemy piece, cannot go farther
                    }

                }
                break;

            case ChessBoard.KING:
//                int colIndex = (col + 1) / 2; //black=0, white=1
                for (int il = -1; il <= 1; il++) {
                    for (int ir = -1; ir <= 1; ir++) {
                        if (il == 0 && ir == 0) continue; //no move
                        toLine = fromLine + il;
                        toRow = fromRow + ir;
                        if (!(toLine >= 0 && toLine <= 7 && toRow >= 0 && toRow <= 7)) break;
                        toPiece = board.setup[toLine][toRow];
                        if (toPiece * col > 0) continue; //own piece
                        Ply p = new Ply(fromLine, fromRow, toLine, toRow, toPiece, false, state.castlingPossibleK, state.castlingPossibleQ, col);
                        if (state.castlingPossibleK[colIndex]) p.toggleCastlingPossK(colIndex);
                        if (state.castlingPossibleQ[colIndex]) p.toggleCastlingPossQ(colIndex);
                        plies.add(p);
                    }
                }

                //castling
                if (state.check) break;

                if (state.castlingPossibleK[colIndex]) {
                    if (board.setup[kLine][5] == 0 && board.setup[kLine][6] == 0) {
                        if (!underAttack( board,-1 * col, kLine, 5) && !underAttack( board,-1 * col, kLine, 6)) {
                            Ply p = new Ply(fromLine, fromRow, fromLine, fromRow + 2, 0, false, state.castlingPossibleK, state.castlingPossibleQ, col);
                            if (state.castlingPossibleK[colIndex]) p.toggleCastlingPossK(colIndex);
                            if (state.castlingPossibleQ[colIndex]) p.toggleCastlingPossQ(colIndex);
                            plies.add(p);
                        }
                    }
                }
                if (state.castlingPossibleQ[colIndex]) {
                    if (board.setup[kLine][1] == 0 && board.setup[kLine][2] == 0 && board.setup[kLine][3] == 0) {
                        if (!underAttack( board, -1 * col, kLine, 1) && !underAttack( board,-1 * col, kLine, 2) && !underAttack( board,-1 * col, kLine, 3)) {
                            Ply p = new Ply(fromLine, fromRow, fromLine, fromRow - 2, 0, false, state.castlingPossibleK, state.castlingPossibleQ, col);
                            if (state.castlingPossibleK[colIndex]) p.toggleCastlingPossK(colIndex);
                            if (state.castlingPossibleQ[colIndex]) p.toggleCastlingPossQ(colIndex);
                            plies.add(p);
                        }
                    }
                }
                break;

        }//end switch

        //for (Ply ply: plies){
        Iterator itr = plies.iterator();
        Ply ply;
        while (itr.hasNext())
        {
            ply=(Ply)itr.next();
            //castling: rook eliminated
            if ( ply.getToPiece() == ChessBoard.ROOK && ply.getToLine()==(7-kLine) ){  //"other" king line!
                if ( ply.getToRow()==0 && state.castlingPossibleQ[1-colIndex] ) ply.toggleCastlingPossQ(1-colIndex);
                if ( ply.getToRow()==7 && state.castlingPossibleK[1-colIndex] ) ply.toggleCastlingPossK(1-colIndex);
            }

            //check
            //apply ply to board
            doPly( board, state, ply );
            int[] lrKing=findKing(board,col);

            //check for check
            if ( underAttack(board,-col,lrKing[0],lrKing[1]) ) itr.remove();

            //take back ply
            undoPly( board, state, ply );
        }

        return plies;
    }

    static int[] findKing(IBoard board, int col){

        int[] lrKing={-1,-1};
        for (int il = 0; il < 8; il++) {
            for (int ir = 0; ir < 8; ir++) {
                if (board.setup[il][ir] == col * ChessBoard.KING) {
                    lrKing[0] = il;
                    lrKing[1] = ir;
                    return lrKing;
                }
            }
        }

        return lrKing;
    }

    private static void doPly( IBoard board, IState state, Ply ply ) {
        //System.out.println("D ply= "+ply);

        state.update(0,0,0, 0);
        if ( ply.toggleCastlingPossK[0] ) state.castlingPossibleK[0]=!state.castlingPossibleK[0];
        if ( ply.toggleCastlingPossK[1] ) state.castlingPossibleK[1]=!state.castlingPossibleK[1];
        if ( ply.toggleCastlingPossQ[0] ) state.castlingPossibleQ[0]=!state.castlingPossibleQ[0];
        if ( ply.toggleCastlingPossQ[1] ) state.castlingPossibleQ[1]=!state.castlingPossibleQ[1];

        //normal move
        if ( ply.getToLine()<0 || ply.getToLine() > 7 || ply.getToRow()<0 || ply.getToRow()>7 ||
             ply.getFromLine()<0 || ply.getFromLine() > 7 || ply.getFromRow()<0 || ply.getFromRow()>7 )
            throw new ArrayIndexOutOfBoundsException("ply = "+ply+board+state);
        board.setup[ply.getToLine()][ply.getToRow()] = board.setup[ply.getFromLine()][ply.getFromRow()];
        board.setup[ply.getFromLine()][ply.getFromRow()] = 0;

        //castling
        if ( board.setup[ply.getFromLine()][ply.getFromRow()] == ChessBoard.KING && Math.abs(ply.getFromRow()-ply.getToRow())>=2 ){
            System.out.println("CASTLING");
            int rookToRow = -1, rookFromRow = -1;
            if (ply.getToRow() == 2) {
                rookToRow = 3;
                rookFromRow = 0;
            }
            if (ply.getToRow() == 6) {
                rookToRow = 5;
                rookFromRow = 7;
            }
            board.setup[ply.getToLine()][rookToRow]=board.setup[ply.getToLine()][rookFromRow];
            board.setup[ply.getToLine()][rookFromRow]=0;
        }

        //en-passant
        else if ( ply.enPassant ){
            System.out.println("ENPASSANT");
            board.setup[ply.getToLine()][ply.getToRow()-ply.getMoverColor()]=0;
        }

        //promotion
        else if (ply.getToLine() == 7 * (ply.getMoverColor()+1)/2 && board.setup[ply.getFromLine()][ply.getFromRow()] == ChessBoard.PAWN) {
            System.out.println("PROMOTION");
            board.setup[ply.getToLine()][ply.getToRow()] = (byte) ( ChessBoard.QUEEN*ply.getMoverColor() );
        }

    }

    private static void undoPly( IBoard board, IState state, Ply ply ){
        //System.out.println("U ply= "+ply);

        state.undoUpdate(0,0,0);
        if ( ply.toggleCastlingPossK[0] ) state.castlingPossibleK[0]=!state.castlingPossibleK[0];
        if ( ply.toggleCastlingPossK[1] ) state.castlingPossibleK[1]=!state.castlingPossibleK[1];
        if ( ply.toggleCastlingPossQ[0] ) state.castlingPossibleQ[0]=!state.castlingPossibleQ[0];
        if ( ply.toggleCastlingPossQ[1] ) state.castlingPossibleQ[1]=!state.castlingPossibleQ[1];

        //normal move
        board.setup[ply.getFromLine()][ply.getFromRow()] = board.setup[ply.getToLine()][ply.getToRow()];
        board.setup[ply.getToLine()][ply.getToRow()] = (byte)ply.getToPiece();

        //castling
        if ( board.setup[ply.getToLine()][ply.getToRow()] == ChessBoard.KING && Math.abs(ply.getFromRow()-ply.getToRow())>=2 ){
            System.out.println("uCASTLING");
            int rookToRow = -1, rookFromRow = -1;
            if (ply.getToRow() == 2) {
                rookToRow = 3;
                rookFromRow = 0;
            }
            if (ply.getToRow() == 6) {
                rookToRow = 5;
                rookFromRow = 7;
            }
            board.setup[ply.getToLine()][rookFromRow]=board.setup[ply.getToLine()][rookToRow];
            board.setup[ply.getToLine()][rookToRow]=0;
        }

        //en-passant
        else if ( ply.enPassant ){
            System.out.println("uENPASSANT");
            board.setup[ply.getToLine()][ply.getToRow()-ply.getMoverColor()]=(byte) (- ChessBoard.PAWN*ply.getMoverColor() );
        }

        //promotion
        else if (ply.getToLine() == 7 * (ply.getMoverColor()+1)/2 && board.setup[ply.getToLine()][ply.getToRow()] == ChessBoard.PAWN) {
            System.out.println("uPROMOTION");
            board.setup[ply.getFromLine()][ply.getFromRow()] = (byte) ( ChessBoard.PAWN*ply.getMoverColor() );
        }

    }

    private static boolean legalMovePawn(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow, int col, SpecialMove sMove, IState _state) {
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

    private static boolean legalMoveKing(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow, int col, SpecialMove sMove, IState _state) {
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

    private static boolean emptyBetween(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow, boolean checkCheck, IState _state) {

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

    IBoardState bestMove(IBoardState iBoardState) {

        System.out.println("bestMove: nextBestMove = " + ChessBoard.nextBestMove );

        bestMove = null;
        int eval;

        mBoard = new IBoard(iBoardState);
        mState = new IState(iBoardState.state);

        eval = pvs2(iBoardState.state.turnOf, startDepth, -INF, +INF);

        if (ChessBoard.USE_THREAD && ChessBoard.moveThread.move.stopBestMove) return null;

        if ( bestMove == null ){
            throw new NullPointerException("bestMove: No possible moves. " + eval );
        } else{
            bestMove.setEval(eval);
            bestMove.setNotation(bestMove.getNotation()+" "+bestMove.getNextMovesNotation());
            int[] lrKing=findKing(bestMove,bestMove.state.turnOf);
            //check for check
            if ( underAttack(bestMove,-bestMove.state.turnOf,lrKing[0],lrKing[1]) ) bestMove.state.check = true;
            else bestMove.state.check = false;

            //System.out.println("BEST MOVE eval="+eval+bestMove);
            return bestMove;
        }
    }


    IBoardState bestMove2(IBoardState iBoardState) {

        System.out.println("bestMove: nextBestMove = " + ChessBoard.nextBestMove );

        bestMove = null;
        int eval;

        if (USE_NEGAMAX) eval = negaMax(iBoardState.state.turnOf, startDepth, -INF, +INF, iBoardState);
        else if (USE_PVS) eval = pvs(iBoardState.state.turnOf, startDepth, -INF, +INF, iBoardState);
        else if (USE_ALPHABETA) {
            if (iBoardState.state.turnOf == ChessBoard.WHITE) eval = maxMove(startDepth, -INF, +INF, iBoardState);
            else eval = minMove(startDepth, -INF, +INF, iBoardState);
        }

        if (ChessBoard.USE_THREAD && ChessBoard.moveThread.move.stopBestMove) return null;

        if ( bestMove == null ){
            throw new NullPointerException("bestMove: No possible moves. " + eval );
        } else{
            bestMove.setEval(eval);
            return bestMove;
        }
    }

    int pvs2(int color, int depth, int alpha, int beta){
        if (ChessBoard.USE_THREAD && ChessBoard.moveThread.move.stopBestMove) return -9997;

        if ( depth==0 ) return EvaluateBoard.eval(mBoard, mState)*color;

        int maxValue = alpha;
        String thisNotation="";
        boolean isFirstMove = true;
        //ArrayList<IBoardState> moveList = allLegalMoves(currBoardState);
        ArrayList<Ply> moveList = listAllMoves(mBoard, mState);
        if ( depth == startDepth && USE_ORDERING ){
            //System.out.println( "A " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
            //TsortList(moveList); //TODO
            //System.out.println( "B " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
        }

//        for ( IBoardState board : moveList ){
        for ( Ply ply : moveList ){
            int value;
            doPly(mBoard, mState, ply);
            //System.out.println("D  d="+depth+" "+mBoard);
            if ( isFirstMove ) value = -pvs2(-color, depth-1, -beta, -maxValue);
            else{
                value = -pvs2(-color, depth-1, -maxValue - 1, -maxValue);
//                if ( maxValue < value && value < beta ) value = -pvs(-color, depth-1, -beta, -value, board);
                if ( maxValue < value && value < beta ){
                    value = -pvs2(-color, depth-1, -beta, -maxValue);
                    //board.setNextMoveNotation(board.getNextMovesNotation().replaceAll(" $",""));
                }
            }
            //undoPly(mBoard, mState, ply);
            isFirstMove = false;
            if ( value > maxValue ){
                maxValue = value;
                //thisNotation = ""; //TODO board.getNextMovesNotation();
                if ( USE_ALPHABETA && maxValue >= beta ) {
                    undoPly(mBoard, mState, ply);
                    break;
                }
                if ( depth == startDepth) {
                    bestMove = new IBoardState(mBoard, mState);
                    bestMove.setNextMoveNotation(Notation.getMoveNotation(mBoard,mState,ChessBoard.currentStaticState,ply.getFromLine(),ply.getFromRow(),ply.getToLine(),ply.getToRow(),mBoard.setup[ply.getToLine()][ply.getToRow()],ply.getToPiece(),new SpecialMove()));
                    //bestMove.setNotation("YY");
                }
            }
            undoPly(mBoard, mState, ply);


        }
        //if ( !thisNotation.equals("") ) currBoardState.setNextMoveNotation( currBoardState.getNextMovesNotation() + " " + thisNotation );
        return maxValue;
    }

    int pvs(int color, int depth, int alpha, int beta, IBoardState currBoardState){
        if (ChessBoard.USE_THREAD && ChessBoard.moveThread.move.stopBestMove) return -9997;

        if ( depth==0 ) return EvaluateBoard.eval(currBoardState, currBoardState.state)*color;

        int maxValue = alpha;
        String thisNotation="";
        boolean isFirstMove = true;
        ArrayList<IBoardState> moveList = allLegalMoves(currBoardState);
        if ( depth == startDepth && USE_ORDERING ){
            //System.out.println( "A " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
            sortList(moveList);
            //System.out.println( "B " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
        }

        for ( IBoardState board : moveList ){
            int value;
            if ( isFirstMove ) value = -pvs(-color, depth-1, -beta, -maxValue, board);
            else{
                value = -pvs(-color, depth-1, -maxValue - 1, -maxValue, board);
//                if ( maxValue < value && value < beta ) value = -pvs(-color, depth-1, -beta, -value, board);
                if ( maxValue < value && value < beta ){
                    value = -pvs(-color, depth-1, -beta, -maxValue, board);
                    //board.setNextMoveNotation(board.getNextMovesNotation().replaceAll(" $",""));
                }
            }
            isFirstMove = false;
            if ( value > maxValue ){
                maxValue = value;
                thisNotation = board.getNextMovesNotation();
                if ( USE_ALPHABETA && maxValue >= beta )
                    break;
                if ( depth == startDepth)
                    bestMove = new IBoardState( board );
            }
        }
        if ( !thisNotation.equals("") ) currBoardState.setNextMoveNotation( currBoardState.getNextMovesNotation() + " " + thisNotation );
        return maxValue;
    }

    int negaMax(int color, int depth, int alpha, int beta, IBoardState currBoardState){
        if (ChessBoard.USE_THREAD && ChessBoard.moveThread.move.stopBestMove) return -9997;

        if ( depth==0 ) return EvaluateBoard.eval(currBoardState, currBoardState.state)*color;

        int maxValue = alpha;
        String thisNotation="";
        ArrayList<IBoardState> moveList = allLegalMoves(currBoardState);
        if ( depth == startDepth && USE_ORDERING ){
            //System.out.println( "A " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
            sortList(moveList);
            //System.out.println( "B " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
        }

        for ( IBoardState board : moveList ){
            int value = -negaMax(-color, depth-1, -beta, -maxValue, board);
            if ( value > maxValue ){
                maxValue = value;
                thisNotation = board.getNextMovesNotation();
                if ( USE_ALPHABETA && maxValue >= beta )
                    break;
                if ( depth == startDepth)
                    bestMove = new IBoardState( board );
            }
        }
        currBoardState.setNextMoveNotation( currBoardState.getNextMovesNotation() + " " + thisNotation);
        return maxValue;
    }

    int maxMove(int depth, int alpha, int beta, IBoardState currBoardState){
        if (ChessBoard.USE_THREAD && ChessBoard.moveThread.move.stopBestMove) return -9997;

        if ( depth==0 ) return EvaluateBoard.eval(currBoardState, currBoardState.state);

        int maxValue = alpha;
        String thisNotation="";
        ArrayList<IBoardState> moveList = allLegalMoves(currBoardState);
        if ( depth == startDepth && USE_ORDERING ){
            //System.out.println( "A " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
            sortList(moveList);
            //System.out.println( "B " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
        }

        for ( IBoardState board : moveList ){
            int value = minMove(depth-1, maxValue, beta, board);
            if ( value > maxValue ){
                maxValue = value;
                thisNotation = board.getNextMovesNotation();
                if ( USE_ALPHABETA && maxValue >= beta )
                    break;
                if ( depth == startDepth)
                    bestMove = new IBoardState( board );
            }
        }
        currBoardState.setNextMoveNotation( currBoardState.getNextMovesNotation() + " " + thisNotation);
        return maxValue;
    }

    int minMove(int depth, int alpha, int beta, IBoardState currBoardState){
        if (ChessBoard.USE_THREAD && ChessBoard.moveThread.move.stopBestMove) return +9997;

        if ( depth==0 ) return EvaluateBoard.eval(currBoardState, currBoardState.state);

        int minValue = beta; //minValue
        String thisNotation="";
        ArrayList<IBoardState> moveList = allLegalMoves(currBoardState);
        if ( depth == startDepth && USE_ORDERING){
            //System.out.println( "A " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
            sortList(moveList);
            //System.out.println( "B " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
        }

        for ( IBoardState board : moveList ){
            int value = maxMove(depth-1, alpha, minValue, board);
            if ( value < minValue ){
                minValue = value;
                thisNotation = board.getNextMovesNotation();
                if ( USE_ALPHABETA && minValue <= alpha )
                    break;
                if ( depth == startDepth)
                    bestMove = new IBoardState( board );
            }
        }
        currBoardState.setNextMoveNotation( currBoardState.getNextMovesNotation() + " " + thisNotation);
        return minValue;
    }

    void sortList(ArrayList<IBoardState> list){
        int indBestMove = -1;
        for ( int i=0; i<list.size(); i++ ){
            //System.out.println("sortList: |" + board.getNotation().replaceAll("^\\d+\\. ","") + "|   |" + ChessBoard.nextBestMove + "| " +
            //        board.getNotation().replaceAll("^\\d+\\. ","").equals(ChessBoard.nextBestMove));
            if ( list.get(i).getNotation().replaceAll("^\\d+\\. ","").equals(ChessBoard.nextBestMove)){
                indBestMove = i;
                break;
            }
        }
        if ( list.size() < 2 || indBestMove<0 ) return;
        Collections.swap(list, 0, indBestMove);
    }

    ArrayList<IBoardState> allLegalMoves(IBoardState iBoardState) {
        nALMCalls++; //TEST

        ArrayList<IBoardState> list = new ArrayList<>();

        for (int il = 0; il < 8; il++) {
            for (int ir = 0; ir < 8; ir++) {
                if (iBoardState.setup[il][ir] * iBoardState.state.turnOf > 0) {
                    ArrayList<IBoardState> listPiece = pieceLegalMove(iBoardState, il, ir, iBoardState.state, false, true, false );
//                    ArrayList<IBoardState> listPiece = pieceLegalMove(iBoard, il, ir, state, false, true, false );
                    list.addAll(listPiece);
                }
            }
        }

        return list;
    }


    Boolean noLegalMoves(IBoard iBoard, IState state) {

        ArrayList<IBoardState> list = new ArrayList<>();

        for (int il = 0; il < 8; il++) {
            for (int ir = 0; ir < 8; ir++) {
                if (iBoard.setup[il][ir] * state.turnOf > 0) {
                    ArrayList<IBoardState> listPiece = pieceLegalMove(iBoard, il, ir, state, true, false, false );
                    list.addAll(listPiece);
                    if ( ! list.isEmpty() ) return false;
                }
            }
        }

        return true;
    }

    ArrayList<IBoardState> pieceLegalMove(IBoard _iBoard, int fromLine, int fromRow, IState _state, boolean stopAfterFirst, boolean updateNotation, boolean doEval) {
        ArrayList<IBoardState> list = new ArrayList<>();
        int eval = -1* -99998 * _state.turnOf;
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

                    IState updatedState = new IState(_state);
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

    static ArrayList<int[]> legalDestination(IBoard _iBoard, int fromLine, int fromRow, IState _state, boolean stopAfterFirst) {
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

    void updateCheckState(IState state, IBoard iBoard) {
        state.check = isChecked(iBoard, state.turnOf); //check of opponent result of the move?
        if (noLegalMoves(iBoard, state)) {
            if (state.check) state.mate = true;
            else state.remis = true;
        }
    }

}
