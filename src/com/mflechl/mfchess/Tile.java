package com.mflechl.mfchess;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.Font;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
//import javax.swing.Icon;

public class Tile extends JLabel {

    //  static Font font = new Font("Arial Unicode MS", Font.BOLD, 50);
    static Font font = new Font("Sans-Serif", Font.PLAIN, 50);

    static Border activeBorder = BorderFactory.createLineBorder(Color.BLUE, 5);
    static Border destinationBorder = BorderFactory.createLineBorder(Color.RED, 3);

    public int piece = 0;
    public int line, row;
    public static int statLine, statRow;

    //  public Map<Integer,String> pieceDict = createDict();

    public Tile(String text, int line, int row) {
        super(text);
        init(line, row);
    }

    /*    public Tile(Icon image, int line, int row) {
		super(image);
		init(line,row);
    } */

    public void init(int line, int row) {
        this.line = line;
        this.row = row;
        statLine = line;
        statRow = row;
        setFont(font);
        setHorizontalAlignment(JLabel.CENTER);
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                fireActionPerformed(new ActionEvent(Tile.this, ActionEvent.ACTION_PERFORMED,
                        "MyMessage"));
            }
        });
    }

    public void setActiveBorder() {
        setBorder(activeBorder);
    }

    public void setDestinationBorder() {
        setBorder(destinationBorder);
    }

    public void setBorderInactive() {
        setBorder(null);
    }

    public int getPiece() {
        return piece;
    }

    public void setPiece(int piece) {
        this.piece = piece;
        if (Integer.signum(piece) < 0) setIcon(ChessBoard.bpieces[-piece]);
        else setIcon(ChessBoard.wpieces[+piece]);
        //	this.setText(pieceDict.get(piece));
        //	System.out.println(line+" "+row+" "+piece);
    }

    public void setFontSize(float fs) {
        font = font.deriveFont(fs);
        setFont(font);
    }

    /* public float getFontSize(){
		return font.getSize();
    } */

    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    /* public void removeActionListener(ActionListener l) {
		listenerList.remove(ActionListener.class, l);
    } */

    protected void fireActionPerformed(ActionEvent ae) {

        Object[] listeners = listenerList.getListeners(ActionListener.class);

        //save it in the static variable so it can be read out externally
        statRow = this.row;
        statLine = this.line;

        for (Object listener : listeners) {
            ((ActionListener) listener).actionPerformed(ae);
        }
    }
}
