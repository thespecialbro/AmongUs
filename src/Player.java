import java.io.Serializable;

public class Player implements Serializable{
    private String name;
    private String color;
    private double posx;
    private double posy;
    private boolean isAlive;
    private boolean isImpostor;

    public Player(String name, String color, double posx, double posy) {
        this.name = name;
        this.color = color;
        this.posx = posx;
        this.posy = posy;
    }


    public Player(String name, String color, boolean isAlive, boolean isImpostor) {
        this.name = name;
        this.color = color;
        this.isAlive = isAlive;
        this.isImpostor = isImpostor;
    }



    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public double getPosX() {
        return posx;
    }

    public double getPosY() {
        return posy;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isImpostor() {
        return isImpostor;
    }
}
