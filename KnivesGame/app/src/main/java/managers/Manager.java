package managers;

import game.GameData;

public abstract class Manager {
    protected GameData gameData = GameData.getInstance();
    
    protected Manager() { }

    public abstract void update();
}
