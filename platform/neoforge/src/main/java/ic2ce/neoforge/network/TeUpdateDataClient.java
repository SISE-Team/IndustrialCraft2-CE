package ic2ce.neoforge.network;

import ic2ce.neoforge.block.MachineBlockType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

/** Parsed updates retain values, not a live network cursor or mutable reflective metadata. */
final class TeUpdateDataClient {
  private final List<TeData> updates = new ArrayList<>();

  TeData addTe(BlockPos position, int fields) {
    var update = new TeData(position, fields);
    updates.add(update);
    return update;
  }

  List<TeData> getTes() {
    return updates;
  }

  record FieldData(String name, Object value) {}

  static final class TeData {
    final BlockPos pos;
    private final List<FieldData> fields;
    MachineBlockType teType;

    TeData(BlockPos position, int count) {
      pos = position;
      fields = new ArrayList<>(count);
    }

    void addField(String name, Object value) {
      fields.add(new FieldData(name, value));
    }

    List<FieldData> getFields() {
      return fields;
    }

    boolean hasClass() {
      return teType != null;
    }
  }
}
