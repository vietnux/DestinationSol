package com.miloshpetrov.sol2.common;

import com.badlogic.gdx.graphics.Color;

public class ColUtil {
  public static void fromHSB(float hue, float saturation, float brightness, float a, Color dest) {
    float r = 0, g = 0, b = 0;
    if (saturation == 0) {
      r = g = b = brightness;
    } else {
      float h = (hue - (float)Math.floor(hue)) * 6.0f;
      float f = h - (float) Math.floor(h);
      float p = brightness * (1.0f - saturation);
      float q = brightness * (1.0f - saturation * f);
      float t = brightness * (1.0f - (saturation * (1.0f - f)));
      switch ((int) h) {
      case 0:
        r = brightness;
        g = t;
        b = p;
        break;
      case 1:
        r = q;
        g = brightness;
        b = p;
        break;
      case 2:
        r = p;
        g = brightness;
        b = t;
        break;
      case 3:
        r = p;
        g = q;
        b = brightness;
        break;
      case 4:
        r = t;
        g = p;
        b = brightness;
        break;
      case 5:
        r = brightness;
        g = p;
        b = q;
        break;
      }
    }
    dest.r = r;
    dest.g = g;
    dest.b = b;
    dest.a = a;
  }

  public static float[] toHSB(Color src) {
    int r = (int)(src.r * 255 + .5f);
    int g = (int)(src.g * 255 + .5f);
    int b = (int)(src.b * 255 + .5f);
    float hue, saturation, brightness;
    int cmax = (r > g) ? r : g;
    if (b > cmax) cmax = b;
    int cmin = (r < g) ? r : g;
    if (b < cmin) cmin = b;

    brightness = ((float) cmax) / 255.0f;
    if (cmax != 0)
      saturation = ((float) (cmax - cmin)) / ((float) cmax);
    else
      saturation = 0;
    if (saturation == 0)
      hue = 0;
    else {
      float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
      float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
      float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
      if (r == cmax)
        hue = bluec - greenc;
      else if (g == cmax)
        hue = 2.0f + redc - bluec;
      else
        hue = 4.0f + greenc - redc;
      hue = hue / 6.0f;
      if (hue < 0)
        hue = hue + 1.0f;
    }
    float[] hsba = new float[4];
    hsba[0] = hue;
    hsba[1] = saturation;
    hsba[2] = brightness;
    hsba[3] = src.a;
    return hsba;
  }

  public static Color load(String s) {
    String[] parts = s.split(" ");
    boolean hsb = "hsb".equals(parts[0]);
    int idx = hsb ? 1 : 0;

    float v1 = Float.parseFloat(parts[idx++]);
    float v2 = Float.parseFloat(parts[idx++]);
    float v3 = Float.parseFloat(parts[idx++]);
    float a = 1;
    if (parts.length > idx) a = Float.parseFloat(parts[idx]);
    Color res = new Color();
    if (hsb) {
      fromHSB(v1, v2, v3, a, res);
    } else {
      res.set(v1, v2, v3, a);
    }
    return res;
  }
}