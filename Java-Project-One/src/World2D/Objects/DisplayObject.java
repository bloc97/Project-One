/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package World2D.Objects;

import Physics2D.Vector2;
import World2D.Camera;
import java.awt.Graphics2D;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author bowen
 */
public interface DisplayObject {
    
    public void renderTransform(Graphics2D g2, Camera camera); //part of the rendering requiring affine transforms
    public void renderNoTransform(Graphics2D g2, Camera camera); //part of the rendering without transforms (per pixel)
    
    //public void setPos(double x, double y);
    public void registerUpdate(Date date);
    
    public boolean isVisible(double scale);
    
    public void hide();
    public void show();
    
    public BigDecimal getX();
    public BigDecimal getY();
    
    public double getSx(Camera camera);
    public double getSy(Camera camera);
    public double getSr(Camera camera);
            
    public static double getSx(double x, BigDecimal offsetX, Camera camera) {
        return ((x + offsetX.subtract(camera.getxPos()).doubleValue()) * camera.getScale() + camera.getxScrOffset());
    }

    public static double getSy(double y, BigDecimal offsetY, Camera camera) {
        return ((y + offsetY.add     (camera.getyPos()).doubleValue()) * -camera.getScale() + camera.getyScrOffset());
    }
    
    public static double getSr(double radius, Camera camera) {
        double rOut = (Math.log10(radius)-6)*3;
        double rIn = radius*camera.getScale();
        double r = (rIn > rOut) ? rIn : rOut;
        return r;
    }
    
}
