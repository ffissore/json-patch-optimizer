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
    removeOptimizer.optimize(optimizedPatches, new SMap("op", "remove", "path", "/bar").add("index", 1));

    assertEquals("remove", optimizedPatches.map("/bar").s("op"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldClearAddedOrCopiedField() {
    optimizedPatches.add("/bar", new SMap("op", "add", "value", "something", "index", 1));

    removeOptimizer.optimize(optimizedPatches, new SMap("op", "remove", "path", "/bar", "index", 2));

    assertTrue(optimizedPatches.isEmpty());

    optimizedPatches.add("/bar", new SMap("op", "copy", "value", "something").add("index", 1));

    removeOptimizer.optimize(optimizedPatches, new SMap("op", "remove", "path", "/bar", "index", 2));

    assertTrue(optimizedPatches.isEmpty());
  }

  @Test
  public void shouldRemoveFieldMarkedAsMovedOrReplaced() {
    optimizedPatches.add("/bar", new SMap("op", "move", "from", "/foo", "index", 1));

    removeOptimizer.optimize(optimizedPatches, new SMap("op", "remove", "path", "/bar", "index", 2));

    assertEquals("remove", optimizedPatches.map("/foo").s("op"));
    assertEquals(1, optimizedPatches.map("/foo").i("index"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "replace", "value", "something", "index", 1));

    removeOptimizer.optimize(optimizedPatches, new SMap("op", "remove", "path", "/bar", "index", 2));

    assertEquals("remove", optimizedPatches.map("/bar").s("op"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldIgnoreFieldAlreadyMarkedAsRemoved() {
    optimizedPatches.add("/bar", new SMap("op", "remove", "index", 1));

    removeOptimizer.optimize(optimizedPatches, new SMap("op", "remove", "path", "/bar", "index", 2));

    assertEquals("remove", optimizedPatches.map("/bar").s("op"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
  }
}
