package com.mflechl.mfchess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class NotationToState {
    NotationToState() {
        //no instance
    }

    private static final String patternMoveNumber = "[0-9]*\\.";
    private static final String patternPiece = "^[KQRBN].*";

    private static final Map<String, Integer> pieceMap = new HashMap<>();
    static {
        pieceMap.put("K", 1);
        pieceMap.put("Q", 2);
        pieceMap.put("R", 3);
        pieceMap.put("B", 4);
        pieceMap.put("N", 5);
    }

    private static final Map<String, Integer> rowMap = new HashMap<>();

    static {
        rowMap.put("a", 0);
        rowMap.put("b", 1);
        rowMap.put("c", 2);
        rowMap.put("d", 3);
        rowMap.put("e", 4);
        rowMap.put("f", 5);
        rowMap.put("g", 6);
        rowMap.put("h", 7);
    }

    static ArrayList<IBoardState> translate(String notation) {

        ArrayList<IBoardState> list = new ArrayList<>();
        IBoardState currentBoard = new IBoardState();
        list.add(currentBoard); //empty board

        int turnOf = ChessBoard.WHITE;

        String[] split = notation.split(" +");
        for (String orig_str : split) {
            String str = orig_str;
            if (str.matches(patternMoveNumber)) continue; //remove move number

            System.out.print(str + " ");
            currentBoard = new IBoardState(currentBoard);                 //since passed by reference to the list

            int toLine, toRow;
            int fromLine = -1, fromRow = -1;
            int piece;
            int toPiece = -1;
            SpecialMove sDummy;
            SpecialMove sMove = new SpecialMove();

            //castling?
            if (str.startsWith("0-0")) { //castling
                fromRow = rowMap.get("e");
                if (turnOf == ChessBoard.WHITE) fromLine = 0;
                else fromLine = 7;
                toLine = fromLine;
                piece = ChessBoard.KING * turnOf;

                if (str.startsWith("0-0-0")) { //queen-side castling
                    toRow = rowMap.get("c");
                } else {
                    toRow = rowMap.get("g");
                }
                //check if it is really legal; also sets sMove.castling = true
                if (!Move.isLegal(currentBoard, sMove, fromLine, fromRow, toLine, toRow, currentBoard.state))
                    return list;
            } else {
                //which piece moved?
                piece = turnOf * ChessBoard.PAWN;
                if (str.matches(patternPiece)) {
                    piece = turnOf * pieceMap.get(str.substring(0, 1));
                    str = str.substring(1);                                       //remove piece from string
                }

                //System.out.println(rowMap.keySet().toString().replaceAll("\\W",""));
                int remFromRow = -1, remFromLine = -1;
//                if (str.substring(0, 2).matches("[abcdefgh]x?[abcdefgh]")) {
                if (str.matches("[abcdefgh]x?[abcdefgh].*")) {
                    remFromRow = rowMap.get(str.substring(0, 1));
                    str = str.substring(1);                                       //remove piece from string
//                } else if(str.substring(0, 2).matches("\\dx?[abcdefgh]")) {
                } else if (str.matches("\\dx?[abcdefgh].*")) {
                    //System.out.println( str.substring(0,1) );
                    remFromLine = Integer.parseInt(str.substring(0, 1)) - 1;
                    str = str.substring(1);                                       //remove piece from string
                } else if (str.matches("[abcdefgh]\\dx?[abcdefgh].*")) {
                    remFromRow = rowMap.get(str.substring(0, 1));
                    remFromLine = Integer.parseInt(str.substring(1, 2)) - 1;
                    str = str.substring(2);                                       //remove piece from string
                }
                System.out.print(str + " ");
                if (str.substring(0, 1).equals("x")) str = str.substring(1);     //remove "x"; str is now final state

                //get TO coordinates and remove info from string
                toLine = Integer.parseInt(str.substring(1, 2)) - 1;
                toRow = rowMap.get(str.substring(0, 1));

                //promotion: to which piece?
                if (str.length() > 2) {
                    if (str.substring(2, 3).matches(patternPiece)) toPiece = turnOf * pieceMap.get(str.substring(2, 3));
                }

                //get FROM coordinates
                for (int iLine = 0; iLine < 8; iLine++) {
                    for (int iRow = 0; iRow < 8; iRow++) {
                        if (piece != currentBoard.setup[iLine][iRow]) continue;
                        if (remFromRow >= 0 && iRow != remFromRow) continue;
                        if (remFromLine >= 0 && iLine != remFromLine) continue;
                        sDummy = new SpecialMove();
                        if (Move.isLegal(currentBoard, sDummy, iLine, iRow, toLine, toRow, currentBoard.state)) {
                            //Tmore than one possibility - should not happen:
                            assert fromLine == -1 : fromRow + " " + fromLine + "    " + iRow + " " + iLine; //Run > Edit Configurations... > Configuration > VM options: -ea
                            fromLine = iLine;
                            fromRow = iRow;
                            sMove = new SpecialMove(sDummy); //so that it does not get overwritten
                        }
                    }
                }
            }

            //move!
            if (fromLine >= 0 && fromRow >= 0) {
                ChessBoard.processMove(currentBoard, fromLine, fromRow, toLine, toRow, currentBoard.setup[fromLine][fromRow], true, sMove);
                if (Math.abs(toPiece) > 1) currentBoard.setup[toLine][toRow] = toPiece; //promotion
                currentBoard.state.update(piece, fromLine, toLine, fromRow); //also: check and castling update!
                ChessBoard.updateCastlingState(currentBoard.state, piece, fromLine, fromRow, toLine, toRow, sMove.castling);
                Move.updateCheckState(currentBoard.state, currentBoard);
                list.add(currentBoard);
                String moveNumberStr = "";
                if (turnOf == ChessBoard.WHITE)
                    moveNumberStr = currentBoard.state.moveNumber + ". ";
                Chess.notation.updateText(moveNumberStr + orig_str, currentBoard.state.nMoves);
            }

            System.out.println(piece + "|" + str + "|" + fromRow + "-" + fromLine + "|" + toRow + "-" + toLine);

            turnOf *= -1;
        }
        return list;
    }

}
