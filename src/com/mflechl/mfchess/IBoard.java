package com.mflechl.mfchess;

public class IBoard {

    IBoard(){ }

    IBoard( IBoard in ){
        setup=DeepCopy.deepCopyInt(in.setup);
    }
    int[][] setup=new int[8][8];

    public String toString() {
        StringBuilder out = new StringBuilder();
        for (int line = 7; line >= 0; line--) {
            for (int row = 0; row < 8; row++) {
                int to_add = setup[line][row];
                if (to_add >= 0) out.append(" ");
                out.append(to_add).append(" ");
            }
            out.append("\n");
        }
        return out.toString();
    }

}
