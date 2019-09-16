package org.fissore.jsonpatchoptimizer;

import org.fissore.steroids.SMap;

class MoveOptimizer implements Optimizer {

  @Override
  public void optimize(SMap optimizedPatches, SMap patch) {
    String path = patch.s("path");
    String fromPath = patch.s("from");

    if (optimizedPatches.notValued(fromPath)) {
      optimizedPatches.add(path, patch.subMap("op", "from", "index"));
      return;
    }

    SMap previous = optimizedPatches.map(fromPath);
    String previousOp = previous.s("op");

    if ("add".equals(previousOp) || "copy".equals(previousOp)) {
      optimizedPatches
        .del(fromPath)
        .add(path, previous.add("path", path));
      return;
    }

    if ("move".equals(previousOp)) {
      optimizedPatches
        .del(fromPath)
        .add(path, previous.add("path", path));

      optimizedPatches.values().stream()
        .map(v -> (SMap) v)
        .filter(p -> "copy".equals(p.s("op")) && fromPath.equals(p.s("from")))
        .forEach(copyPatch -> copyPatch.add("from", path));

      return;
    }

    throw new IllegalStateException("Attempted to move when previous state is: " + previous);
  }
}
