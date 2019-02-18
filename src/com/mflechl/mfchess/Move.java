package com.mflechl.mfchess;

import java.util.ArrayList;

public final class Move {
    Move() {
        //no instance
    }

    public static ChessBoard.SpecialMove sDummy=new ChessBoard.SpecialMove();

    //move from fromLine, fromRow to toLine,toRow legal?
    static boolean isLegal(IBoard _iBoard, ChessBoard.SpecialMove sMove, int fromLine, int fromRow, int toLine, int toRow) {
        int col = Integer.signum(_iBoard.setup[fromLine][fromRow]);

        //check if move per se is fine (way to move for a given piece, no obstacles in between)
        boolean isLegal = false;

        switch (Math.abs(_iBoard.setup[fromLine][fromRow])) {
            case ChessBoard.PAWN:
                isLegal = legalMovePawn(_iBoard, fromLine, fromRow, toLine, toRow, col, sMove);
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
                isLegal = legalMoveKing(_iBoard, fromLine, fromRow, toLine, toRow, col, sMove);
                break;
        }

        if (!isLegal) return false;

        //check if move does not result in check
        if (Math.abs(_iBoard.setup[toLine][toRow]) == ChessBoard.KING) return true; //if it is a test for check, do not need this!

        //make move on hypothetical tiles and test if this would mean "check"
        ChessBoard.hypo_iBoard = new IBoard( _iBoard ); //deep copy
        ChessBoard.processMove(fromLine, fromRow, toLine, toRow, _iBoard.setup[fromLine][fromRow], true, sMove);
        //System.out.println(_iBoard.setup[1][2]+" "+ChessBoard.hypo_iBoard.setup[1][2]);
        boolean isCheck = isChecked(ChessBoard.hypo_iBoard, col);
	/*
        if (isCheck){ //take back the setting of this
            state.enPassantDone=false;
            state.castlingDone=false;
        }
	*/

        return (!isCheck);
    }


    private static boolean legalMovePawn(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow, int col, ChessBoard.SpecialMove sMove) {
        if (fromRow == toRow) {
            //normal move
            if ((toLine - fromLine == col) && _iBoard.setup[toLine][toRow] == 0) return true;
            //two steps from starting line
            if (((col == 1 && fromLine == 1) || (col == -1 && fromLine == 6)) &&   //in starting line for white or black
                    (toLine - fromLine == 2 * col)) {                                      //move two steps in the right direction
                //no piece in between
                return _iBoard.setup[fromLine + col][fromRow] == 0;
            }
        }
        //capture piece
        else if (Math.abs(fromRow - toRow) == 1 && (toLine - fromLine) == col) {    //eliminate other piece?
            if (_iBoard.setup[toLine][toRow] * col < 0) return true; //to-field is not empty and has opposite sign
            //XX	    if ( state.enPassantPossible == toRow ){ state.enPassantDone=true; return true;} //en passant?
            if (ChessBoard.state.enPassantPossible == toRow) {
                sMove.enPassant = true;
                return true;
            } //en passant?
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

    private static boolean legalMoveKing(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow, int col, ChessBoard.SpecialMove sMove) {
        //normal move
        if (Math.abs(fromRow - toRow) <= 1 && Math.abs(fromLine - toLine) <= 1) return true;

        //castling
        System.out.println(ChessBoard.state.castlingPossibleQ[1] + " " + ChessBoard.state.castlingPossibleK[1] + " W  B " + ChessBoard.state.castlingPossibleQ[0] + " " + ChessBoard.state.castlingPossibleK[0]);
        //	if ( (col==WHITE && !state.castlingPossibleQ[1] && !state.castlingPossibleK[1] ) ||
        //	     (col==BLACK && !state.castlingPossibleQ[0] && !state.castlingPossibleK[0] ) ) return false;

        if (fromLine != toLine) return false; //king must be on the starting position to fulfill later criteria
        if (ChessBoard.state.check) return false;

        int colIndex = (col + 1) / 2; //black=0, white=1
        if ((toRow == 2 && emptyBetween(_iBoard, fromLine, fromRow, toLine, 1, true) && ChessBoard.state.castlingPossibleQ[colIndex]) ||
                (toRow == 6 && emptyBetween(_iBoard, fromLine, fromRow, toLine, 6, true) && ChessBoard.state.castlingPossibleK[colIndex])) {
            sMove.castling = true;
            return true;
        }
        return false;
    }


    private static boolean emptyBetween(IBoard _iBoard, int fromLine, int fromRow, int toLine, int toRow, boolean checkCheck) {

        if (toRow == fromRow) { //rook-type move
            int direction = Integer.signum(toLine - fromLine);  //+1 if toLine is larger, -1 if fromLine
            for (int i = fromLine + direction; (i * direction) < (toLine * direction); i += direction) {
                if (_iBoard.setup[i][fromRow] != 0) return false;
            }
        } else if (toLine == fromLine) { //rook-type move
            int direction = Integer.signum(toRow - fromRow);  //+1 if toRow is larger, -1 if fromRow
            for (int j = fromRow + direction; (j * direction) < (toRow * direction); j += direction) {
                if (_iBoard.setup[fromLine][j] != 0) return false;
                if (checkCheck) { //only needed for castling
                    ChessBoard.hypo_iBoard = _iBoard;
                    ChessBoard.processMove(fromLine, fromRow, toLine, j, _iBoard.setup[fromLine][fromRow], true, sDummy);
                    if (isChecked(ChessBoard.hypo_iBoard, ChessBoard.state.turnOf)) return false;
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
        //	System.out.print(lineKing+" L "+rowKing+"  ");

        //check opponent pieces: can eliminate king=is in check
        for (int il = 0; il < 8; il++) {
            for (int ir = 0; ir < 8; ir++) {
                if (_iBoard.setup[il][ir] * col < 0) {
                    //		    if ( isLegal(_iBoard, il, ir, lineKing, rowKing) ) return true;
                    if (isLegal(_iBoard, sDummy, il, ir, lineKing, rowKing)) return true;
                }
            }
        }
        return false;
    }

    static ArrayList<IBoard> allLegalMoves(IBoard _iBoard){
        ArrayList<IBoard> list=new ArrayList<>();
        list.add(_iBoard);
        return list;
    }


}
