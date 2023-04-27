import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private String color;
    private double posx;
    private double posy;
    private boolean isAlive = true;
    private boolean isImpostor;
    private int id;
    private int facing;

    public Player(String name, String color, double posx, double posy, int facing) {
        this.name = name;
        this.color = color;
        this.posx = posx;
        this.posy = posy;
        this.facing = facing;
    }


    public Player(String name, String color, boolean isImpostor) {

        this.name = name;
        this.color = color;
        this.isImpostor = isImpostor;
    }

    public Player(boolean isAlive) {
        this.isAlive = isAlive;
    }
    

    public void setId(int id) {
        this.id = id;
    }

    public int getId() { return id; }

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

    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    public boolean isImpostor() {
        return isImpostor;
    }

    public int getFacing() {
        return facing;
    }
}
