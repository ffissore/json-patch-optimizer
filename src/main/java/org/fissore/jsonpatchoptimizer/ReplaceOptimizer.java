package org.fissore.jsonpatchoptimizer;

import org.fissore.steroids.SMap;

class ReplaceOptimizer implements Optimizer {

  @Override
  public void optimize(SMap optimizedPatches, SMap patch) {
    String path = patch.s("path");

    if (optimizedPatches.notValued(path)) {
      optimizedPatches.add(path, patch.subMap("op", "value", "index"));
      return;
    }

    SMap previous = optimizedPatches.map(path);
    String previousOp = previous.s("op");

    if ("add".equals(previousOp) || "replace".equals(previousOp)) {
      previous.add("value", patch.o("value"));
      return;
    }

    if ("remove".equals(previousOp)) {
      optimizedPatches.add(path, patch.subMap("op", "value", "index"));
      return;
    }

    if ("copy".equals(previousOp)) {
      optimizedPatches.add(path, new SMap("op", "add", "value", patch.o("value"), "index", previous.i("index")));
      return;
    }

    if ("move".equals(previousOp)) {
      optimizedPatches
        .add(previous.s("from"), new SMap("op", "remove", "index", previous.i("index")))
        .add(path, new SMap("op", "add", "value", patch.o("value"), "index", patch.i("index")));
      return;
    }

    throw new IllegalStateException("Attempted to replace when previous state is: " + previous);
  }
}
