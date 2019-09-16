package org.fissore.jsonpatchoptimizer;

import org.fissore.steroids.SMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReplaceOptimizerTest {

  private ReplaceOptimizer replaceOptimizer;
  private SMap optimizedPatches;

  @Before
  public void setUp() {
    replaceOptimizer = new ReplaceOptimizer();
    optimizedPatches = new SMap();
  }

  @Test
  public void shouldMarkFieldAsReplaced() {
    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something"));

    assertEquals("replace", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
  }

  @Test
  public void shouldUseNewValueWhenReplacingAddedOrReplacedField() {
    optimizedPatches.add("/bar", new SMap("op", "add", "value", "something"));

    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something new"));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something new", optimizedPatches.map("/bar").s("value"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "replace", "value", "something"));

    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something new"));

    assertEquals("replace", optimizedPatches.map("/bar").s("op"));
    assertEquals("something new", optimizedPatches.map("/bar").s("value"));
  }

  @Test
  public void shouldReplaceFieldMarkedAsRemove() {
    optimizedPatches.add("/bar", new SMap("op", "remove"));

    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something"));

    assertEquals("replace", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
  }

  @Test
  public void shouldAddFieldMarkAsCopiedAndThenReplaced() {
    optimizedPatches.add("/bar", new SMap("op", "copy", "from", "/foo"));

    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something"));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
  }

  @Test
  public void shouldRemoveAndAddPreviouslyMovedAndThenReplacedField() {
    optimizedPatches.add("/bar", new SMap("op", "move", "from", "/foo"));

    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something"));

    assertEquals("remove", optimizedPatches.map("/foo").s("op"));
    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
  }
}
