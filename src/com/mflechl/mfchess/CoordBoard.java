package com.mflechl.mfchess;

import java.awt.Font;
//X import java.awt.Color;
import javax.swing.JLabel;

class CoordBoard {

    Coord[][] coordTiles = new Coord[10][10];

    //X    private Color color; // Color of squares
    final static String[] alpha = {"", "a", "b", "c", "d", "e", "f", "g", "h", ""};

    CoordBoard(/*Color color*/) {
        //X this.color = color;
        this.initBoard();
    }

    private void initBoard() {
        for (int i = coordTiles.length - 1; i >= 0; i--) {
            for (int j = 0; j < coordTiles[i].length; j++) {
                String lbl = "";
                if ((i + j) % 9 == 0) lbl = " ";
                else if (i == 0 || i == 9) lbl = alpha[j];
                else if (j == 0 || j == 9) lbl = Integer.toString(i);
                if (!lbl.equals("")) coordTiles[i][j] = new Coord(lbl, i, j);
            }
        }
    }

    void setMaxFontSize() {
        float mindim = Math.min(coordTiles[0][1].getSize().height, coordTiles[0][1].getSize().width);
        setFontSize(mindim * (float) 0.4);
    }

    private void setFontSize(float fs) {
        for (int i = 0; i < coordTiles.length; i++) {
            for (int j = 0; j < coordTiles[i].length; j++) {
                if (i == 0 || j == 0 || i == 9 || j == 9) {
                    coordTiles[i][j].setFontSize(fs);
                }
            }
        }
    }

    /* public float getFontSize() {
	    return tiles[0][0].getFontSize();
    }*/


    static class Coord extends JLabel {

        private static Font unicode = new Font("Arial Unicode MS", Font.PLAIN, 50);

        Coord(String msg, int line, int row) {

            super(msg);

            this.setFont(unicode);
            this.setHorizontalAlignment(JLabel.CENTER);
            if (row == 0) {
                this.setHorizontalAlignment(JLabel.RIGHT);
                this.setText(this.getText() + " ");
            }
            if (row == 9) {
                this.setHorizontalAlignment(JLabel.LEFT);
                this.setText(" " + this.getText());
            }
            if (line == 0) this.setVerticalAlignment(JLabel.TOP);
            if (line == 9) this.setVerticalAlignment(JLabel.BOTTOM);
            this.setOpaque(true);
        }

        void setFontSize(float fs) {
            unicode = unicode.deriveFont(fs);
            setFont(unicode);
        }

        /*    float getFontSize(){
            return unicode.getSize();
        } */

    }
}


