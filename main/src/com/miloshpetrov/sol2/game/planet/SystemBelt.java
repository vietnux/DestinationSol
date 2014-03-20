package com.miloshpetrov.sol2.game.planet;

import com.badlogic.gdx.math.Vector2;

public class SystemBelt {
  private final Float myHalfWidth;
  private final float myRadius;
  private final SolSystem myS;
  private final SysConfig myConfig;

  public SystemBelt(Float halfWidth, float radius, SolSystem s, SysConfig config) {
    myHalfWidth = halfWidth;
    myRadius = radius;
    myS = s;
    myConfig = config;
  }

  public float getRadius() {
    return myRadius;
  }

  public Float getHalfWidth() {
    return myHalfWidth;
  }

  public boolean contains(Vector2 pos) {
    float toCenter = myS.getPos().dst(pos);
    return myRadius - myHalfWidth < toCenter && toCenter < myRadius + myHalfWidth;
  }

  public SysConfig getConfig() {
    return myConfig;
  }
}
