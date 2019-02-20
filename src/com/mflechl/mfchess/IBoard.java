package com.mflechl.mfchess;

public class IBoard {

    IBoard(){ }

    IBoard( IBoard in ){
        setup = Copy.deepCopyInt(in.setup);
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
