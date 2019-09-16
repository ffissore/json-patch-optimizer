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
    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "value", "something").add("index", 1));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldAddAlreadyAddedFieldUsingNewValue() {
    optimizedPatches.add("/bar", new SMap("op", "add", "value", "something", "index", 1));

    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "value", "something new").add("index", 2));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something new", optimizedPatches.map("/bar").s("value"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldReplaceFieldMarkedAsRemoved() {
    optimizedPatches.add("/bar", new SMap("op", "remove", "index", 1));

    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "value", "something").add("index", 2));

    assertEquals("replace", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals(2, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldAddFieldMarkedAsCopyOrMove() {
    optimizedPatches.add("/bar", new SMap("op", "copy", "from", "/foo", "index", 1));

    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "value", "something").add("index", 2));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals(2, optimizedPatches.map("/bar").i("index"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "move", "from", "/foo", "index", 1));

    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "value", "something").add("index", 2));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals(2, optimizedPatches.map("/bar").i("index"));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailWhenAddingFieldMarkedAsReplaced() {
    optimizedPatches.add("/bar", new SMap("op", "replace", "value", "something", "index", 1));

    addOptimizer.optimize(optimizedPatches, new SMap("op", "add", "path", "/bar", "something new", "something").add("index", 2));
  }
}
