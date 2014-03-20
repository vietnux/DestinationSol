package com.miloshpetrov.sol2.game.planet;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.miloshpetrov.sol2.Const;
import com.miloshpetrov.sol2.TexMan;
import com.miloshpetrov.sol2.common.Col;
import com.miloshpetrov.sol2.common.SolMath;
import com.miloshpetrov.sol2.game.*;
import com.miloshpetrov.sol2.game.maze.Maze;
import com.miloshpetrov.sol2.game.maze.MazeConfigs;
import com.miloshpetrov.sol2.game.ship.HullConfigs;
import com.miloshpetrov.sol2.save.SaveData;

import java.util.ArrayList;

public class PlanetMan {

  private final ArrayList<SolSystem> mySystems;
  private final ArrayList<Planet> myPlanets;
  private final ArrayList<SystemBelt> myBelts;
  private final LandingPlaceFinder myLandingPlaceFinder;
  private final PlanetConfigs myPlanetConfigs;
  private final MazeConfigs myMazeConfigs;
  private final ArrayList<Maze> myMazes;
  private final SunSingleton mySunSingleton;
  private final SysConfigs mySysConfigs;
  private Planet myNearestPlanet;

  public PlanetMan(TexMan texMan, HullConfigs hullConfigs) {
    myPlanetConfigs = new PlanetConfigs(texMan, hullConfigs);
    mySysConfigs = new SysConfigs(texMan, hullConfigs);
    myMazeConfigs = new MazeConfigs(texMan, hullConfigs);

    mySystems = new ArrayList<SolSystem>();
    myMazes = new ArrayList<Maze>();
    myPlanets = new ArrayList<Planet>();
    myBelts = new ArrayList<SystemBelt>();
    myLandingPlaceFinder = new LandingPlaceFinder();
    mySunSingleton = new SunSingleton(texMan);
  }

  public void fill(SaveData sd) {
    if (sd != null) {
      mySystems.addAll(sd.systems);
      myPlanets.addAll(sd.planets);
    } else {
      new SystemsBuilder().build(mySystems, myPlanets, myBelts, myPlanetConfigs, myMazeConfigs, myMazes, mySysConfigs);
    }
  }

  public void update(SolGame game) {
    Vector2 camPos = game.getCam().getPos();
    for (Planet p : myPlanets) {
      p.update(game);
    }
    for (Maze m : myMazes) {
      m.update(game);
    }

    myNearestPlanet = getNearestPlanet(camPos);

    SolSystem nearestSys = getNearestSystem(camPos);
    applyGrav(game, nearestSys);
  }

  public Planet getNearestPlanet(Vector2 pos) {
    float minDst = Float.MAX_VALUE;
    Planet res = null;
    for (Planet p : myPlanets) {
      float dst = pos.dst(p.getPos());
      if (dst < minDst) {
        minDst = dst;
        res = p;
      }
    }
    return res;
  }

  private void applyGrav(SolGame game, SolSystem nearestSys) {
    float npGh = myNearestPlanet.getGroundHeight();
    float npFh = myNearestPlanet.getFullHeight();
    Vector2 npPos = myNearestPlanet.getPos();
    Vector2 sysPos = nearestSys.getPos();
    float npGravConst = myNearestPlanet.getGravConst();

    for (SolObj obj : game.getObjMan().getObjs()) {
      if (!obj.receivesGravity()) continue;

      Vector2 objPos = obj.getPos();
      float minDist;
      Vector2 srcPos;
      float gravConst;
      boolean onPlanet;
      float toNp = npPos.dst(objPos);
      float toSys = sysPos.dst(objPos);
      if (toNp < npFh) {
        minDist = npGh;
        srcPos = npPos;
        gravConst = npGravConst;
        onPlanet = true;
      } else if (toSys < Const.SUN_RADIUS) {
        minDist = SunSingleton.SUN_HOT_RAD;
        srcPos = sysPos;
        gravConst = SunSingleton.GRAV_CONST;
        onPlanet = false;
      } else {
        continue;
      }

      Vector2 grav = SolMath.getVec(srcPos);
      grav.sub(objPos);
      float len = grav.len();
      grav.nor();
      if (len < minDist) {
        len = minDist;
      }
      float g = gravConst / len / len;
      grav.scl(g);
      obj.receiveAcc(grav, game);
      SolMath.free(grav);
      if (!onPlanet) {
        mySunSingleton.doDmg(game, obj, toSys);
      }
    }

  }

  public Planet getNearestPlanet() {
    return myNearestPlanet;
  }

  public void drawDebug(Drawer drawer, SolGame game) {
    if (DebugAspects.PLANET_BORDERS) {
      float lineWidth = game.getCam().getRealLineWidth();
      for (Planet p : myPlanets) {
        Vector2 pos = p.getPos();
        float angle = p.getAngle();
        float fh = p.getFullHeight();
        Color col = p == myNearestPlanet ? Col.W : Col.G;
        drawer.drawCircle(pos, p.getGroundHeight(), col, lineWidth);
        drawer.drawCircle(pos, fh, col, lineWidth);
        drawer.drawLine(pos.x, pos.y, angle, fh, col, lineWidth);
      }

    }
  }

  public ArrayList<Planet> getPlanets() {
    return myPlanets;
  }

  public ArrayList<SystemBelt> getBelts() {
    return myBelts;
  }

  public ArrayList<SolSystem> getSystems() {
    return mySystems;
  }

  public Vector2 findLandingPlace(SolGame game, Planet p, ArrayList<Float> takenAngles) {
    return myLandingPlaceFinder.find(game, p, takenAngles);
  }

  public ArrayList<Maze> getMazes() {
    return myMazes;
  }

  public SolSystem getNearestSystem(Vector2 pos) {
    float minDst = Float.MAX_VALUE;
    SolSystem res = null;
    for (SolSystem s : mySystems) {
      float dst = pos.dst(s.getPos());
      if (dst < minDst) {
        minDst = dst;
        res = s;
      }
    }
    return res;
  }

  public Maze getNearestMaze(Vector2 pos) {
    float minDst = Float.MAX_VALUE;
    Maze res = null;
    for (Maze m : myMazes) {
      float dst = pos.dst(m.getPos());
      if (dst < minDst) {
        minDst = dst;
        res = m;
      }
    }
    return res;
  }

  public void drawSun(SolGame game, Drawer drawer) {
    mySunSingleton.draw(game, drawer);
  }

}
