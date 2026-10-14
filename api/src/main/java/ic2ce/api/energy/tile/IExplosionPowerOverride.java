package ic2ce.api.energy.tile;

public interface IExplosionPowerOverride {
   boolean shouldExplode();

   float getExplosionPower(int var1, float var2);
}
