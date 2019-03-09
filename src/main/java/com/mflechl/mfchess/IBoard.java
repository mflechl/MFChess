package com.mflechl.mfchess;

import java.util.ArrayList;

public class IBoard {

    IBoard() {
        init(setup);
    }

    IBoard( IBoard in ){
        setup = Copy.deepCopyInt(in.setup);
    }

    byte[][] setup = new byte[8][8]; // line / row

    static final String U_ = "\033[4m";
    static final String _U = "\033[0m";
    //static final String B_ = "\033[1m";
    //static final String _B = "\033[0m";

    //set up initial chess board, i.e. state before any move
    static void init(byte[][] setup) {
        for (int j = 0; j < setup[1].length; j++) {
            setup[1][j] = ChessBoard.WHITE * ChessBoard.PAWN;
            setup[6][j] = ChessBoard.BLACK * ChessBoard.PAWN;
            setup[6][j] = ChessBoard.BLACK * ChessBoard.PAWN;
        }
        setup[0][0] = ChessBoard.WHITE * ChessBoard.ROOK;
        setup[0][7] = ChessBoard.WHITE * ChessBoard.ROOK;
        setup[7][0] = ChessBoard.BLACK * ChessBoard.ROOK;
        setup[7][7] = ChessBoard.BLACK * ChessBoard.ROOK;

        setup[0][1] = ChessBoard.WHITE * ChessBoard.KNIGHT;
        setup[0][6] = ChessBoard.WHITE * ChessBoard.KNIGHT;
        setup[7][1] = ChessBoard.BLACK * ChessBoard.KNIGHT;
        setup[7][6] = ChessBoard.BLACK * ChessBoard.KNIGHT;

        setup[0][2] = ChessBoard.WHITE * ChessBoard.BISHOP;
        setup[0][5] = ChessBoard.WHITE * ChessBoard.BISHOP;
        setup[7][2] = ChessBoard.BLACK * ChessBoard.BISHOP;
        setup[7][5] = ChessBoard.BLACK * ChessBoard.BISHOP;

        setup[0][3] = ChessBoard.WHITE * ChessBoard.QUEEN;
        setup[0][4] = ChessBoard.WHITE * ChessBoard.KING;
        setup[7][3] = ChessBoard.BLACK * ChessBoard.QUEEN;
        setup[7][4] = ChessBoard.BLACK * ChessBoard.KING;
    }

    static ArrayList<int[]> diff(IBoard b1, IBoard b2) {
        ArrayList<int[]> list = new ArrayList<>();
        for (int iline = 0; iline < 8; iline++) {
            for (int irow = 0; irow < 8; irow++) {
                if (b1.setup[iline][irow] != b2.setup[iline][irow]) {
                    int[] coord = {iline, irow};
                    list.add(coord);
                }
            }
        }
        return list;
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("\n_________________\n");
        for (int line = 7; line >= 0; line--) {
            out.append(U_ + "|");
            for (int row = 0; row < 8; row++) {
                int to_add = setup[line][row];
                int apiece = Math.abs(to_add);
//                if (to_add >= 0) out.append(" ");
                if (to_add != 0) {
//                    out.append(to_add);
                    if (to_add > 0) out.append(ChessPieceImage.upieces[apiece]);
                    if (to_add < 0) out.append(ChessPieceImage.bupieces[apiece]);
                } else out.append(" ");
                out.append("|");
            }
            out.append(_U);
            out.append("\n");
        }
        return out.toString();
    }

}

/*
//poor man's profiling
        long startTime = System.currentTimeMillis();
        for (int i=0; i<1000000; i++) {
        setup = Copy.deepCopyInt(in.setup);
//            setup = Copy.deepArrayCopy(in.setup);
        }
        long finishTime = System.currentTimeMillis();
        System.out.println("That took: " + (finishTime - startTime) + " ms");
*/
