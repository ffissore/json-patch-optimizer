package org.fissore.jsonpatchoptimizer;

import org.fissore.steroids.SMap;

class RemoveOptimizer implements Optimizer {

  public void optimize(SMap optimizedPatches, SMap patch) {
    String path = patch.s("path");

    if (optimizedPatches.notValued(path)) {
      optimizedPatches.add(path, patch.subMap("op"));
      return;
    }

    SMap previous = optimizedPatches.map(path);
    String previousOp = previous.s("op");

    if ("add".equals(previousOp) || "copy".equals(previousOp)) {
      optimizedPatches.del(path);
      return;
    }

    if ("remove".equals(previousOp)) {
      return;
    }

    if ("replace".equals(previousOp)) {
      optimizedPatches.add(path, patch.subMap("op"));
      return;
    }

    if ("move".equals(previousOp)) {
      optimizedPatches
        .del(path)
        .add(previous.s("from"), new SMap("op", "remove"));
      return;
    }

    throw new IllegalStateException("Attempted to remove when previous state is: " + previous);
  }
}
