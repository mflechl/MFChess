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

    private static final Map<String, Byte> pieceMap = new HashMap<>();
    static {
        pieceMap.put("K", (byte) 1);
        pieceMap.put("Q", (byte) 2);
        pieceMap.put("R", (byte) 3);
        pieceMap.put("B", (byte) 4);
        pieceMap.put("N", (byte) 5);
    }

    private static final Map<String, Byte> rowMap = new HashMap<>();

    static {
        rowMap.put("a", (byte) 0);
        rowMap.put("b", (byte) 1);
        rowMap.put("c", (byte) 2);
        rowMap.put("d", (byte) 3);
        rowMap.put("e", (byte) 4);
        rowMap.put("f", (byte) 5);
        rowMap.put("g", (byte) 6);
        rowMap.put("h", (byte) 7);
    }

    static IBoardState noteToBoard(final String move, IBoardState prevBoard) {
        return noteToBoard(move, prevBoard, new StringBuilder());
    }

    static IBoardState noteToBoard(final String move, IBoardState prevBoard, StringBuilder notation) {
        String str = move.replaceAll("^\\s+", "").replaceAll("\\s$", "");
        IBoardState currBoard = new IBoardState(prevBoard);

        byte toLine, toRow;
        byte fromLine = -1, fromRow = -1;
        byte piece;
        byte toPiece = -1;
        byte turnOf = prevBoard.state.turnOf;
        SpecialMove sDummy;
        SpecialMove sMove = new SpecialMove();

        //castling?
        if (str.startsWith("0-0")) { //castling
            fromRow = rowMap.get("e");
            if (turnOf == ChessBoard.WHITE) fromLine = 0;
            else fromLine = 7;
            toLine = fromLine;
            piece = (byte) (ChessBoard.KING * turnOf);

            if (str.startsWith("0-0-0")) { //queen-side castling
                toRow = rowMap.get("c");
            } else {
                toRow = rowMap.get("g");
            }
            //check if it is really legal; also sets sMove.castling = true
            if (!Move.isLegal(currBoard, sMove, fromLine, fromRow, toLine, toRow, currBoard.state))
                return null;
        } else {
            //which piece moved?
            piece = (byte) (turnOf * ChessBoard.PAWN);
            if (str.matches(patternPiece)) {
                piece = (byte) (turnOf * pieceMap.get(str.substring(0, 1)));
                str = str.substring(1);                                       //remove piece from string
            }

            byte remFromRow = -1, remFromLine = -1;
            if (str.matches("[abcdefgh]x?[abcdefgh].*")) {
                remFromRow = rowMap.get(str.substring(0, 1));
                str = str.substring(1);                                       //remove piece from string
            } else if (str.matches("\\dx?[abcdefgh].*")) {
                remFromLine = (byte) (Byte.parseByte(str.substring(0, 1)) - 1);
                str = str.substring(1);                                       //remove piece from string
            } else if (str.matches("[abcdefgh]\\dx?[abcdefgh].*")) {
                remFromRow = rowMap.get(str.substring(0, 1));
                remFromLine = (byte) (Byte.parseByte(str.substring(1, 2)) - 1);
                str = str.substring(2);                                       //remove piece from string
            }
            if (str.substring(0, 1).equals("x")) str = str.substring(1);     //remove "x"; str is now final state

            //get TO coordinates and remove info from string
            toLine = (byte) (Byte.parseByte(str.substring(1, 2)) - 1);
            toRow = rowMap.get(str.substring(0, 1));

            //promotion: to which piece?
            if (str.length() > 2) {
                if (str.substring(2, 3).matches(patternPiece))
                    toPiece = (byte) (turnOf * pieceMap.get(str.substring(2, 3)));
            }

            //get FROM coordinates
            for (byte iLine = 0; iLine < 8; iLine++) {
                for (byte iRow = 0; iRow < 8; iRow++) {
                    if (piece != currBoard.setup[iLine][iRow]) continue;
                    if (remFromRow >= 0 && iRow != remFromRow) continue;
                    if (remFromLine >= 0 && iLine != remFromLine) continue;
                    sDummy = new SpecialMove();
                    if (Move.isLegal(currBoard, sDummy, iLine, iRow, toLine, toRow, currBoard.state)) {
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
            ChessBoard.processMove(currBoard, fromLine, fromRow, toLine, toRow, currBoard.setup[fromLine][fromRow], true, sMove);
            if (Math.abs(toPiece) > 1) currBoard.setup[toLine][toRow] = toPiece; //promotion
            currBoard.state.update(piece, fromLine, toLine, fromRow); //also: check and castling update!
            ChessBoard.updateCastlingState(currBoard.state, piece, fromLine, fromRow, toLine, toRow, sMove.castling);
            Move.updateCheckState(currBoard.state, currBoard);

            String moveNumberStr = "";
            if (turnOf == ChessBoard.WHITE)
                moveNumberStr = currBoard.state.moveNumber + ". ";
            currBoard.setNotation(moveNumberStr + move);
            notation.append(moveNumberStr).append(move);
        }

        //System.out.println(piece + "|" + str + "|" + fromRow + "-" + fromLine + "|" + toRow + "-" + toLine);
        return currBoard;
    }

    static ArrayList<IBoardState> translate(String notation) {

        ArrayList<IBoardState> list = new ArrayList<>();
        IBoardState currentBoard = new IBoardState();
        list.add(currentBoard); //empty board

        //int turnOf = ChessBoard.WHITE;

        String[] split = notation.replaceAll("^\\s+", "").replaceAll("\\s$", "").split(" +");
        for (String str : split) {
            if (str.matches(patternMoveNumber)) continue; //remove move number

            StringBuilder notationMove = new StringBuilder();
            currentBoard = noteToBoard(str, currentBoard, notationMove);
            if (currentBoard != null) {
                list.add(currentBoard);
                Chess.notation.updateText(notationMove.toString(), currentBoard.state.nMoves);
            } else System.out.println("Warning: translate - currentBoard is null.");
            //turnOf *= -1;
        }
        return list;
    }

}
