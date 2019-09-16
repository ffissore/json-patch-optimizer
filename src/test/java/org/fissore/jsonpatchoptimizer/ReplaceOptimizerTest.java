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
    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something").add("index", 1));

    assertEquals("replace", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldUseNewValueWhenReplacingAddedOrReplacedField() {
    optimizedPatches.add("/bar", new SMap("op", "add", "value", "something", "index", 1));

    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something new").add("index", 2));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something new", optimizedPatches.map("/bar").s("value"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "replace", "value", "something", "index", 1));

    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something new").add("index", 2));

    assertEquals("replace", optimizedPatches.map("/bar").s("op"));
    assertEquals("something new", optimizedPatches.map("/bar").s("value"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldReplaceFieldMarkedAsRemove() {
    optimizedPatches.add("/bar", new SMap("op", "remove", "index", 1));

    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something").add("index", 2));

    assertEquals("replace", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals(2, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldAddFieldMarkAsCopiedAndThenReplaced() {
    optimizedPatches.add("/bar", new SMap("op", "copy", "from", "/foo", "index", 1));

    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something").add("index", 2));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldRemoveAndAddPreviouslyMovedAndThenReplacedField() {
    optimizedPatches.add("/bar", new SMap("op", "move", "from", "/foo", "index", 1));

    replaceOptimizer.optimize(optimizedPatches, new SMap("op", "replace", "path", "/bar", "value", "something").add("index", 2));

    assertEquals("remove", optimizedPatches.map("/foo").s("op"));
    assertEquals(1, optimizedPatches.map("/foo").i("index"));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals(2, optimizedPatches.map("/bar").i("index"));
  }
}
