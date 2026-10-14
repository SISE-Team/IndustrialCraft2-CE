package ic2ce.neoforge.client.widget;

/** Atlas endpoints are sampled live; geometry owners choose their coordinate unit. */
public interface AtlasRegion {
  int getUS();

  int getVS();

  int getUE();

  int getVE();
}
