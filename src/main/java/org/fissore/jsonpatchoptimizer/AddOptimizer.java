package org.fissore.jsonpatchoptimizer;

import org.fissore.steroids.SMap;

class AddOptimizer implements Optimizer {

  public void optimize(SMap optimizedPatches, SMap patch) {
    String path = patch.s("path");

    if (optimizedPatches.notValued(path)) {
      optimizedPatches.add(path, patch.subMap("op", "value"));
      return;
    }

    SMap previous = optimizedPatches.map(path);
    String previousOp = previous.s("op");

    if ("add".equals(previousOp)) {
      previous.add("value", patch.o("value"));
      return;
    }

    if ("remove".equals(previousOp)) {
      optimizedPatches.add(path, new SMap("op", "replace", "value", patch.o("value")));
      return;
    }

    if ("copy".equals(previousOp) || "move".equals(previousOp)) {
      optimizedPatches.add(path, patch.subMap("op", "value"));
      return;
    }

    throw new IllegalStateException("Attempted to add when previous state is: " + previous);
  }
}
