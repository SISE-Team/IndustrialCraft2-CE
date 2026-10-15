package ic2ce.neoforge.personal;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native owned inventory isolation, revocation and malformed removal boundaries. */
public final class PersonalContracts {
  public record Result(int checks, List<String> failures) {}

  private int checks;
  private final List<String> failures = new ArrayList<>();

  private void check(boolean condition, String label) {
    checks++;
    if (!condition) failures.add("native personal chest: " + label);
  }

  public static Result run(ServerLevel world) {
    return new PersonalContracts().exercise(world);
  }

  private Result exercise(ServerLevel world) {
    var position = new BlockPos(8720, 100, 8720);
    world.getChunkAt(position);
    world.setBlockAndUpdate(position, PersonalBlocks.PERSONAL_CHEST.defaultBlockState());
    var tile = (PrivateChest) world.getBlockEntity(position);
    var owner =
        new GameProfile(UUID.fromString("051b6536-0ddd-400b-ab4c-350976623513"), "IC2ChestOwner");
    var guest =
        new GameProfile(UUID.fromString("393a5a3a-b783-4c7e-8f4c-e1d13a9f9720"), "IC2ChestGuest");
    check(
        tile.permitsAccess(owner) && !tile.permitsAccess(guest),
        "first server access owns the chest and a different UUID is rejected");
    var view = tile.getPrivilegedInventory(owner);
    var input = new ItemStack(Items.DIAMOND, 13);
    view.setItem(0, input);
    input.setCount(1);
    check(
        view.getItem(0).getCount() == 13,
        "privileged insertion takes its own stack instead of borrowing a caller");
    var peek = view.getItem(0);
    peek.setCount(64);
    check(
        view.getItem(0).getCount() == 13,
        "external lookup cannot mutate stored items without a transaction");
    check(
        view.removeItem(0, -3).isEmpty() && view.getItem(0).getCount() == 13,
        "negative removal cannot manufacture stored diamonds");
    check(
        view.removeItem(0, 3).getCount() == 3 && view.getItem(0).getCount() == 10,
        "positive native extraction returns exactly the removed quantity");
    var denied = tile.getPrivilegedInventory(guest);
    denied.setItem(0, new ItemStack(Items.GOLD_INGOT));
    denied.clearContent();
    check(
        denied.getContainerSize() == 0
            && denied.removeItem(0, 10).isEmpty()
            && view.getItem(0).getCount() == 10,
        "unauthorized wrapper cannot mutate or drain any owned item");
    check(
        tile.getPrivilegedInventory(null).getContainerSize() == 0,
        "anonymous automation has no privileged view");
    tile.setOwner(guest);
    check(
        view.getContainerSize() == 0 && view.removeItem(0, 1).isEmpty(),
        "ownership transfer immediately revokes an earlier inventory view");
    var saved = new CompoundTag();
    tile.saveAdditional(saved);
    tile.load(saved);
    check(
        tile.getOwner().id().equals(guest.id()) && tile.contentSlot.get().getCount() == 10,
        "UUID owner and contents survive native persistence");
    tile.setOwner(null);
    tile.saveAdditional(saved);
    check(
        !saved.contains("ownerGameProfile"), "clearing owner erases stale persisted authorization");
    tile.contentSlot.clear();
    world.removeBlock(position, false);
    return new Result(checks, List.copyOf(failures));
  }
}
