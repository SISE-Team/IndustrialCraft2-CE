"""Group independent native blast phases and current damage/effect registry ownership."""
from reorganize_energy_devices import reorganize, rewrite_named_roles
RULES={
 'Ic2Explosion':('ic2ce.core','ic2ce.neoforge.explosion','NativeBlast'),
 'Ic2DamageSource':('ic2ce.core','ic2ce.neoforge.damage','DamageProfiles'),
 'Ic2Potion':('ic2ce.core','ic2ce.neoforge.damage','RadiationEffect')}
def rewrite_damage(text):
 return rewrite_named_roles(text,RULES,{},implicit_packages=('ic2ce.core',))
def main():reorganize(RULES,rewrite_damage,'damage-layout-migration.json','Phased native blast tracing, real destruction callbacks and current world damage/effect ownership')
if __name__=='__main__':main()
