import java.io.Serializable;

public class GameInfo implements Serializable{
    private String gameID;
    private String mapName;
    private int posx;
    private int posy;
    private Player[] others;

    public GameInfo(String gameID, String mapName, int posx, int posy, Player[] others) {
        this.gameID = gameID;
        this.mapName = mapName;
        this.posx = posx;
        this.posy = posy;
        this.others = others;
    }

    public String getGameID() {
        return gameID;
    }

    public String getMapName() {
        return mapName;
    }

    public int getPosx() {
        return posx;
    }

    public int getPosy() {
        return posy;
    }

    public Player[] getOthers() {
        return others;
    }
}
