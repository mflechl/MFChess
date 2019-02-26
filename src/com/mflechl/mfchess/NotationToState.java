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

        String[] split = notation.split(" ");
        for (String orig_str : split) {
            String str = orig_str;
            if (str.matches(patternMoveNumber)) continue; //remove move number

            System.out.print(str + " ");
            currentBoard = new IBoardState(currentBoard);                 //since passed by reference to the list

            //which piece moved?
            int piece = turnOf * ChessBoard.PAWN;
            if (str.matches(patternPiece)) {
                piece = turnOf * pieceMap.get(str.substring(0, 1));
                str = str.substring(1);                                       //remove piece from string
            }

            if (str.substring(0, 1).equals("x")) str = str.substring(1);     //remove "x"; str is now final state

            //get to coordinates
            int toLine = Integer.parseInt(str.substring(1, 2)) - 1;
            int toRow = rowMap.get(str.substring(0, 1));

            //get from coordinates
            SpecialMove sMove = new SpecialMove();
            int fromLine = -1, fromRow = -1;
            for (int iLine = 0; iLine < 8; iLine++) {
                for (int iRow = 0; iRow < 8; iRow++) {
                    if (piece != currentBoard.setup[iLine][iRow]) continue;
                    sMove = new SpecialMove();
                    if (Move.isLegal(currentBoard, sMove, iLine, iRow, toLine, toRow)) {
                        fromLine = iLine;
                        fromRow = iRow;
                        //TODO: more than one possibility!
                    }
                }
            }
            if (fromLine >= 0 && fromRow >= 0) {
                ChessBoard.processMove(currentBoard, fromLine, fromRow, toLine, toRow, currentBoard.setup[fromLine][fromRow], true, sMove);
                currentBoard.state.update(piece, fromLine, toLine, fromRow); //also: check and castling update!
                list.add(currentBoard);
                String moveNumberStr = "";
                if (turnOf == ChessBoard.WHITE)
                    moveNumberStr = Notation.decorateMoveNumber(currentBoard.state.moveNumber);
                Chess.notation.updateText(moveNumberStr + orig_str, currentBoard.state.nMoves);
            }

            System.out.println(piece + "|" + str + "|" + fromRow + "-" + fromLine + "|" + toRow + "-" + toLine);

            turnOf *= -1;
        }
        return list;
    }

}
