package com.miloshpetrov.sol2.game.item;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.miloshpetrov.sol2.SolFiles;
import com.miloshpetrov.sol2.common.SolMath;
import com.miloshpetrov.sol2.game.SolGame;
import com.miloshpetrov.sol2.game.ship.ShipHull;
import com.miloshpetrov.sol2.game.ship.SolShip;
import com.miloshpetrov.sol2.game.sound.SolSounds;
import com.miloshpetrov.sol2.game.sound.SoundMan;

public class Shield implements SolItem {
  public static final float SIZE_PERC = .7f;
  private final Config myConfig;
  private float myLife;
  private float myIdleTime;

  private Shield(Config config) {
    myConfig = config;
    myLife = myConfig.maxLife;
  }

  public void update(SolGame game) {
    float ts = game.getTimeStep();
    if (myIdleTime >= myConfig.myMaxIdleTime) {
      if (myLife < myConfig.maxLife) {
        float regen = myConfig.regenSpd * ts;
        myLife = SolMath.approach(myLife, myConfig.maxLife, regen);
      }
    } else {
      myIdleTime += ts;
    }
  }

  @Override
  public String getTexName() {
    return "shield";
  }

  @Override
  public String getDisplayName() {
    return myConfig.displayName;
  }

  @Override
  public float getPrice() {
    return myConfig.price;
  }

  @Override
  public String getDesc() {
    return myConfig.desc;
  }

  @Override
  public SolItem copy() {
    return new Shield(myConfig);
  }

  @Override
  public boolean isSame(SolItem item) {
    return item instanceof Shield && ((Shield) item).myConfig == myConfig;
  }

  public float getLife() {
    return myLife;
  }

  public float getMaxLife() {
    return myConfig.maxLife;
  }

  public float absorb(SolGame game, float dmg, Vector2 pos, SolShip ship) {
    if (dmg <= 0) return 0;
    game.getSoundMan().play(game, myConfig.sounds, pos, ship);
    myIdleTime = 0f;
    if (myLife > 0) {
      ShipHull hull = ship.getHull();
      game.getPartMan().shieldSpark(game, pos, hull);
    }
    if (myLife >= dmg) {
      myLife -= dmg;
      return 0;
    }
    dmg -= myLife;
    myLife = 0;
    return dmg;
  }
  
  public static class Config {
    public final String displayName;
    public final int price;
    public final String desc;
    public final SolSounds sounds;
    public final Shield example;
    public float maxLife;
    public float myMaxIdleTime = 2;
    public float regenSpd;

    private Config(int maxLife, String displayName, int price, String desc, SolSounds sounds) {
      this.maxLife = maxLife;
      this.displayName = displayName;
      this.price = price;
      this.desc = desc;
      this.sounds = sounds;
      regenSpd = this.maxLife / 3;
      example = new Shield(this);
    }

    public static void loadConfigs(ItemMan itemMan, SoundMan soundMan) {
      JsonReader r = new JsonReader();
      FileHandle configFile = SolFiles.readOnly(ItemMan.ITEM_CONFIGS_DIR + "shields.json");
      JsonValue parsed = r.parse(configFile);
      for (JsonValue sh : parsed) {
        int maxLife = sh.getInt("maxLife");
        String displayName = sh.getString("displayName");
        int price = sh.getInt("price");
        String desc = sh.getString("desc");
        String soundsDir = sh.getString("sounds");
        SolSounds sounds = soundMan.getSounds(soundsDir, configFile);
        Config config = new Config(maxLife, displayName, price, desc, sounds);
        itemMan.registerItem(sh.name(), config.example);
      }
    }
  }
}
