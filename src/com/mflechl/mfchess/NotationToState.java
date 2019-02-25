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

    static ArrayList<IBoardState> translate(String notation) {

        ArrayList<IBoardState> list = new ArrayList<>();

        int turnOf = ChessBoard.WHITE;

        String[] split = notation.split(" ");
        for (String str : split) {
            int piece = turnOf * ChessBoard.PAWN;
            if (str.matches(patternMoveNumber)) continue;                 //remove move number
            System.out.print(str + " ");
            if (str.matches(patternPiece)) {                               //which piece moved?
                piece = turnOf * pieceMap.get(str.substring(0, 1));
                str = str.substring(1);                                       //remove piece from string
            }
            if (str.substring(0, 1).equals("x")) str = str.substring(1);     //remove "x", not needed

            System.out.println(piece + "|" + str + "|");

            turnOf *= -1;
        }
        return list;
    }

}
