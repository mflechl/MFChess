package com.mflechl.mfchess;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class Notation extends JLabel {

    //    static Border activeBorder = BorderFactory.createLineBorder(Color.BLUE, 5);
    public static ArrayList<String> notationStrings = new ArrayList<>();

    private static int w = 100;
    private static Font font = new Font("Sans-Serif", Font.PLAIN, 20);

    Notation() {
        super();
        this.setBackground(Color.WHITE);
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); //top left bottom right
        this.setFont(font);
        notationStrings.add(""); //entry for state of new board
    }

    //lines=normal notation minus 1; rows=a->1, b->2, ...; ep=en passant done
    //void addMove(int imove, int aLine, int aRow, int toLine, int toRow, int piece, int eliminatedPiece, boolean enpassant, boolean castling, boolean check, boolean mate, boolean remis) {
    void addMove(IBoard board, int pos, int imove, int aLine, int aRow, int toLine, int toRow, int piece, int eliminatedPiece, boolean enpassant, boolean castling, State state, boolean mate, boolean remis) {
        //	int eliminatedPiece=com.mflechl.mfchess.ChessBoard.board_content[toLine][toRow];
        //for display
        String lbl = "";
        int apiece = Math.abs(piece);

        //write move number
        if (piece > 0) {
            lbl += "<font color='blue'>";
            lbl += Integer.toString(imove);
            lbl += ".";
            lbl += "</font>";
        }

        //castling
        if (castling) {
            lbl += "0-0";
            if (toRow == 2) lbl += "-0"; //queen-side
        } else {
            //letter of moved piece, unless it is a pawn
            if (apiece != ChessBoard.PAWN) {
                lbl += ChessBoard.lpieces[apiece];  //letters
                //	    if (piece>0) lbl+=com.mflechl.mfchess.ChessBoard.upieces[apiece]; //white pieces, unicode
                //	    else         lbl+=com.mflechl.mfchess.ChessBoard.bupieces[apiece]; //black pieces, unicode
            }
            lbl += ambiguity(board, piece, aLine, aRow, toLine, toRow, state); //XX

            //elimination
            if (eliminatedPiece != 0 || enpassant) {
                if (apiece == ChessBoard.PAWN || enpassant)
                    lbl += CoordBoard.alpha[aRow + 1]; //in this case, write from row of pawn
                lbl += "x";
            }
            //Destination field
            lbl += CoordBoard.alpha[toRow + 1];
            lbl += Integer.toString(toLine + 1);
        }
        if (mate || remis || state.check) {
            lbl += "<font color='red'>";
            if (mate) {
                lbl += "# ";
                if (piece > 0) lbl += "1-0";
                else lbl += "0-1";
            } else if (remis) lbl += " &#189; - &#189;";
            else lbl += "+";   //must be check
            lbl += "</font>";
        }


        lbl += " ";
        updateText(lbl, pos);

    }

    private void updateText(String newText, int pos) {
        notationStrings.add(pos, newText);
        setWidth(w);
        //	this.setText("<html><body style='width: 100%'>"+notationStrings+"</html>");
    }

    void setWidth(int width) {
        w = width;
        //	System.out.println("XX "+width);
//        setText("<html><body style='width: " + width + "px'>" + notationStrings + "</html>");
        String str = String.valueOf(notationStrings);
        str = str.replaceAll("[\\[\\],]", "");
        setText("<html><body style='width: " + width + "px'>" + str + "</html>");
    }

    String ambiguity(IBoard board, int piece, int fromLine, int fromRow, int toLine, int toRow, State state) {
        // isLegal(IBoard _iBoard, ChessBoard.SpecialMove sMove, int fromLine, int fromRow, int toLine, int toRow, State _state);
        String amb = "";
        int namb = 0;
        for (int iLine = 0; iLine < 8; iLine++) {
            for (int iRow = 0; iRow < 8; iRow++) {
                if (iLine == fromLine && iRow == fromRow) continue;
                if (board.setup[iLine][iRow] == piece) {
                    if (Move.isLegal(board, Move.sDummy, iLine, iRow, toLine, toRow, state)) {
                        namb++;
                        //on same line?
                        if (iRow != fromRow && Math.abs(piece) != ChessBoard.PAWN) amb = CoordBoard.alpha[fromRow + 1];
                        else if (iLine != fromLine) amb = Integer.toString(fromLine + 1);
                    }
                }
            }
        }
        if (namb > 1) { //three different pieces of the same kind can get there. Return both row and line.
            //this may not actually be needed, though.
            amb = CoordBoard.alpha[fromRow + 1] + (fromLine + 1);
        }
        return amb;
    }


}
