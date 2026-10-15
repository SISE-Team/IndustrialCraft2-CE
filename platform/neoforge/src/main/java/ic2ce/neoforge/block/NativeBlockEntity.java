package ic2ce.neoforge.block;

import ic2ce.api.network.*;
import ic2ce.neoforge.client.text.LocalizedText;
import ic2ce.neoforge.component.ComponentTypes;
import ic2ce.neoforge.component.ElectricalBuffer;
import ic2ce.neoforge.component.NativeComponent;
import ic2ce.neoforge.diagnostics.DiagnosticArea;
import ic2ce.neoforge.diagnostics.ProjectLog;
import ic2ce.neoforge.menu.IHasGui;
import ic2ce.neoforge.menu.document.ScreenConditions;
import ic2ce.neoforge.power.PowerItems;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.runtime.SideServices;
import ic2ce.neoforge.world.IWorldTickCallback;
import ic2ce.neoforge.world.TickHandler;
import ic2ce.neoforge.world.WorldItems;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** Native block lifecycle, compact component ownership, NBT bridge and typed world hooks. */
public abstract class NativeBlockEntity extends BlockEntity
    implements INetworkDataProvider, INetworkUpdateListener, ScreenConditions {
  public static final String teBlockName = "teBlk";
  protected static final int lightOpacityTranslucent = 0, lightOpacityOpaque = 255;
  private static final List<AABB> FULL_CUBE = List.of(new AABB(0, 0, 0, 1, 1, 1));

  private record TickHooks(boolean client, boolean server) {}

  private static final ClassValue<TickHooks> TICK_HOOKS =
      new ClassValue<>() {
        @Override
        protected TickHooks computeValue(Class<?> type) {
          boolean client = false, server = false;
          for (var current = type;
              current != null && current != NativeBlockEntity.class && !(client && server);
              current = current.getSuperclass()) {
            for (var method : current.getDeclaredMethods()) {
              if (method.getParameterCount() != 0) continue;
              if (method.getName().equals("updateEntityClient")) client = true;
              if (method.getName().equals("updateEntityServer")) server = true;
            }
          }
          return new TickHooks(client, server);
        }
      };
  private NativeComponent[] components;
  private int componentCount;
  private ArrayList<NativeComponent> tickingComponents;
  private List<NativeComponent> componentView;
  private boolean active, enableWorldTick, initializingComponents, removalDropsEmitted;
  private byte loadState;
  private int loadGeneration;
  private HolderLookup.Provider serializationLookup;
  private String networkIdentity;
  protected final MachineBlockType teBlock;

  public NativeBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
    super(type, position, state);
    teBlock = (MachineBlockType) state.getBlock();
  }

  public final MachineBlockType getBlockType() {
    return teBlock;
  }

  @Override
  public final void setRemoved() {
    loadGeneration++;
    if (loadState == 2) onUnloaded();
    else loadState = 3;
    super.setRemoved();
  }

  @Override
  public final void clearRemoved() {
    super.clearRemoved();
    var world = getLevel();
    if (world == null) throw new IllegalStateException("Block load has no native level");
    if (loadState == 2 || loadState == 1) return;
    loadState = 1;
    removalDropsEmitted = false;
    int generation = ++loadGeneration;
    IWorldTickCallback callback =
        new IWorldTickCallback() {
          @Override
          public void onTick(Level observed) {
            if (observed != getLevel()
                || generation != loadGeneration
                || isRemoved()
                || loadState != 1) return;
            if (!observed.isLoaded(worldPosition)) {
              TickHandler.requestSingleWorldTick(observed, this);
              return;
            }
            if (observed.getBlockState(worldPosition).getBlock() != teBlock
                || observed.getBlockEntity(worldPosition) != NativeBlockEntity.this) {
              loadState = 3;
              return;
            }
            onLoaded();
          }
        };
    TickHandler.requestSingleWorldTick(world, callback);
  }

  protected void onLoaded() {
    if (loadState != 1)
      throw new IllegalStateException("Block initialized outside its queued lifecycle");
    loadState = 2;
    var hooks = TICK_HOOKS.get(getClass());
    enableWorldTick = getLevel().isClientSide() ? hooks.client() : hooks.server();
    initializingComponents = true;
    try {
      if (components != null)
        for (int index = 0; index < componentCount; index++) loadComponent(components[index]);
    } finally {
      initializingComponents = false;
    }
  }

  private void loadComponent(NativeComponent component) {
    component.onLoaded();
    if (component.enableWorldTick()) {
      if (tickingComponents == null) tickingComponents = new ArrayList<>(2);
      tickingComponents.add(component);
    }
  }

  protected void onUnloaded() {
    if (loadState == 3) return;
    loadState = 3;
    loadGeneration++;
    enableWorldTick = false;
    if (tickingComponents != null) tickingComponents.clear();
    if (components != null)
      for (int index = componentCount - 1; index >= 0; index--) components[index].onUnloaded();
  }

  public void load(CompoundTag data) {
    active = data.getBooleanOr("active", false);
    if (components == null || !data.contains("components")) return;
    var saved = data.getCompoundOrEmpty("components");
    for (var name : saved.keySet()) {
      var type = ComponentTypes.getClass(name);
      var component = type == null ? null : getComponent(type);
      if (component == null)
        ProjectLog.LOG.warn(
            DiagnosticArea.Block, "Unknown component %s while loading %s", name, this);
      else component.readFromNbt(saved.getCompoundOrEmpty(name));
    }
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    serializationLookup = input.lookup();
    load(input.read("ic2", CompoundTag.CODEC).orElseGet(CompoundTag::new));
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    var data = new CompoundTag();
    saveAdditional(data);
    output.store("ic2", CompoundTag.CODEC, data);
  }

  public final HolderLookup.Provider getSerializationLookup() {
    if (getLevel() != null) return getLevel().registryAccess();
    if (serializationLookup != null) return serializationLookup;
    var lookup = EnvProxy.get().getRegistryLookup();
    if (lookup == null)
      throw new IllegalStateException("No native serialization registry is available");
    return lookup;
  }

  public void saveAdditional(CompoundTag data) {
    data.putBoolean("active", active);
    data.remove("components");
    if (components == null) return;
    CompoundTag saved = null;
    for (int index = 0; index < componentCount; index++) {
      var component = components[index];
      var value = component.writeToNbt();
      if (value == null) continue;
      var id = ComponentTypes.getId(component.getClass());
      if (id == null)
        throw new IllegalStateException(
            "Unregistered native component: " + component.getClass().getName());
      if (saved == null) saved = new CompoundTag();
      saved.put(id, value);
    }
    if (saved != null) data.put("components", saved);
  }

  public final boolean canTick() {
    return enableWorldTick || tickingComponents != null && !tickingComponents.isEmpty();
  }

  public final void tick() {
    if (loadState != 2 || isRemoved()) return;
    if (tickingComponents != null)
      for (int index = 0; index < tickingComponents.size() && loadState == 2; index++)
        tickingComponents.get(index).onWorldTick();
    if (!enableWorldTick || loadState != 2) return;
    if (getLevel().isClientSide()) updateEntityClient();
    else updateEntityServer();
  }

  protected void updateEntityClient() {}

  protected void updateEntityServer() {}

  @Override
  public List<String> getNetworkedFields() {
    if (networkIdentity == null) {
      var key = BuiltInRegistries.BLOCK.getKey(teBlock);
      if (key != null) networkIdentity = "teBlk=" + key;
    }
    var fields = new ArrayList<String>(3);
    fields.add(networkIdentity == null ? "teBlk=null" : networkIdentity);
    fields.add("active");
    return fields;
  }

  @Override
  public void onNetworkUpdate(String field) {
    if (getLevel() != null
        && getLevel().isClientSide()
        && (field.equals("facing") || field.equals("active") && teBlock.canActive())) rerender();
  }

  protected final void rerender() {
    var world = getLevel();
    if (world == null || world.getBlockState(worldPosition).getBlock() != teBlock) return;
    var state = getBlockState();
    world.sendBlockUpdated(worldPosition, state, state, 2);
    if (teBlock.canActive() && state.hasProperty(MachineBlockType.ACTIVE))
      world.setBlockAndUpdate(worldPosition, state.setValue(MachineBlockType.ACTIVE, active));
  }

  public void onPlaced(ItemStack stack, LivingEntity placer, Direction direction) {}

  protected VoxelShape getOutlineShape(CollisionContext context) {
    return shape(false);
  }

  protected VoxelShape getCollisionShape(CollisionContext context) {
    return shape(true);
  }

  protected VoxelShape getCullingShape() {
    return shape(false);
  }

  private VoxelShape shape(boolean collision) {
    var boxes = getAabbs(collision);
    if (boxes == FULL_CUBE) return Shapes.block();
    var shape = Shapes.empty();
    for (int index = 0; index < boxes.size(); index++)
      shape = Shapes.or(shape, Shapes.create(boxes.get(index)));
    return shape;
  }

  protected List<AABB> getAabbs(boolean collision) {
    return FULL_CUBE;
  }

  protected boolean isNormalCube() {
    var boxes = getAabbs(false);
    if (boxes.size() != 1) return false;
    var box = boxes.getFirst();
    return box.minX <= 0
        && box.minY <= 0
        && box.minZ <= 0
        && box.maxX >= 1
        && box.maxY >= 1
        && box.maxZ >= 1;
  }

  protected int getLightOpacity() {
    return isNormalCube() ? lightOpacityOpaque : lightOpacityTranslucent;
  }

  protected int getLightValue() {
    return 0;
  }

  protected void onEntityCollision(Entity entity) {}

  protected InteractionResult onActivated(
      Player player, InteractionHand hand, Direction face, Vec3 hit) {
    if (!(this instanceof IHasGui screen) || getLevel() == null) return InteractionResult.PASS;
    return getLevel().isClientSide()
        ? InteractionResult.SUCCESS
        : screen.openManagedBe(player, hand) ? InteractionResult.CONSUME : InteractionResult.PASS;
  }

  protected InteractionResult onClicked(Player player) {
    return InteractionResult.PASS;
  }

  protected void onNeighborChange(Block changed) {
    if (components != null)
      for (int index = 0; index < componentCount; index++)
        components[index].onNeighborChange(changed);
  }

  @Override
  public void preRemoveSideEffects(BlockPos position, BlockState state) {
    if (removalDropsEmitted
        || !(getLevel() instanceof net.minecraft.server.level.ServerLevel server)) return;
    removalDropsEmitted = true;
    // The native Container default would spill retained box contents in addition to their block
    // item.
    for (var stack : getAuxDrops(0))
      if (stack != null && !stack.isEmpty()) Block.popResource(server, position, stack);
  }

  protected boolean recolor(Direction face, DyeColor color) {
    return false;
  }

  protected void onExploded(Explosion explosion) {}

  protected void onBlockBreak() {}

  protected boolean onRemovedByPlayer(Player player, boolean harvest) {
    return true;
  }

  protected ItemStack getPickBlock(Player player, BlockHitResult hit) {
    return new ItemStack(teBlock);
  }

  protected List<ItemStack> getSelfDrops(int fortune, boolean wrench) {
    var drop = adjustDrop(getPickBlock(null, null), wrench);
    return drop == null || drop.isEmpty() ? List.of() : List.of(drop);
  }

  protected List<ItemStack> getAuxDrops(int fortune) {
    return List.of();
  }

  protected boolean canEntityDestroy(Entity entity) {
    return true;
  }

  public ItemStack adjustDrop(ItemStack original, boolean wrench) {
    if (wrench) return original;
    return switch (teBlock.getDefaultDrop()) {
      case Self -> original;
      case None -> null;
      case Generator -> new ItemStack(PowerItems.GENERATOR);
      case Machine -> new ItemStack(WorldItems.MACHINE);
      case AdvMachine -> new ItemStack(WorldItems.ADVANCED_MACHINE);
    };
  }

  public Direction getFacing() {
    return teBlock.facingProperty == null
        ? Direction.NORTH
        : getBlockState().getValue(teBlock.facingProperty);
  }

  protected Set<Direction> getSupportedFacings() {
    return teBlock.getSupportedFacings();
  }

  protected Direction getPlacementFacing(LivingEntity placer, Direction requested) {
    return requested;
  }

  protected boolean canSetFacingWrench(Direction face, Player player) {
    return face != null
        && face != getFacing()
        && teBlock.allowWrenchRotating()
        && getSupportedFacings().contains(face);
  }

  protected boolean setFacingWrench(Level world, Direction face, Player player) {
    if (!canSetFacingWrench(face, player)) return false;
    setFacing(world, face);
    return true;
  }

  protected boolean wrenchCanRemove(Player player) {
    return true;
  }

  protected void setFacing(Level world, Direction face) {
    Objects.requireNonNull(face);
    if (face == getFacing() || !getSupportedFacings().contains(face))
      throw new IllegalArgumentException("Invalid native block rotation: " + face);
    world.setBlockAndUpdate(
        worldPosition, world.getBlockState(worldPosition).setValue(teBlock.facingProperty, face));
  }

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean value) {
    if (teBlock.canActive() && active != value) {
      active = value;
      if (getLevel() != null && !getLevel().isClientSide())
        SideServices.network(true).updateTileEntityField(this, "active");
    }
  }

  @Override
  public boolean getGuiState(String key) {
    if (key.equals("active")) return active;
    throw new IllegalArgumentException("Unknown native block condition: " + key);
  }

  public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag flags) {
    var energy = getComponent(ElectricalBuffer.class);
    if (energy == null) return;
    if (!energy.getSourceDirs().isEmpty())
      tooltip.add(LocalizedText.translate("ic2.item.tooltip.PowerTier", energy.getSourceTier()));
    else if (!energy.getSinkDirs().isEmpty())
      tooltip.add(LocalizedText.translate("ic2.item.tooltip.PowerTier", energy.getSinkTier()));
  }

  protected final <T extends NativeComponent> T addComponent(T component) {
    Objects.requireNonNull(component);
    if (component.getParent() != this)
      throw new IllegalArgumentException("Component belongs to another native block");
    if (hasComponent(component.getClass()))
      throw new IllegalArgumentException(
          "Native block already owns component " + component.getClass().getName());
    if (components == null) components = new NativeComponent[4];
    else if (componentCount == components.length)
      components = Arrays.copyOf(components, componentCount + 4);
    components[componentCount++] = component;
    if (loadState == 2 && !initializingComponents) loadComponent(component);
    return component;
  }

  public boolean hasComponent(Class<? extends NativeComponent> type) {
    return getComponent(type) != null;
  }

  public <T extends NativeComponent> T getComponent(Class<T> type) {
    if (components != null)
      for (int index = 0; index < componentCount; index++) {
        var component = components[index];
        if (component.getClass() == type) return type.cast(component);
      }
    return null;
  }

  public final Iterable<? extends NativeComponent> getComponents() {
    if (components == null) return List.of();
    if (componentView == null)
      componentView =
          new AbstractList<>() {
            @Override
            public int size() {
              return componentCount;
            }

            @Override
            public NativeComponent get(int index) {
              Objects.checkIndex(index, componentCount);
              return components[index];
            }
          };
    return componentView;
  }
}
