package com.mflechl.mfchess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public final class Move {
    Move() {
    }

    //static final boolean PICK_RANDOM = false;
    static boolean USE_PLY_METHOD = true;

    static final boolean USE_ALPHABETA = true;
    static final boolean USE_NEGAMAX = false;        //if false, use regular alphabeta (if that is true), otherwise negamax instead.
    static final boolean USE_PVS = !USE_NEGAMAX;            //if false, use regular alphabeta (if that is true), otherwise principal variation search instead. NEGAMAX and PVS should not be true at the same time!
    static final boolean USE_ORDERING = true; //DEFAULT: true!
    static final int DEFAULT_START_DEPTH = 5;
    static final int MAX_DEPTH = 5;

    static final int[][] knightMoves = {{1, 2}, {2, 1}, {-1, 2}, {2, -1}, {-2, 1}, {1, -2}, {-1, -2}, {-2, -1}};
    static final int[][] rookMoves = {{-1, 0}, {1, 0}, {0, -1}, {0, +1}};

    private int startDepth = DEFAULT_START_DEPTH;

    private static final int INF = 1000000;
    static int nALMCalls = 0;

    //    public static SpecialMove sDummy = new SpecialMove();
    public final static SpecialMove SDUMMY = new SpecialMove();

    private IBoardState bestMove;

    String beyondMoves = "";
    String nextMove = "";

    private IBoard mBoard;
    private IState mState;

    volatile boolean stopBestMove = false;

    public void setStartDepth(int startDepth) {
        this.startDepth = startDepth;
    }
    public int getStartDepth() {
        return startDepth;
    }

    //move from fromLine, fromRow to toLine,toRow legal?
    static boolean isLegal(IBoard _iBoard, SpecialMove sMove, int fromLine, int fromRow, int toLine, int toRow) {
        return isLegal(_iBoard, sMove, fromLine, fromRow, toLine, toRow, ChessBoard.currentStaticState);
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
        //pawn moves
        for (int ir = -1; ir <= 1; ir += 2) {
            fromRow = row + ir;
            fromLine = line - col;
            if (fromLine < 0 || fromLine > 7 || fromRow < 0 || fromRow > 7) break;
            if (board.setup[fromLine][fromRow] * col == ChessBoard.PAWN) return true;
        }
        //king moves
        for (int il = -1; il <= 1; il++) {
            for (int ir = -1; ir <= 1; ir++) {
                if (il == 0 && ir == 0) continue; //no move
                fromRow = row + ir;
                fromLine = line + il;
                if (fromLine < 0 || fromLine > 7 || fromRow < 0 || fromRow > 7) break;
                if (board.setup[fromLine][fromRow] * col == ChessBoard.KING) return true;
            }
        }

        return false;
    }

    static ArrayList<Ply> orderList(ArrayList<Ply> plies) {
        ArrayList<Ply> orderedPlies = new ArrayList<>();
        for (Ply ply : plies) {
            int i = 0;
            for (Ply oPly : orderedPlies) {
                if (ply.getToLine() < oPly.getToLine()) break;
                if (ply.getToLine() == oPly.getToLine() && ply.getToRow() < oPly.getToRow()) break;
                i++;
            }
            orderedPlies.add(i, ply);
        }

        return orderedPlies;
    }

    static ArrayList<Ply> listAllMoves(final IBoard board, final IState state) {
        nALMCalls++; //TEST

        ArrayList<Ply> plies = new ArrayList<>();

        for (int il = 0; il < 8; il++) {
            for (int ir = 0; ir < 8; ir++) {
                if (board.setup[il][ir] * state.turnOf > 0) {
                    //plies.addAll(listAllMovesSquare(board, state, il, ir));
                    plies.addAll(orderList(listAllMovesSquare(board, state, il, ir, false)));
                }
            }
        }

        //System.out.println("  Ply");
        //if (tmp) for (Ply ply : plies) System.out.println(ply);

        return plies;

    }

    boolean noMoreMoveLeft(final IBoard board, final IState state) {
        nALMCalls++; //TEST

        for (int il = 0; il < 8; il++) {
            for (int ir = 0; ir < 8; ir++) {
                if (board.setup[il][ir] * state.turnOf > 0) {
                    if ( ! listAllMovesSquare(board, state, il, ir, true).isEmpty() ) return false;
                }
            }
        }

        return true;

    }


    static ArrayList<Ply> listAllMovesSquare(final IBoard board, final IState state, int fromLine, int fromRow, boolean stopAfterFirst) {
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
                if (fromLine+col > 7){
                    break;
                }
                if (board.setup[fromLine + col][fromRow] == 0) {
                    Ply ply = new Ply(fromLine, fromRow, fromLine + col, fromRow, 0, false, col, state.enPassantPossible);
                    if (ply.getToLine() == 7 * colIndex) ply.togglePromotion();
                    plies.add(ply);
                    if (stopAfterFirst && checkAndCastling(board,state,plies,kLine,col,colIndex,plies.size()-1) ) return plies;
                }
                //move two squares
                if (((col == ChessBoard.WHITE && fromLine == 1) || (col == ChessBoard.BLACK && fromLine == 6)) &&
                        (board.setup[fromLine + 2 * col][fromRow] == 0)) {
                    if (board.setup[fromLine + col][fromRow] == 0) { //no piece in between
                        plies.add(new Ply(fromLine, fromRow, fromLine + 2 * col, fromRow, 0, false, col, state.enPassantPossible));
                        if (stopAfterFirst && checkAndCastling(board, state, plies, kLine, col, colIndex, plies.size() - 1))
                            return plies;
                    }
                }
                //eliminate or en-passant
                for (int i = -1; i <= 1; i += 2) {
                    if (fromRow + i >= 0 && fromRow + i <= 7) {
                        toPiece = board.setup[fromLine + col][fromRow + i];
                        if (toPiece * col < 0) {
                            Ply ply = new Ply(fromLine, fromRow, fromLine + col, fromRow + i, toPiece, false, col, state.enPassantPossible);
                            if (ply.getToLine() == 7 * colIndex) ply.togglePromotion();
                            plies.add( ply );
                            if (stopAfterFirst && checkAndCastling(board,state,plies,kLine,col,colIndex,plies.size()-1) ) return plies;
                        } else if (state.enPassantPossible == fromRow + i) { //en passant?
                            if ((fromLine == 4 && col == ChessBoard.WHITE) || (fromLine == 3 && col == ChessBoard.BLACK)) {
                                plies.add(new Ply(fromLine, fromRow, fromLine + col, fromRow + i, toPiece, true, col, state.enPassantPossible));
                                if (stopAfterFirst && checkAndCastling(board,state,plies,kLine,col,colIndex,plies.size()-1) ) return plies;
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
                        if (toPiece * col <= 0) {
                            plies.add(new Ply(fromLine, fromRow, toLine, toRow, toPiece, false, col, state.enPassantPossible));
                            if (stopAfterFirst && checkAndCastling(board,state,plies,kLine,col,colIndex,plies.size()-1) ) return plies;
                            //System.out.println("XXXXX " + plies.get(plies.size() - 1).getToggleCastlingPossK(0));
                        }
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
                            plies.add(new Ply(fromLine, fromRow, toLine, toRow, toPiece, false, col, state.enPassantPossible));
                            if (stopAfterFirst && checkAndCastling(board,state,plies,kLine,col,colIndex,plies.size()-1) ) return plies;
                            if (toPiece * col != 0) break; //enemy piece, cannot go farther
                        }
                    }

                }
                if (piece == ChessBoard.BISHOP) break; //continue with rook-moves if it is a queen!

            case ChessBoard.ROOK: //TODO: update castling state
                for (int[] rookMove : rookMoves) {
                    for (int d = 1; d <= 8; d++) {
                        toLine = fromLine + d * rookMove[0];
                        toRow = fromRow + d * rookMove[1];
                        if (!(toLine >= 0 && toLine <= 7 && toRow >= 0 && toRow <= 7)) break;
                        toPiece = board.setup[toLine][toRow];
                        if (toPiece * col > 0) break; //own piece, give up that direction
                        plies.add(new Ply(fromLine, fromRow, toLine, toRow, toPiece, false, col, state.enPassantPossible));
                        if (stopAfterFirst && checkAndCastling(board,state,plies,kLine,col,colIndex,plies.size()-1) ) return plies;
                        if (toPiece * col != 0) break; //enemy piece, cannot go farther
                    }

                }
                break;

            case ChessBoard.KING:
                for (int il = -1; il <= 1; il++) {
                    for (int ir = -1; ir <= 1; ir++) {
                        //System.out.println("LAM "+fromLine+" "+fromRow+"    "+il+" "+ir);
                        if (il == 0 && ir == 0) continue; //no move
                        toLine = fromLine + il;
                        toRow = fromRow + ir;
                        if (!(toLine >= 0 && toLine <= 7 && toRow >= 0 && toRow <= 7)) continue;

                        toPiece = board.setup[toLine][toRow];
                        if (toPiece * col > 0) continue; //own piece

                        Ply p = new Ply(fromLine, fromRow, toLine, toRow, toPiece, false, col, state.enPassantPossible);
                        if (state.castlingPossibleK[colIndex]) p.toggleCastlingPossK(colIndex);
                        if (state.castlingPossibleQ[colIndex]) p.toggleCastlingPossQ(colIndex);
                        plies.add(p);
                        if (stopAfterFirst && checkAndCastling(board,state,plies,kLine,col,colIndex,plies.size()-1) ) return plies;
                    }
                }

                //castling
                if (state.check) break;

                if (state.castlingPossibleK[colIndex]) {
                    if (board.setup[kLine][5] == 0 && board.setup[kLine][6] == 0) {
                        if (!underAttack(board, -col, kLine, 5) && !underAttack(board, -col, kLine, 6)) {
                            Ply p = new Ply(fromLine, fromRow, fromLine, fromRow + 2, 0, false, col, state.enPassantPossible);
                            if (state.castlingPossibleK[colIndex]) p.toggleCastlingPossK(colIndex);
                            if (state.castlingPossibleQ[colIndex]) p.toggleCastlingPossQ(colIndex);
                            plies.add(p);
                            if (stopAfterFirst && checkAndCastling(board,state,plies,kLine,col,colIndex,plies.size()-1) ) return plies;
                        }
                    }
                }
                if (state.castlingPossibleQ[colIndex]) {
                    if (board.setup[kLine][1] == 0 && board.setup[kLine][2] == 0 && board.setup[kLine][3] == 0) {
                        if (!underAttack(board, -col, kLine, 1) && !underAttack(board, -col, kLine, 2) && !underAttack(board, -col, kLine, 3)) {
                            Ply p = new Ply(fromLine, fromRow, fromLine, fromRow - 2, 0, false, col, state.enPassantPossible);
                            if (state.castlingPossibleK[colIndex]) p.toggleCastlingPossK(colIndex);
                            if (state.castlingPossibleQ[colIndex]) p.toggleCastlingPossQ(colIndex);
                            plies.add(p);
                            if (stopAfterFirst && checkAndCastling(board,state,plies,kLine,col,colIndex,plies.size()-1) ) return plies;
                        }
                    }
                }
                break;

        }//end switch

        if ( ! stopAfterFirst ) checkAndCastling(board, state, plies, kLine, col, colIndex, 0);

        return plies;

    }

    static boolean checkAndCastling(IBoard board, IState state, ArrayList<Ply> plies, int kLine, int col, int colIndex, int startIndex){

        Iterator itr = plies.listIterator(startIndex);
        Ply ply;
        boolean moveExists=false;
        while (itr.hasNext()) {
            ply = (Ply) itr.next();
            //castling: rook eliminated
            if (ply.getToPiece() == ChessBoard.ROOK && ply.getToLine() == (7 - kLine)) {  //"other" king line!
                if (ply.getToRow() == 0 && state.castlingPossibleQ[1 - colIndex]) ply.toggleCastlingPossQ(1 - colIndex);
                if (ply.getToRow() == 7 && state.castlingPossibleK[1 - colIndex]) ply.toggleCastlingPossK(1 - colIndex);
            }

            //check
            //apply ply to board
            doPly(board, state, ply);
            int[] lrKing = findKing(board, col);

            //check for check
            if (underAttack(board, -col, lrKing[0], lrKing[1])) itr.remove();
            else moveExists=true;

            //take back ply
            undoPly(board, state, ply);
        }


        return moveExists;
    }



    static int[] findKing(IBoard board, int col) {

        int[] lrKing = {-1, -1};
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

    private static void doPly(IBoard board, IState state, Ply ply) {
        //System.out.println("D ply= "+ply);

        byte movingPiece = board.setup[ply.getFromLine()][ply.getFromRow()];
        state.update(movingPiece, ply.getFromLine(), ply.getToLine(), ply.getFromRow());
        if (ply.toggleCastlingPossK[0]) state.castlingPossibleK[0] = !state.castlingPossibleK[0];
        if (ply.toggleCastlingPossK[1]) state.castlingPossibleK[1] = !state.castlingPossibleK[1];
        if (ply.toggleCastlingPossQ[0]) state.castlingPossibleQ[0] = !state.castlingPossibleQ[0];
        if (ply.toggleCastlingPossQ[1]) state.castlingPossibleQ[1] = !state.castlingPossibleQ[1];

        //normal move
        if (ply.getToLine() < 0 || ply.getToLine() > 7 || ply.getToRow() < 0 || ply.getToRow() > 7 ||
                ply.getFromLine() < 0 || ply.getFromLine() > 7 || ply.getFromRow() < 0 || ply.getFromRow() > 7)
            throw new ArrayIndexOutOfBoundsException("ply = " + ply + board + state);
        board.setup[ply.getToLine()][ply.getToRow()] = movingPiece;
        board.setup[ply.getFromLine()][ply.getFromRow()] = 0;

        //castling
        if (Math.abs(movingPiece) == ChessBoard.KING && Math.abs(ply.getFromRow() - ply.getToRow()) >= 2) {
            int rookToRow = -1, rookFromRow = -1;
            if (ply.getToRow() == 2) {
                rookToRow = 3;
                rookFromRow = 0;
            }
            if (ply.getToRow() == 6) {
                rookToRow = 5;
                rookFromRow = 7;
            }
            board.setup[ply.getToLine()][rookToRow] = board.setup[ply.getToLine()][rookFromRow];
            board.setup[ply.getToLine()][rookFromRow] = 0;
        }

        //en-passant
        else if (ply.enPassant) {
            //System.out.println("ENPASSANT "+ply);
            board.setup[ply.getToLine() - ply.getMoverColor()][ply.getToRow()] = 0;
        }

        //promotion
//        else if (ply.getToLine() == 7 * (ply.getMoverColor()+1)/2 && Math.abs(movingPiece) == ChessBoard.PAWN) {
        else if (ply.togglePromotion) {
            board.setup[ply.getToLine()][ply.getToRow()] = (byte) (ChessBoard.QUEEN * ply.getMoverColor());
        }

        //state.check = false; //should be un-done in undoPly... but is that needed at all?

    }

    static void undoPly(IBoard board, IState state, Ply ply) {
        //System.out.println("U ply= "+ply);

        byte movingPiece = board.setup[ply.getToLine()][ply.getToRow()];
        state.undoUpdate(ply.getPrevEnpassantPossible());
        if (ply.toggleCastlingPossK[0]) state.castlingPossibleK[0] = !state.castlingPossibleK[0];
        if (ply.toggleCastlingPossK[1]) state.castlingPossibleK[1] = !state.castlingPossibleK[1];
        if (ply.toggleCastlingPossQ[0]) state.castlingPossibleQ[0] = !state.castlingPossibleQ[0];
        if (ply.toggleCastlingPossQ[1]) state.castlingPossibleQ[1] = !state.castlingPossibleQ[1];

        //normal move
        board.setup[ply.getFromLine()][ply.getFromRow()] = movingPiece;
        board.setup[ply.getToLine()][ply.getToRow()] = (byte) ply.getToPiece();

        //castling
        if (Math.abs(movingPiece) == ChessBoard.KING && Math.abs(ply.getFromRow() - ply.getToRow()) >= 2) {
            int rookToRow = -1, rookFromRow = -1;
            if (ply.getToRow() == 2) {
                rookToRow = 3;
                rookFromRow = 0;
            }
            if (ply.getToRow() == 6) {
                rookToRow = 5;
                rookFromRow = 7;
            }
            board.setup[ply.getToLine()][rookFromRow] = board.setup[ply.getToLine()][rookToRow];
            board.setup[ply.getToLine()][rookToRow] = 0;
        }

        //en-passant
        else if (ply.enPassant) {
            //System.out.println("uENPASSANT");
            board.setup[ply.getToLine() - ply.getMoverColor()][ply.getToRow()] = (byte) (-ChessBoard.PAWN * ply.getMoverColor());
        }

        //promotion
//        else if (ply.getToLine() == 7 * (ply.getMoverColor()+1)/2 && Math.abs(movingPiece) == ChessBoard.PAWN) {
        else if (ply.togglePromotion) {
            board.setup[ply.getFromLine()][ply.getFromRow()] = (byte) (ChessBoard.PAWN * ply.getMoverColor());
        }

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
        if (USE_PLY_METHOD) return bestMoveNew(iBoardState);
        else return bestMoveOld(iBoardState);
    }

    IBoardState bestMoveNew(IBoardState iBoardState) {

        System.out.println("bestMove: nextBestMove = " + ChessBoard.nextBestMove );

        bestMove = null;
        int eval;

        mBoard = new IBoard(iBoardState);
        mState = new IState(iBoardState.state);
        mState.check=false;

        ArrayList<Ply> newPV=new ArrayList<>(); //GETPV

        //long startTime = System.currentTimeMillis();

        /////////////////////////////////////////////////////////////////////////////////////
        eval = pvs2(iBoardState.state.turnOf, startDepth, -INF, +INF, newPV);
        /////////////////////////////////////////////////////////////////////////////////////

        //System.out.println("1st: " + (System.currentTimeMillis() - startTime) + " ms");
        IState tmpState = new IState(iBoardState.state);
        tmpState.check=false;
        //bestMove.setNextMoveNotation(Notation.getMoveNotation(iBoardState,bestMove.state,tmpState,bestPly.getFromLine(),bestPly.getFromRow(),
        //        bestPly.getToLine(),bestPly.getToRow(),bestMove.setup[bestPly.getToLine()][bestPly.getToRow()],bestPly.getToPiece(),new SpecialMove()));

        if (ChessBoard.USE_THREAD && ChessBoard.moveThread.move.stopBestMove) return null;

        if ( bestMove == null ){
            throw new NullPointerException("bestMove: No possible moves. " + eval );
        } else{
            bestMove.setEval(eval*iBoardState.state.turnOf);
            int[] lrKing=findKing(bestMove,bestMove.state.turnOf);
            //check for check
            bestMove.state.check = underAttack(bestMove, -bestMove.state.turnOf, lrKing[0], lrKing[1]);

            if ( listAllMoves(bestMove, bestMove.state).isEmpty() ){
                if (bestMove.state.check) bestMove.state.mate = true;
                else bestMove.state.remis = true;
            }

            IBoard b=new IBoard(bestMove);
            IState s=new IState(bestMove.state);
            IState sPrev=new IState(tmpState);

            nextMove=Notation.getMoveNotationPresBoard(b,newPV.get(0),s,sPrev);
            for (int i=1; i<4; i++) {
                if (newPV.size() > i) ChessBoard.nextBestPlies[i-1] = new Ply(newPV.get(i));
                else ChessBoard.nextBestPlies[i-1] = new Ply(); //this should better be done after executing the move
            }

            newPV.remove(0);
            beyondMoves="";
            for (Ply p: newPV) {
                sPrev = new IState(s);
                doPly(b, s, p);
                s.check = underAttack(b, -s.turnOf, findKing(b, s.turnOf)[0], findKing(b, s.turnOf)[1]);

 //               beyondMoves = beyondMoves + " " + (Notation.getMoveNotation(b, p, s, sPrev));
                beyondMoves = beyondMoves + " " + (Notation.getMoveNotationPresBoard(b, p, s, sPrev));
            }
            beyondMoves=beyondMoves.trim();
            System.out.println("PV |"+nextMove+"|"+beyondMoves+"|"+bestMove.getEval());
            //for (Ply p: newPV) System.out.println("pv "+p);
            bestMove.setNextMoveNotation(beyondMoves);
            bestMove.setNotation(nextMove);

            //bestMove.setNextMoveNotation(Notation.getMoveNotation(iBoardState,bestMove.state,iBoardState.state,bestPly.getFromLine(),bestPly.getFromRow(),
            //        bestPly.getToLine(),bestPly.getToRow(),bestMove.setup[bestPly.getToLine()][bestPly.getToRow()],bestPly.getToPiece(),new SpecialMove()));
            //bestMove.setNotation(/*bestMove.getNotation()+" "+*/bestMove.getNextMovesNotation());

            //System.out.println("BEST MOVE eval="+eval+bestMove);
            return bestMove;
        }
    }


    IBoardState bestMoveOld(IBoardState iBoardState) {

        System.out.println("bestMove: nextBestMove = " + ChessBoard.nextBestMove );

        bestMove = null;
        int eval;

        if (USE_NEGAMAX) eval = negaMax(iBoardState.state.turnOf, startDepth, -INF, +INF, iBoardState);
        else if (USE_PVS) eval = pvs(iBoardState.state.turnOf, startDepth, -INF, +INF, iBoardState);
        else { //for alpha-beta AND for original minimax
            if (iBoardState.state.turnOf == ChessBoard.WHITE) eval = maxMove(startDepth, -INF, +INF, iBoardState);
            else eval = minMove(startDepth, -INF, +INF, iBoardState);
        }

        if (ChessBoard.USE_THREAD && ChessBoard.moveThread.move.stopBestMove) return null;

        if ( bestMove == null ){
            throw new NullPointerException("bestMove: No possible moves. " + eval );
        } else{
            String nextMovesLabel=bestMove.getNextMovesNotation().replaceAll("^\\d+\\. ","");
            nextMovesLabel = nextMovesLabel.replaceAll("^\\S* ","");
            bestMove.setNextMoveNotation(nextMovesLabel);
            bestMove.setEval(eval*iBoardState.state.turnOf);
            System.out.println("PV |"+bestMove.getNotation()+"|"+bestMove.getNextMovesNotation()+"|"+bestMove.getEval());
            return bestMove;
        }
    }

    int pvs2(int color, int depth, int alpha, int beta, ArrayList<Ply> PV){
        if (ChessBoard.USE_THREAD && ChessBoard.moveThread.move.stopBestMove) return -9997;
        if ( depth==0 ){
//            if ( listAllMoves(mBoard, mState).isEmpty() ) return -INF;
            if ( noMoreMoveLeft(mBoard,mState) ){
                //System.out.println(color + "XXXXXXXXXXX\n"+mBoard);
                int[] lrKing=findKing(mBoard,mState.turnOf);
                if ( underAttack(mBoard,color,lrKing[0],lrKing[1]) )  return -99999; //mate
                else return 0; //remis
            }
            return EvaluateBoard.eval(mBoard, mState)*color;
        }

        int maxValue = alpha;
        boolean isFirstMove = true;
        //tmp=(depth==startDepth);
        ArrayList<Ply> moveList = listAllMoves(mBoard, mState);

        if ( depth > getStartDepth() - ChessBoard.MAX_NEXT_PLIES       &&      USE_ORDERING ){
            //System.out.println( "A " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
            sortPlyList(moveList, getStartDepth()-depth);
            //System.out.println( "B " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
        }

        if ( moveList.size() == 0 ) return -99999; //mate // System.out.println("OOOO "+depth);
        for ( Ply ply : moveList ){

            ArrayList<Ply> childPV=new ArrayList<>();
            int value;
            doPly(mBoard, mState, ply);
            if ( isFirstMove ) value = -pvs2(-color, depth-1, -beta, -maxValue, childPV);
            else{
                value = -pvs2(-color, depth-1, -maxValue - 1, -maxValue, childPV);
                if ( maxValue < value && value < beta ){
                    value = -pvs2(-color, depth-1, -beta, -maxValue, childPV);
                }
            }

            /*
            if (depth==startDepth){
                System.out.println(ply+" KK "+value*color+" KK "+childPV.size());
                for (Ply p: childPV) System.out.println("PV= "+p);
            }
            */

            /*if ( depth == startDepth)*/ //System.out.println("DDD "+depth+"=?"+startDepth+"  "+ply+"       "+value+" > "+maxValue);

            isFirstMove = false;
            if ( value > maxValue ){
                maxValue = value;
                if ( USE_ALPHABETA && maxValue >= beta ) {
                    undoPly(mBoard, mState, ply);
                    break;
                }

                PV.clear(); //GETPV
                PV.add(ply); //need deep copy!? //GETPV
                PV.addAll(childPV); //deep? GETPV

                if ( depth == startDepth) {
                    bestMove = new IBoardState(mBoard, mState);
                    //bestPly = new Ply(ply);

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
            //sortBSList(moveList);
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

            //if (depth==startDepth) System.out.println(board.getNextMovesNotation()+" "+board.getNotation()+" "+value);

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
//        int maxValue = -INF;  //to be able to list all best-move sequences

        String thisNotation="";
        ArrayList<IBoardState> moveList = allLegalMoves(currBoardState);
        if ( depth == startDepth && USE_ORDERING ){
            //System.out.println( "A " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
            sortBSList(moveList);
            //System.out.println( "B " + moveList.get(0).getNextMovesNotation() + "    " + ChessBoard.nextBestMove );
        }

        for ( IBoardState board : moveList ){
            int value = -negaMax(-color, depth-1, -beta, -maxValue, board);
            //if (depth==startDepth) System.out.println(board.getNextMovesNotation()+" "+board.getNotation()+" "+value);

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
            sortBSList(moveList);
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
            sortBSList(moveList);
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

    void sortBSList(ArrayList<IBoardState> list){
        int indBestMove = -1;
        for ( int i=0; i<list.size(); i++ ){
            //System.out.println("sortBSList: |" + board.getNotation().replaceAll("^\\d+\\. ","") + "|   |" + ChessBoard.nextBestMove + "| " +
            //        board.getNotation().replaceAll("^\\d+\\. ","").equals(ChessBoard.nextBestMove));
            if ( list.get(i).getNotation().replaceAll("^\\d+\\. ","").equals(ChessBoard.nextBestMove)){
                indBestMove = i;
                break;
            }
        }
        if ( list.size() < 2 || indBestMove<0 ) return;
        Collections.swap(list, 0, indBestMove);
    }

    void sortPlyList(ArrayList<Ply> list, int depth){
        int indBestMove = -1;
        for ( int i=0; i<list.size(); i++ ){
            //System.out.println("sortBSList: |" + board.getNotation().replaceAll("^\\d+\\. ","") + "|   |" + ChessBoard.nextBestMove + "| " +
            //        board.getNotation().replaceAll("^\\d+\\. ","").equals(ChessBoard.nextBestMove));
            if ( ChessBoard.nextBestPlies[depth].equals( list.get(i) ) ){
                indBestMove = i;
                break;
            }
        }
        if ( list.size() < 2 || indBestMove<0 ) return;
        Collections.swap(list, 0, indBestMove);
        //System.out.println("SORT: "+list.get(0)+"\n      "+ChessBoard.nextBestPly);

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

        //if (tmp) for (IBoardState b: list) System.out.println(b);

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
