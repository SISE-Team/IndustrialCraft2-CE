# Redstone links during chunk unload

Version 1.0.2 fixes a server shutdown deadlock observed with C2ME in a world containing linked reactor chambers. `SignalInput.onUnloaded()` used the ordinary unlink operation, which immediately updated its receiver. That update sampled neighboring blocks and could synchronously request another chunk while C2ME was already unloading it.

Unloading now detaches both directions without sampling the world. A receiver that remains loaded gets one deferred refresh on the next world tick. Removed or unloading receivers discard the callback. Ordinary explicit unlink operations still refresh immediately, and loading a component enables normal signal sampling again.

The source change is in IndustrialCraft2-CE itself. No Compat patch or C2ME modification is required. Native signal contracts cover link propagation, immediate unlink, deferred unload refresh, an unloading receiver, and component reload. Full-pack validation uses an untouched copy of the reported world with both this change and the Mekanism heat persistence fix, including two complete save and reload cycles. Evidence is retained in the instance `.work/verification/mekanism-heat-20261017/` directory.
