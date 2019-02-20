package com.mflechl.mfchess;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class Notation extends JLabel {

    //    static Border activeBorder = BorderFactory.createLineBorder(Color.BLUE, 5);
    private ArrayList<String> notationString = new ArrayList<>();

    private static int w = 100;
    private static Font font = new Font("Sans-Serif", Font.PLAIN, 20);

    Notation() {
        super();
        this.setBackground(Color.WHITE);
        this.setOpaque(true);
        this.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); //top left bottom right
        this.setFont(font);
    }

    //lines=normal notation minus 1; rows=a->1, b->2, ...; ep=en passant done
    void addMove(int imove, int aLine, int aRow, int toLine, int toRow, int piece, int eliminatedPiece, boolean enpassant, boolean castling, boolean check, boolean mate, boolean remis) {
        //	int eliminatedPiece=com.mflechl.mfchess.ChessBoard.board_content[toLine][toRow];
        //for display
        String lbl = "";
        int apiece = Math.abs(piece);
        if (piece > 0) {
            lbl += "<font color='blue'>";
            lbl += Integer.toString(imove);
            lbl += ".";
            lbl += "</font>";
        }

        if (castling) {
            lbl += "0-0";
            if (toRow == 2) lbl += "-0"; //queen-side
        } else {
            if (apiece != ChessBoard.PAWN) {
                lbl += ChessBoard.lpieces[apiece];  //letters
                //	    if (piece>0) lbl+=com.mflechl.mfchess.ChessBoard.upieces[apiece]; //white pieces, unicode
                //	    else         lbl+=com.mflechl.mfchess.ChessBoard.bupieces[apiece]; //black pieces, unicode
            }
            if (eliminatedPiece != 0 || enpassant) {
                if (apiece == ChessBoard.PAWN || enpassant) lbl += CoordBoard.alpha[aRow + 1];
                lbl += "x";
            }
            lbl += CoordBoard.alpha[toRow + 1];
            lbl += Integer.toString(toLine + 1);
        }
        if (mate || remis || check) {
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
        updateText(lbl);

    }

    private void updateText(String newText) {
        notationString.add(newText);
        setWidth(w);
        //	this.setText("<html><body style='width: 100%'>"+notationString+"</html>");
    }

    void setWidth(int width) {
        w = width;
        //	System.out.println("XX "+width);
//        setText("<html><body style='width: " + width + "px'>" + notationString + "</html>");
        String str = String.valueOf(notationString);
        str = str.replaceAll("[\\[\\]\\,]", "");
        setText("<html><body style='width: " + width + "px'>" + str + "</html>");
    }
}
