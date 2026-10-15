package ic2ce.neoforge.item.tool;

import ic2ce.api.item.*;
import ic2ce.api.network.INetworkItemEventListener;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.material.CommonMaterialTags;
import ic2ce.neoforge.registry.SoundDefinitions;
import ic2ce.neoforge.runtime.SideServices;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Component-defined electrical tool foundation; native charge authority and per-owner client audio
 * are separate.
 */
public abstract class PoweredTool extends Item
    implements IElectricItem,
        INetworkItemEventListener,
        IItemHudInfo,
        ic2ce.api.item.CreativeVariants {
  public double operationEnergyCost;
  public int maxCharge, transferLimit, tier;
  protected final float speed;
  private final ToolMaterial material;
  private final List<TagKey<Block>> effectiveBlocks;

  protected PoweredTool(Properties properties, int cost) {
    this(properties, cost, ToolMaterial.IRON, List.of());
  }

  protected PoweredTool(
      Properties properties, int cost, ToolMaterial material, Collection<TagKey<Block>> effective) {
    this(properties, cost, material, effective, true);
  }

  protected PoweredTool(
      Properties properties,
      int cost,
      ToolMaterial material,
      Collection<TagKey<Block>> effective,
      boolean creativeMining) {
    super(components(properties, material, effective, creativeMining));
    if (cost < 0) throw new IllegalArgumentException("Negative tool operation cost");
    operationEnergyCost = cost;
    this.material = Objects.requireNonNull(material);
    effectiveBlocks = List.copyOf(effective);
    speed = material.speed();
  }

  private static Properties components(
      Properties properties,
      ToolMaterial material,
      Collection<TagKey<Block>> effective,
      boolean creativeMining) {
    var lookup =
        net.minecraft.core.registries.BuiltInRegistries.acquireBootstrapRegistrationLookup(
            net.minecraft.core.registries.BuiltInRegistries.BLOCK);
    var rules = new ArrayList<Tool.Rule>(effective.size() + 1);
    rules.add(Tool.Rule.deniesDrops(lookup.getOrThrow(material.incorrectBlocksForDrops())));
    for (var tag : effective)
      rules.add(Tool.Rule.minesAndDrops(lookup.getOrThrow(tag), material.speed()));
    var modifiers =
        ItemAttributeModifiers.builder()
            .add(
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    Item.BASE_ATTACK_DAMAGE_ID,
                    2 + material.attackDamageBonus(),
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
            .add(
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    Item.BASE_ATTACK_SPEED_ID,
                    -3,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
            .build();
    return properties
        .stacksTo(1)
        .component(DataComponents.WEAPON, new Weapon(0))
        .component(DataComponents.TOOL, new Tool(List.copyOf(rules), 1, 0, creativeMining))
        .attributes(modifiers);
  }

  public static boolean consumeEnergy(
      ItemStack stack, double amount, int tier, LivingEntity entity) {
    if (stack == null
        || stack.isEmpty()
        || !(stack.getItem() instanceof IElectricItem)
        || !Double.isFinite(amount)
        || amount < 0) return false;
    if (entity != null && entity.level().isClientSide())
      return ElectricItem.manager.canUse(stack, amount);
    if (!ElectricItem.manager.canUse(stack, amount)) return false;
    boolean consumed =
        entity == null
            ? Math.abs(
                    ElectricItem.manager.discharge(stack, amount, tier, true, false, false)
                        - amount)
                <= 1e-8
            : ElectricItem.manager.use(stack, amount, entity);
    if (consumed
        && amount > 0
        && ElectricItem.manager.getCharge(stack) <= 0
        && entity instanceof Player player)
      SideServices.network(true).initiateItemEvent(player, stack, 0, true);
    return consumed;
  }

  public boolean consumeEnergy(ItemStack stack, double amount, LivingEntity entity) {
    return consumeEnergy(stack, amount, tier, entity);
  }

  @Override
  public void onNetworkEvent(ItemStack stack, Player player, int event) {
    if (event == 0 && player.level().isClientSide()) player.playSound(getShutdownSound(), 1, 1);
  }

  @Override
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    return List.of(
        Objects.toString(ElectricItem.manager.getToolTip(stack), ""),
        LocalizedText.translate("ic2.item.tooltip.PowerTier", tier));
  }

  private void recharge(ItemStack stack, Player player) {
    if (player != null && !player.level().isClientSide())
      ElectricItem.manager.use(stack, 0, player);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    recharge(context.getItemInHand(), context.getPlayer());
    return super.useOn(context);
  }

  @Override
  public InteractionResult use(Level world, Player player, InteractionHand hand) {
    recharge(player.getItemInHand(hand), player);
    return super.use(world, player, hand);
  }

  private boolean effective(BlockState state) {
    for (var tag : effectiveBlocks) if (CommonMaterialTags.matches(state, tag)) return true;
    return false;
  }

  @Override
  public float getDestroySpeed(ItemStack stack, BlockState state) {
    return effective(state) && canUse(stack) ? speed : 1;
  }

  @Override
  public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
    return !CommonMaterialTags.matches(state, material.incorrectBlocksForDrops())
        && effective(state);
  }

  @Override
  public boolean mineBlock(
      ItemStack stack, Level world, BlockState state, BlockPos position, LivingEntity actor) {
    if (!world.isClientSide() && state.getDestroySpeed(world, position) != 0)
      consumeEnergy(stack, operationEnergyCost, actor);
    return true;
  }

  @Override
  public void hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity owner) {}

  public int getEnchantmentValue() {
    return 0;
  }

  public boolean isEnchantable(ItemStack stack) {
    return false;
  }

  @Override
  public boolean canProvideEnergy(ItemStack stack) {
    return false;
  }

  @Override
  public double getMaxCharge(ItemStack stack) {
    return Math.max(0, maxCharge);
  }

  @Override
  public int getTier(ItemStack stack) {
    return Math.max(0, tier);
  }

  @Override
  public double getTransferLimit(ItemStack stack) {
    return Math.max(0, transferLimit);
  }

  public void addCreativeVariants(java.util.function.Consumer<ItemStack> output) {
    output.accept(getItemStack(0));
    output.accept(getItemStack(getMaxCharge(ItemStack.EMPTY)));
  }

  protected ItemStack getItemStack(double charge) {
    var stack = new ItemStack(this);
    ElectricItem.manager.charge(stack, charge, Integer.MAX_VALUE, true, false);
    return stack;
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      TooltipDisplay display,
      java.util.function.Consumer<Component> text,
      TooltipFlag flags) {
    ic2ce.neoforge.item.tooltip.ElectricTooltip.addTooltip(stack, text, flags);
  }

  public boolean onDroppedByPlayer(ItemStack stack, Player player) {
    return true;
  }

  protected SoundEvent getIdleSound(LivingEntity actor, ItemStack stack) {
    return null;
  }

  protected SoundEvent getStartSound(LivingEntity actor, ItemStack stack) {
    return null;
  }

  protected SoundEvent getStopSound(LivingEntity actor, ItemStack stack) {
    return null;
  }

  public final SoundEvent heldIdleSound(LivingEntity actor, ItemStack stack) {
    return getIdleSound(actor, stack);
  }

  public final SoundEvent heldStartSound(LivingEntity actor, ItemStack stack) {
    return getStartSound(actor, stack);
  }

  public final SoundEvent heldStopSound(LivingEntity actor, ItemStack stack) {
    return getStopSound(actor, stack);
  }

  public SoundEvent getShutdownSound() {
    return SoundDefinitions.ITEM_ELECTRIC_SHUTDOWN;
  }

  @Override
  public boolean isBarVisible(ItemStack stack) {
    return true;
  }

  private double chargeFraction(ItemStack stack) {
    double fraction = ElectricItem.manager.getChargeLevel(stack);
    return Double.isFinite(fraction) ? Math.clamp(fraction, 0, 1) : 0;
  }

  @Override
  public int getBarWidth(ItemStack stack) {
    return (int) Math.round(chargeFraction(stack) * 13);
  }

  @Override
  public int getBarColor(ItemStack stack) {
    return Mth.hsvToRgb((float) (chargeFraction(stack) / 3), 1, 1);
  }

  public boolean canUse(ItemStack stack) {
    return Double.isFinite(operationEnergyCost)
        && operationEnergyCost >= 0
        && ElectricItem.manager.canUse(stack, operationEnergyCost);
  }
}
