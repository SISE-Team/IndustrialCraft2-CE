package ic2ce.neoforge;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import ic2ce.api.energy.ProfileEvent;
import ic2ce.api.event.ExplosionEvent;
import ic2ce.api.event.RetextureEvent;
import ic2ce.neoforge.fluid.ClientEnvFluidHandlerNeoForge;
import ic2ce.neoforge.fluid.EnvFluidHandler;
import ic2ce.neoforge.inventory.transfer.EnvItemHandler;
import ic2ce.neoforge.network.GrowingBuffer;
import ic2ce.neoforge.runtime.EnvProxy;
import ic2ce.neoforge.fluid.EnvFluidHandlerNeoForge;
import ic2ce.neoforge.inventory.transfer.NativeItemTransfers;
import ic2ce.neoforge.registry.RegistryPlan;
import ic2ce.neoforge.runtime.ProjectRuntime;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** Native NeoForge registration, transfer, menu and gameplay services. */
public final class EnvProxyNeoForge implements EnvProxy {
  static final RegistryPlan<Block> BLOCKS = new RegistryPlan<>(Registries.BLOCK);
  static final RegistryPlan<Item> ITEMS = new RegistryPlan<>(Registries.ITEM);
  static final RegistryPlan<BlockEntityType<?>> BLOCK_ENTITIES =
      new RegistryPlan<>(Registries.BLOCK_ENTITY_TYPE);
  static final RegistryPlan<MenuType<?>> MENUS = new RegistryPlan<>(Registries.MENU);
  static final RegistryPlan<EntityType<?>> ENTITIES = new RegistryPlan<>(Registries.ENTITY_TYPE);
  static final RegistryPlan<MobEffect> EFFECTS = new RegistryPlan<>(Registries.MOB_EFFECT);
  static final RegistryPlan<CreativeModeTab> TABS =
      new RegistryPlan<>(Registries.CREATIVE_MODE_TAB);
  static final RegistryPlan<FoliagePlacerType<?>> FOLIAGE =
      new RegistryPlan<>(Registries.FOLIAGE_PLACER_TYPE);
  static final RegistryPlan<RecipeType<?>> RECIPE_TYPES =
      new RegistryPlan<>(Registries.RECIPE_TYPE);
  static final RegistryPlan<RecipeSerializer<?>> RECIPE_SERIALIZERS =
      new RegistryPlan<>(Registries.RECIPE_SERIALIZER);
  static final RegistryPlan<SoundEvent> SOUNDS = new RegistryPlan<>(Registries.SOUND_EVENT);
  static final RegistryPlan<GameEvent> GAME_EVENTS = new RegistryPlan<>(Registries.GAME_EVENT);
  static final RegistryPlan<PlacementModifierType<?>> PLACEMENTS =
      new RegistryPlan<>(Registries.PLACEMENT_MODIFIER_TYPE);
  static final RegistryPlan<TicketType> TICKETS = new RegistryPlan<>(Registries.TICKET_TYPE);
  static final RegistryPlan<
          com.mojang.serialization.MapCodec<
              ? extends net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider>>
      LOOT_PROVIDERS = new RegistryPlan<>(Registries.LOOT_NBT_PROVIDER_TYPE);
  private static final TicketType CHUNK_TICKET =
      TICKETS.stage(
          Identifier.fromNamespaceAndPath("ic2", "chunk_loader"),
          new TicketType(
              TicketType.NO_TIMEOUT,
              TicketType.FLAG_PERSIST
                  | TicketType.FLAG_LOADING
                  | TicketType.FLAG_SIMULATION
                  | TicketType.FLAG_KEEP_DIMENSION_ACTIVE));
  public static final Supplier<TicketType> CHUNK_LOADER_TICKET_TYPE = () -> CHUNK_TICKET;
  static final Map<Item, Integer> burnTimeRecord =
      Collections.synchronizedMap(new IdentityHashMap<>());
  private static final Map<Block, FireProperties> FIRE =
      Collections.synchronizedMap(new IdentityHashMap<>());

  private record FireProperties(int flammability, int spread) {}

  static void replay(RegisterEvent event) {
    for (var plan :
        List.of(
            BLOCKS,
            ITEMS,
            BLOCK_ENTITIES,
            MENUS,
            ENTITIES,
            EFFECTS,
            TABS,
            FOLIAGE,
            RECIPE_TYPES,
            RECIPE_SERIALIZERS,
            SOUNDS,
            GAME_EVENTS,
            PLACEMENTS,
            TICKETS,
            LOOT_PROVIDERS)) plan.replay(event);
  }

  public static <T extends net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider>
      com.mojang.serialization.MapCodec<T> registerLootProvider(
          Identifier identifier, com.mojang.serialization.MapCodec<T> codec) {
    return LOOT_PROVIDERS.stage(identifier, codec);
  }

