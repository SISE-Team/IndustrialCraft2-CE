package ic2ce.neoforge.client.widget;

/** Value geometry owns its four atlas endpoints and is safe to share between projections. */
public record FixedAtlasRegion(int uS, int vS, int uE, int vE) implements AtlasRegion {
  public int getUS() {
    return uS;
  }

  public int getVS() {
    return vS;
  }

  public int getUE() {
    return uE;
  }

  public int getVE() {
    return vE;
  }
}
