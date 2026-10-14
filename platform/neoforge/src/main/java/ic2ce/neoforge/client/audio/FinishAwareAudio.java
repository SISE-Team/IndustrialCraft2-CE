package ic2ce.neoforge.client.audio;

public interface FinishAwareAudio {
   void addOnFinishListener(Runnable var1);

   void onFinish(Runnable var1);

   void clearOnFinishListener();

   void finish();
}
