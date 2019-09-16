package org.fissore.jsonpatchoptimizer;

import org.fissore.steroids.SMap;

public interface Optimizer {

  void optimize(SMap optimizedPatches, SMap patch);

}
