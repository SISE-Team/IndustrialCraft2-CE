package ic2ce.api.energy.tile;

public interface IMultiEnergySource extends IEnergySource {
   boolean sendMultipleEnergyPackets();

   int getMultipleEnergyPacketAmount();
}
