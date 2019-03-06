package com.mflechl.mfchess;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ChessPieceImage {

    static final String[] upieces = {"", "\u2654", "\u2655", "\u2656", "\u2657", "\u2658", "\u2659"};
    static final String[] bupieces = {"", "\u265A", "\u265B", "\u265C", "\u265D", "\u265E", "\u265F"};

    private static final Color[] pieceColors = {
            new Color(213, 213, 187), new Color(80, 80, 80)
            //        new Color(203, 203, 197), new Color(192, 142, 60)
    };

    private static final Color outlineColor = Color.DARK_GRAY;

    //adapted from https://stackoverflow.com/questions/18686199/fill-unicode-characters-in-labels
    private static ArrayList<Shape> separateShapeIntoRegions(Shape shape) {
        ArrayList<Shape> regions = new ArrayList<Shape>();

        PathIterator pi = shape.getPathIterator(null);
//        int ii = 0;
        GeneralPath gp = new GeneralPath();
        while (!pi.isDone()) {
            double[] coords = new double[6];
            int pathSegmentType = pi.currentSegment(coords);
            int windingRule = pi.getWindingRule();
            gp.setWindingRule(windingRule);
            if (pathSegmentType == PathIterator.SEG_MOVETO) {
                gp = new GeneralPath();
                gp.setWindingRule(windingRule);
                gp.moveTo(coords[0], coords[1]);
            } else if (pathSegmentType == PathIterator.SEG_LINETO) {
                gp.lineTo(coords[0], coords[1]);
            } else if (pathSegmentType == PathIterator.SEG_QUADTO) {
                gp.quadTo(coords[0], coords[1], coords[2], coords[3]);
            } else if (pathSegmentType == PathIterator.SEG_CUBICTO) {
                gp.curveTo(
                        coords[0], coords[1],
                        coords[2], coords[3],
                        coords[4], coords[5]);
            } else if (pathSegmentType == PathIterator.SEG_CLOSE) {
                gp.closePath();
                regions.add(new Area(gp));
            } else {
                System.err.println("Unexpected value! " + pathSegmentType);
            }

            pi.next();
        }

        return regions;
    }

    static BufferedImage getImageForChessPiece(
            int piece, int side, boolean gradient) {
        int sz = Tile.font.getSize();
        //int sz = (int)this.getFontSize();
        BufferedImage bi = new BufferedImage(
                sz, sz, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(
                RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(
                RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        FontRenderContext frc = g.getFontRenderContext();
        GlyphVector gv = Tile.font.createGlyphVector(frc, upieces[piece]);
        //Rectangle2D box1 = gv.getVisualBounds();

        Shape shape1 = gv.getOutline();
        Rectangle r = shape1.getBounds();
        int spaceX = sz - r.width;
        int spaceY = sz - r.height;
        AffineTransform trans = AffineTransform.getTranslateInstance(
                -r.x + (spaceX / 2f), -r.y + (spaceY / 2f));

        Shape shapeCentered = trans.createTransformedShape(shape1);

        Shape imageShape = new Rectangle2D.Double(0, 0, sz, sz);
        Area imageShapeArea = new Area(imageShape);
        Area shapeArea = new Area(shapeCentered);
        imageShapeArea.subtract(shapeArea);
        ArrayList<Shape> regions = separateShapeIntoRegions(imageShapeArea);
        g.setStroke(new BasicStroke(1));
        g.setColor(pieceColors[side]);
        Color baseColor = pieceColors[side];
        if (gradient) {
            GradientPaint gp = new GradientPaint(
                    sz / 2f - (r.width / 4f), sz / 2f - (r.height / 4f), baseColor.brighter(),
                    sz / 2f + (r.width / 4f), sz / 2f + (r.height / 4f), baseColor,
                    false);
            g.setPaint(gp);
        } else {
            g.setColor(baseColor);
        }

        for (Shape region : regions) {
            Rectangle r1 = region.getBounds();
            if (!(r1.getX() < 0.001) || !(r1.getY() < 0.001)) {
                g.fill(region);
            }
        }
        g.setColor(outlineColor);
        g.fill(shapeArea);
        g.dispose();

        return bi;
    }


}
