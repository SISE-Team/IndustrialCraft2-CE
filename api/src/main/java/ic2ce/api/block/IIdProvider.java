package ic2ce.api.block;

public interface IIdProvider {
   String getName();

   int getId();

   default int getColor() {
      return 16777215;
   }

   default String getModelName() {
      return this.getName();
   }
}
