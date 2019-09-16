package org.fissore.jsonpatchoptimizer;

import org.fissore.steroids.SMap;

class CopyOptimizer implements Optimizer {

  @Override
  public void optimize(SMap optimizedPatches, SMap patch) {
    String path = patch.s("path");

    if (optimizedPatches.notValued(path)) {
      if (optimizedPatches.notValued(patch.s("from"))) {
        optimizedPatches.add(path, patch.subMap("op", "from", "index"));
        return;
      }

      SMap previous = optimizedPatches.map(patch.s("from"));
      String previousOp = previous.s("op");

      if ("add".equals(previousOp) || "replace".equals(previousOp)) {
        optimizedPatches.add(path, new SMap("op", "add", "value", previous.o("value"), "index", patch.i("index")));
        return;
      }

      optimizedPatches.add(path, patch.subMap("op", "from", "index"));
      return;
    }

    SMap previous = optimizedPatches.map(path);
    String previousOp = previous.s("op");

    if ("copy".equals(previousOp)) {
      previous.add("from", patch.s("from"));
      return;
    }

    if ("add".equals(previousOp) || "remove".equals(previousOp) || "move".equals(previousOp)) {
      optimizedPatches.add(path, patch.subMap("op", "from", "index"));
      return;
    }

    throw new IllegalStateException("Attempted to copy when previous state is: " + previous);
  }
}
