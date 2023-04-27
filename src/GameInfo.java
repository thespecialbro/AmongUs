import java.io.Serializable;

public class GameInfo implements Serializable{
    private String gameID;
    private String mapName;
    private double posx;
    private double posy;
    private Player[] others;
    private boolean isAlive = true;

    public GameInfo(String gameID, String mapName, double posx, double posy, Player[] others, boolean isAlive) {
        this.gameID = gameID;
        this.mapName = mapName;
        this.posx = posx;
        this.posy = posy;
        this.others = others;
        this.isAlive = isAlive;
    }
    

    public String getGameID() {
        return gameID;
    }

    public String getMapName() {
        return mapName;
    }

    public double getPosx() {
        return posx;
    }

    public double getPosy() {
        return posy;
    }

    public Player[] getOthers() {
        return others;
    }

    

    public String toString() {
        return String.format("(%.2f %.2f), %d visible", posx, posy, others!=null ? others.length : 0);
    }

    public boolean getAlive() {
        return isAlive;
    }
}
