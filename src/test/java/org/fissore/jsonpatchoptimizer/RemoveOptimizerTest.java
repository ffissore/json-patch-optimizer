package org.fissore.jsonpatchoptimizer;

import org.fissore.steroids.SMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RemoveOptimizerTest {

  private RemoveOptimizer removeOptimizer;
  private SMap optimizedPatches;

  @Before
  public void setUp() {
    removeOptimizer = new RemoveOptimizer();
    optimizedPatches = new SMap();
  }

  @Test
  public void shouldMarkFieldRemoved() {
    removeOptimizer.optimize(optimizedPatches, new SMap("op", "remove", "path", "/bar"));

    assertEquals("remove", optimizedPatches.map("/bar").s("op"));
  }

  @Test
  public void shouldClearAddedOrCopiedField() {
    optimizedPatches.add("/bar", new SMap("op", "add", "value", "something"));

    removeOptimizer.optimize(optimizedPatches, new SMap("path", "/bar"));

    assertTrue(optimizedPatches.isEmpty());

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "copy", "value", "something"));

    removeOptimizer.optimize(optimizedPatches, new SMap("path", "/bar"));

    assertTrue(optimizedPatches.isEmpty());
  }

  @Test
  public void shouldRemoveFieldMarkedAsMovedOrReplaced() {
    optimizedPatches.add("/bar", new SMap("op", "move", "from", "/foo"));

    removeOptimizer.optimize(optimizedPatches, new SMap("path", "/bar"));

    assertEquals("remove", optimizedPatches.map("/foo").s("op"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "replace", "value", "something"));

    removeOptimizer.optimize(optimizedPatches, new SMap("op", "remove", "path", "/bar"));

    assertEquals("remove", optimizedPatches.map("/bar").s("op"));
  }

  @Test
  public void shouldIgnoreFieldAlreadyMarkedAsRemoved() {
    optimizedPatches.add("/bar", new SMap("op", "remove"));

    removeOptimizer.optimize(optimizedPatches, new SMap("op", "remove", "path", "/bar"));

    assertEquals("remove", optimizedPatches.map("/bar").s("op"));
  }
}
