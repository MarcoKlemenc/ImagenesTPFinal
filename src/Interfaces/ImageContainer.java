package Interfaces;

import javax.swing.*;
import java.awt.*;

public class ImageContainer extends JLabel {

    private AppVideo app;
    private String mode = null;

    private int x1;
    private int y1;
    private int x2;
    private int y2;

    private int[][] contours = null;
    private int[] contourColor = null;

    ImageContainer(AppVideo app) {
        super();
        MouseArea listener = new MouseArea(this);
        addMouseListener(listener);
        addMouseMotionListener(listener);
        this.app = app;
    }

    String getMode() {
        return mode;
    }

    void setMode(String mode) {
        this.mode = mode;
    }

    void setX1(int x1) {
        this.x1 = x1;
    }

    void setY1(int y1) {
        this.y1 = y1;
    }

    void setX2(int x2) {
        this.x2 = x2;
    }

    void setY2(int y2) {
        this.y2 = y2;
    }

    void setContours(int[][] contours) {
        this.contours = contours;
    }

    void setContourColor(int[] contourColor) {
        this.contourColor = contourColor;
    }

    void endMouseMovement() {
        if (this.mode.equals("activeContours")) {
            this.app.addActiveContour(this.x1, this.y1, this.x2, this.y2);
            this.app.getActiveContours();
        }
        this.mode = null;
        this.x1 = 0;
        this.y1 = 0;
        this.x2 = 0;
        this.y2 = 0;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (this.contours != null) {
            g2.setColor(new Color(this.contourColor[0], this.contourColor[1], this.contourColor[2]));
            for (int x = 0; x < this.contours.length; x++) {
                for (int y = 0; y < this.contours[0].length; y++) {
                    if (this.contours[x][y] == -1) {
                        g2.drawLine(x, y, x, y);
                    }
                }
            }
        }
        if (this.mode != null) {
            g2.setColor(new Color(0, 192, 255, 96));
            g2.fillRect(this.x1, this.y1, this.x2 - this.x1 + 1, this.y2 - this.y1 + 1);
        }
    }
}
