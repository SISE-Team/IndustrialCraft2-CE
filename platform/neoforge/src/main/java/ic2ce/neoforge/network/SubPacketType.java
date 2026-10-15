package ic2ce.neoforge.network;

/** Stable packet identifiers with immutable permitted-direction metadata. */
public enum SubPacketType {
  Rpc(true, true),
  TileEntityEvent(true, true),
  ItemEvent(true, true),
  PlayerItemData(true, true),
  ContainerData(true, true),
  ContainerEvent(true, true),
  HandHeldInvData(true, true),
  LargePacket(true, false),
  GuiDisplay(true, false),
  ExplosionEffect(true, false),
  TileEntityBlockComponent(true, false),
  TileEntityBlockLandEffect(true, false),
  TileEntityBlockRunEffect(true, false),
  KeyUpdate(false, true),
  TileEntityData(false, true),
  RequestGUI(false, true);
  private static final SubPacketType[] INDEX = values();
  private final boolean serverToClient, clientToServer;

  SubPacketType(boolean outbound, boolean inbound) {
    serverToClient = outbound;
    clientToServer = inbound;
  }

  public int getId() {
    return ordinal() + 1;
  }

  public void writeTo(GrowingBuffer output) {
    output.writeByte(getId());
  }

  public static SubPacketType read(GrowingBuffer input, boolean server) {
    int index = input.readUnsignedByte() - 1;
    if (index < 0 || index >= INDEX.length) return null;
    var type = INDEX[index];
    return (server ? type.clientToServer : type.serverToClient) ? type : null;
  }
}
