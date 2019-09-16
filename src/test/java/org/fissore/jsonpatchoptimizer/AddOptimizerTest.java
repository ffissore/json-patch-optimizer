package org.fissore.jsonpatchoptimizer;

import org.fissore.steroids.SMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddOptimizerTest {

  private AddOptimizer addOptimizer;
  private SMap optimizedPatches;

  @Before
  public void setUp() {
    addOptimizer = new AddOptimizer();
    optimizedPatches = new SMap();
  }

  @Test
  public void shouldMarkFieldAsAdded() {
    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "value", "something"));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
  }

  @Test
  public void shouldAddAlreadyAddedFieldUsingNewValue() {
    optimizedPatches.add("/bar", new SMap("op", "add", "value", "something"));

    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "value", "something new"));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something new", optimizedPatches.map("/bar").s("value"));
  }

  @Test
  public void shouldReplaceFieldMarkedAsRemoved() {
    optimizedPatches.add("/bar", new SMap("op", "remove"));

    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "value", "something"));

    assertEquals("replace", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
  }

  @Test
  public void shouldAddFieldMarkedAsCopyOrMove() {
    optimizedPatches.add("/bar", new SMap("op", "copy", "from", "/foo"));

    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "value", "something"));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "move", "from", "/foo"));

    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "value", "something"));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailWhenAddingFieldMarkedAsReplaced() {
    optimizedPatches.add("/bar", new SMap("op", "replace", "value", "something"));

    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "something new", "something"));
  }
}
