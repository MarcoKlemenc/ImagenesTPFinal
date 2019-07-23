package Interfaces;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseArea extends MouseAdapter {

    private ImageContainer container;

    public MouseArea(ImageContainer container) {
        this.container = container;
    }

    public void mousePressed(MouseEvent e) {
        if (this.container.getMode() != null) {
            this.container.setX1(e.getX());
            this.container.setY1(e.getY());
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (this.container.getMode() != null) {
            this.container.setX2(e.getX());
            this.container.setY2(e.getY());
            this.container.repaint();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (this.container.getMode() != null) {
            this.container.setX2(e.getX());
            this.container.setY2(e.getY());
        }
        this.container.endMouseMovement();
    }
}