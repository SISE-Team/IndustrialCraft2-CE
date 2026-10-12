package ic2.core.uu;

import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class UuIndex {
   public static final UuIndex instance = new UuIndex();
   protected final List<IRecipeResolver> resolvers = new ArrayList<>();
   protected final List<ILateRecipeResolver> lateResolvers = new ArrayList<>();

   private UuIndex() {
   }

   public void addResolver(IRecipeResolver resolver) {
      this.resolvers.add(resolver);
   }

   public void addResolver(ILateRecipeResolver resolver) {
      this.lateResolvers.add(resolver);
   }

   public void add(ItemStack stack, double value) {
      if (stack != null && stack.getItem() != null) {
         UuGraph.set(stack, value);
      } else {
         throw new NullPointerException("invalid itemstack to add");
      }
   }

   public double get(ItemStack request) {
      return UuGraph.get(request);
   }

   public double getInBuckets(ItemStack request) {
      double ret = UuGraph.get(request);
      return ret * 1.0E-5;
   }

   public void init() {
   }

   public void init(net.minecraft.server.level.ServerLevel world) {
      this.resolvers.clear(); this.lateResolvers.clear();
      this.addResolver(new RecipeResolver());
      this.addResolver(new VanillaSmeltingResolver());
      for (var getter : java.util.List.of(ic2.api.recipe.Recipes.macerator, ic2.api.recipe.Recipes.extractor,
         ic2.api.recipe.Recipes.compressor, ic2.api.recipe.Recipes.centrifuge, ic2.api.recipe.Recipes.blockcutter,
         ic2.api.recipe.Recipes.blastfurnace, ic2.api.recipe.Recipes.metalformerExtruding,
         ic2.api.recipe.Recipes.metalformerCutting, ic2.api.recipe.Recipes.metalformerRolling, ic2.api.recipe.Recipes.oreWashing)) {
         this.addResolver(new MachineRecipeResolver(getter.get(world)));
      }
      this.addResolver(new ScrapBoxResolver());
      this.addResolver(new RecyclerResolver());
      this.refresh(true);
   }

   public void refresh(boolean reset) {
      if (reset) UuGraph.clearInitialValues();
      Config config = MainConfig.get().getSub("balance/uu-values/world scan");
      if (config == null) {
         IC2.log.info(LogCategory.Uu, "Loading predefined UU world scan values, run /ic2 uu-world-scan <small|medium|large> to calibrate them for your world.");
         config = new Config("uu scan values");

         try {
            config.load(IC2.class.getResourceAsStream("/assets/ic2/config/uu_scan_values.ini"));
         } catch (Exception var9) {
            throw new RuntimeException("Error loading base config", var9);
         }
      } else {
         IC2.log.debug(LogCategory.Uu, "Loading UU world scan values from the user config.");
      }

      Iterator<Config.Value> it = config.valueIterator();

      while (it.hasNext()) {
         Config.Value value = it.next();

         ItemStack stack;
         try {
            stack = ConfigUtil.asStack(value.name.replace("IC2:misc_resource#", "ic2:").replace("ic2:misc_resource#", "ic2:"));
         } catch (ParseException var8) {
            throw new Config.ParseException("invalid key", value, var8);
         }

         if (stack == null) {
            IC2.log.warn(LogCategory.Uu, "UU world-scan config: Can't find ItemStack for %s, ignoring the entry in line %d.", value.name, value.getLine());
         } else {
            this.add(stack, value.getDouble());
         }
      }

      it = MainConfig.get().getSub("balance/uu-values/predefined").valueIterator();

      while (it.hasNext()) {
         Config.Value value = it.next();

         ItemStack stackx;
         try {
            stackx = ConfigUtil.asStack(value.name.replace("IC2:misc_resource#", "ic2:").replace("ic2:misc_resource#", "ic2:"));
         } catch (ParseException var7) {
            throw new Config.ParseException("invalid key", value, var7);
         }

         if (stackx == null) {
            IC2.log.warn(LogCategory.Uu, "UU predefined config: Can't find ItemStack for %s, ignoring the entry in line %d.", value.name, value.getLine());
         } else {
            this.add(stackx, value.getDouble());
         }
      }

      UuGraph.build(reset);
   }
}
