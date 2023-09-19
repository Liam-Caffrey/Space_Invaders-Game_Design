import java.awt.Image;
/*
 * Liam Caffrey
 * 21378316
 *
 */
public class PlayerBullet extends Sprite2D {
    public PlayerBullet(Image i) {
        super(i,i);
    }
    public boolean move() {
        y-=10;
        return (y<0); //if bullet moves offscreen return true
    }
}