  public static int flammability(BlockState state) {
    var fire = FIRE.get(state.getBlock());
    return fire == null || waterlogged(state) ? 0 : fire.flammability();
  }

  public static int fireSpread(BlockState state) {
    var fire = FIRE.get(state.getBlock());
    return fire == null || waterlogged(state) ? 0 : fire.spread();
  }

  private static boolean waterlogged(BlockState state) {
    var property =
        net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
    return state.hasProperty(property) && state.getValue(property);
  }

  @Override
  public boolean isClientEnv() {
    return FMLEnvironment.getDist().isClient();
  }

  @Override
  public MinecraftServer getServer() {
    return ServerLifecycleHooks.getCurrentServer();
  }

  @Override
  public HolderLookup.Provider getRegistryLookup() {
    var server = getServer();
    if (server != null) return server.registryAccess();
    return isClientEnv() ? ClientServices.registryLookup() : null;
  }

  private static final class ClientServices {
    static HolderLookup.Provider registryLookup() {
      return ic2ce.neoforge.client.SideProxyClient.envProxy.getRegistryLookup();
    }

    static EnvFluidHandler fluids() {
      return new ClientEnvFluidHandlerNeoForge();
    }
  }

  @Override
  public void registerBlock(Identifier id, Block block) {
    BLOCKS.stage(id, block);
  }

  @Override
  public void registerItem(Identifier id, Item item) {
    ITEMS.stage(id, item);
  }

