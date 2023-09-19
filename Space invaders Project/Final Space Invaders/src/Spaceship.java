import java.awt.Image;
/*
 * Liam Caffrey
 * 21378316
 *
 */


public class Spaceship extends Sprite2D {
    private Image bulletImage;
    public Spaceship(Image i, Image bullet) {
        super(i,i);
        bulletImage = bullet;
    }
    public void move() {
        x+=xSpeed;
        if (x<=0) {
            x=0;
            xSpeed=0;
        }
        else if (x>=InvadersApplication.WindowSize.width-myImage.getWidth(null)) {
            x=InvadersApplication.WindowSize.width-myImage.getWidth(null);
            xSpeed=0; //stops our player from moving out of bounds
        }
    }
    public PlayerBullet shootBullet() {
        PlayerBullet b = new PlayerBullet(bulletImage);
        b.setPosition(this.x+54/2, this.y); //shoot a bullet
        return b;
    }
}