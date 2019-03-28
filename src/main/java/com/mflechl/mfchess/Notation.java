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
        notationStrings.add(""); //entry for currentStaticState of new board
    }

    static String getMoveNotation(IBoard board, IState state, IState prevState, int fromLine, int fromRow, int toLine, int toRow, int movingPiece, int eliminatedPiece, SpecialMove sMove) {
        return getMoveNotation(board, state.moveNumber, state.check, fromLine, fromRow, toLine, toRow, movingPiece, eliminatedPiece, sMove.enPassant, sMove.castling, prevState, state.mate, state.remis);
    }

    static String getMoveNotation(IBoard board, int imove, boolean check, int aLine, int aRow, int toLine, int toRow, int piece, int eliminatedPiece, boolean enpassant, boolean castling, IState state, boolean mate, boolean remis) {
        String lbl = "";
        int apiece = Math.abs(piece);

        //write move number
        if (piece > 0) lbl += imove + ". ";

        if ( apiece == ChessBoard.KING && Math.abs(toRow-aRow)==2 ) castling=true; //do not need argument above anymore!

        //castling
        if (castling) {
            lbl += "0-0";
            if (toRow == 2) lbl += "-0"; //queen-side
        } else {
            //letter of moved piece, unless it is a pawn
            if (apiece != ChessBoard.PAWN) {
                lbl += ChessBoard.lpieces[apiece];  //letters
                //    if (piece>0) lbl+=com.mflechl.mfchess.ChessPieceImage.upieces[apiece]; //white pieces, unicode
                //    else         lbl+=com.mflechl.mfchess.ChessPieceImage.bupieces[apiece]; //black pieces, unicode
            }
            lbl += ambiguity(board, piece, aLine, aRow, toLine, toRow, state);

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

        // promotion
        if ((piece == ChessBoard.BLACK * ChessBoard.PAWN && toLine == 0) || (piece == ChessBoard.WHITE * ChessBoard.PAWN && toLine == 7)) {
            lbl += "Q";
        }

        // mate / remis /check
        if (mate || remis || check) {
            if (mate) {
                lbl += "# ";
                if (piece > 0) lbl += "1-0";
                else lbl += "0-1";
            } else if (remis) lbl += " &#189; - &#189;";
            else lbl += "+";   //must be check
        }


        //lbl += " ";
        return lbl;
    }

    /*
    void addMove(IBoard board, IState state, IState prevState, int fromLine, int fromRow, int toLine, int toRow, int movingPiece, int eliminatedPiece, SpecialMove sMove) {
        addMove(board, state.nMoves, state.moveNumber, state.check, fromLine, fromRow, toLine, toRow, movingPiece, eliminatedPiece, sMove.enPassant, sMove.castling, prevState, state.mate, state.remis);
    }

    //lines=normal notation minus 1; rows=a->1, b->2, ...; ep=en passant done
    void addMove(IBoard board, int pos, int imove, boolean check, int fromLine, int fromRow, int toLine, int toRow, int piece, int eliminatedPiece, boolean enpassant, boolean castling, IState state, boolean mate, boolean remis) {
        String lbl = getMoveNotation(board, imove, check, fromLine, fromRow, toLine, toRow, piece, eliminatedPiece, enpassant, castling, state, mate, remis);
        if (pos >= 0) updateText(lbl, pos);
    }
    */

    void updateText(String newText, int pos) {
        notationStrings.add(pos, newText);
        System.out.println(String.valueOf(notationStrings).replaceAll("<[^>]*>", "").replaceAll(",", "").replaceAll("[\\[\\]] *", "").replaceAll(" +", " "));
        display();
        //	this.setText("<html><body style='width: 100%'>"+notationStrings+"</html>");
    }

    static String getNotationString() {
        return String.valueOf(notationStrings).replaceAll("<[^>]*>", "").replaceAll(",", "").replaceAll("[\\[\\]] *", "").replaceAll(" +", " ");
    }

    void display() {
        setHTML(w);
    }

    void setHTML(int width) {
        w = width;
        //	System.out.println("XX "+width);
//        setText("<html><body style='width: " + width + "px'>" + notationStrings + "</html>");
        String str = String.valueOf(notationStrings);
        str = str.replaceAll("[\\[\\],]", "");
        str = str.replaceAll("(\\d*\\.)", "<font color='blue'>$1</font>");
        str = str.replaceAll("(&#189; - &#189;)", "<font color='red'>$1</font>");
        str = str.replaceAll("([+#])$", "<font color='red'>$1</font>");
        str = str.replaceAll("([+#])([^1])", "<font color='red'>$1</font>$2");
        str = str.replaceAll("(1-0)", "<font color='red'>$1</font>");
        setText("<html><body style='width: " + width + "px'>" + str + "</html>");
    }

    //check if the notation is ambiguous, i.e. if row and/or line need also be given
    //board is the board *before* the last move has happened
    static String ambiguity(IBoard board, int piece, int fromLine, int fromRow, int toLine, int toRow, IState state) {
        String amb = "";
        int namb = 0;
        for (int iLine = 0; iLine < 8; iLine++) {
            for (int iRow = 0; iRow < 8; iRow++) {
                if (iLine == fromLine && iRow == fromRow) continue; //this is the move that was actually made
                if (board.setup[iLine][iRow] == piece) {
                    //System.out.println("ambiguity: state="+state+"sDummy: "+Move.sDummy+"\n"+board);
                    if (Move.isLegal(board, new SpecialMove(), iLine, iRow, toLine, toRow, state)) {
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