  @Override
  public <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
      Identifier id, BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
    if (blocks.length == 0)
      throw new IllegalArgumentException("A block entity requires supported blocks: " + id);
    return BLOCK_ENTITIES.stage(id, new BlockEntityType<>(factory::apply, Set.of(blocks), false));
  }

  @Override
  public <T extends AbstractContainerMenu> MenuType<T> registerScreenHandler(
      Identifier id, BiFunction<Integer, Inventory, T> factory) {
    return registerExtendedScreenHandler(
        id, (window, inventory, data) -> factory.apply(window, inventory));
  }

  @Override
  public <T extends AbstractContainerMenu> MenuType<T> registerExtendedScreenHandler(
      Identifier id, ExtendedClientScreenHandlerFactory<T> factory) {
    return MENUS.stage(id, IMenuTypeExtension.create(factory::create));
  }

  @Override
  public void registerEntity(Identifier id, EntityType<?> type) {
    ENTITIES.stage(id, type);
  }

  @Override
  public WoodType registerSignType(String name) {
    return WoodType.register(
        new WoodType(
            ProjectRuntime.getIdentifier(name).toString(),
            net.minecraft.world.level.block.state.properties.BlockSetType.OAK));
  }

  @Override
  public void registerStatusEffect(Identifier id, MobEffect effect) {
    EFFECTS.stage(id, effect);
  }

  @Override
  public void registerFlammableBlock(Block block, int burn, int spread) {
    if (burn < 0 || spread < 0) throw new IllegalArgumentException("Negative fire properties");
    FIRE.put(Objects.requireNonNull(block), new FireProperties(burn, spread));
  }

  @Override
  public SoundEvent registerSoundEvent(String name) {
    var id = ProjectRuntime.getIdentifier(name);
    return SOUNDS.stage(id, SoundEvent.createVariableRangeEvent(id));
  }

  @Override
  public GameEvent registerGameEvent(String name, int range) {
    if (range <= 0) throw new IllegalArgumentException("Game event range must be positive");
    return GAME_EVENTS.stage(ProjectRuntime.getIdentifier(name), new GameEvent(range));
  }

  @Override
  public void registerPlacementModifierType(Identifier id, PlacementModifierType<?> type) {
    PLACEMENTS.stage(id, type);
  }

  @Override
  public <T extends FoliagePlacer> FoliagePlacerType<T> registerFoliagePlacer(
      Identifier id, Codec<T> codec) {
    return FOLIAGE.stage(id, new FoliagePlacerType<>(codec.fieldOf("value")));
  }

  @Override
  public <T extends Recipe<?>> RecipeType<T> registerRecipeType(Identifier id) {
    return RECIPE_TYPES.stage(id, RecipeType.simple(id));
  }

  @Override
  public void registerRecipeSerializer(Identifier id, RecipeSerializer<?> serializer) {
    RECIPE_SERIALIZERS.stage(id, serializer);
  }

  @Override
  public void runAfterRegistryInit(Runnable task) {
    IndustrialCraft2CE.instance.runAfterRegistryInit(Objects.requireNonNull(task));
  }

  @Override
  public Identifier createItemGroup(Identifier id, Supplier<ItemStack> icon) {
    TABS.stage(
        id,
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + id.getNamespace() + "." + id.getPath()))
            .icon(
                () -> {
                  var value = icon.get();
                  return value == null ? ItemStack.EMPTY : value.copy();
                })
            .displayItems(
                (parameters, output) -> ic2ce.neoforge.registry.CreativeContents.accept(id, output))
            .build());
    return id;
  }

  @Override
  public EnvFluidHandler createFluidStackHandler() {
    return isClientEnv() ? ClientServices.fluids() : new EnvFluidHandlerNeoForge();
  }

  @Override
  public EnvItemHandler createItemHandler() {
    return new NativeItemTransfers();
  }

  @Override
  public float getBlastResistance(
      BlockState state, BlockGetter world, BlockPos position, Explosion blast) {
    return state.getBlock().getExplosionResistance(state, world, position, blast);
  }

  @Override
  public BlockState rotate(
      BlockState state, LevelAccessor world, BlockPos position, Rotation rotation) {
    return state.rotate(world, position, rotation);
  }

  @Override
  public boolean hasRecipeRemainder(ItemStack stack) {
    return stack != null && !stack.isEmpty() && stack.getItem().getCraftingRemainder(stack) != null;
  }

  @Override
  public ItemStack getRecipeRemainder(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
    var remainder = stack.getItem().getCraftingRemainder(stack);
    return remainder == null ? ItemStack.EMPTY : remainder.create();
  }

  @Override
  public void registerBurnTime(ItemLike material, int ticks) {
    if (ticks < 0) throw new IllegalArgumentException("Negative burn time");
    burnTimeRecord.put(material.asItem(), ticks);
  }

  @Override
  public int getBurnTime(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return 0;
    var override = burnTimeRecord.get(stack.getItem());
    if (override != null) return override;
    var server = getServer();
    var world = server == null && isClientEnv() ? ProjectRuntime.sideProxy.getPlayerWorld() : null;
    var values = server != null ? server.fuelValues() : world != null ? world.fuelValues() : null;
    return values == null ? 0 : Math.max(0, stack.getBurnTime(RecipeType.SMELTING, values));
  }

  @Override
  public boolean biomeHasType(Holder<Biome> biome, ic2ce.api.crops.BiomeType type) {
    if (type == ic2ce.api.crops.BiomeType.WATER)
      return biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_OCEAN)
          || biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_RIVER);
    String tag =
        switch (type) {
          case DENSE -> "is_dense_vegetation";
          case SPARSE -> "is_sparse_vegetation";
          case CONIFEROUS -> "is_tree/coniferous";
          case HILLS -> "is_hill";
          case MESA -> "is_badlands";
          default -> "is_" + type.name().toLowerCase(Locale.ROOT);
        };
    return biome.is(
        net.minecraft.tags.TagKey.create(
            Registries.BIOME, Identifier.fromNamespaceAndPath("c", tag)));
  }

  @Override
  public Collection<ic2ce.api.crops.BiomeType> getBiomeTypes(Holder<Biome> biome) {
    var types = java.util.EnumSet.noneOf(ic2ce.api.crops.BiomeType.class);
    for (var type : ic2ce.api.crops.BiomeType.values())
      if (biomeHasType(biome, type)) types.add(type);
    return Collections.unmodifiableSet(types);
  }

  @Override
  public boolean openHandledScreen(Player player, MenuProvider factory, GrowingBuffer data) {
    return player instanceof ServerPlayer actor
        && factory != null
        && actor
            .openMenu(
                factory,
                buffer -> {
                  if (data != null) data.writeTo(buffer);
                })
            .isPresent();
  }

  @Override
  public boolean isFakePlayer(Player player) {
    return player instanceof FakePlayer;
  }

  @Override
  public Player createFakePlayer(ServerLevel world, GameProfile profile) {
    return FakePlayerFactory.get(world, profile);
  }

  @Override
  public void announceProfileLoad(Set<String> loaded, String active) {
    NeoForge.EVENT_BUS.post(new ProfileEvent.Load(Set.copyOf(loaded), active));
  }

  @Override
  public void announceProfileSwitch(String from, String to) {
    NeoForge.EVENT_BUS.post(new ProfileEvent.Switch(from, to));
  }

  @Override
  public boolean announceRetexture(
      Level world,
      BlockPos position,
      BlockState state,
      Direction face,
      Player actor,
      BlockState reference,
      String variant,
      Direction referenceFace,
      int[] colors) {
    var event =
        new RetextureEvent(
            world,
            position,
            state,
            face,
            actor,
            reference,
            variant,
            referenceFace,
            colors == null ? null : colors.clone());
    NeoForge.EVENT_BUS.post(event);
    return event.applied;
  }

  @Override
  public boolean announceExplosion(
      Level world,
      Entity entity,
      Vec3 position,
      double power,
      LivingEntity igniter,
      int radiationRange,
      double rangeLimit) {
    return !NeoForge.EVENT_BUS
        .post(
            new ExplosionEvent(world, entity, position, power, igniter, radiationRange, rangeLimit))
        .isCanceled();
  }
}
