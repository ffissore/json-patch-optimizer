package org.fissore.jsonpatchoptimizer;

import org.fissore.steroids.SMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CopyOptimizerTest {

  private CopyOptimizer copyOptimizer;
  private SMap optimizedPatches;

  @Before
  public void setUp() {
    copyOptimizer = new CopyOptimizer();
    optimizedPatches = new SMap();
  }

  @Test
  public void shouldMarkFieldAsCopied() {
    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/bar", "from", "/foo"));

    assertEquals("copy", optimizedPatches.map("/bar").s("op"));
    assertEquals("/foo", optimizedPatches.map("/bar").s("from"));
  }

  @Test
  public void shouldUpdateOriginFieldOfExistingCopy() {
    optimizedPatches.add("/bar", new SMap("op", "copy", "from", "/foo"));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/bar", "from", "/baz"));

    assertEquals("copy", optimizedPatches.map("/bar").s("op"));
    assertEquals("/baz", optimizedPatches.map("/bar").s("from"));
  }

  @Test
  public void shouldOverwriteAddRemoveAndMoveWhenCopy() {
    optimizedPatches.add("/bar", new SMap("op", "add", "value", "something"));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/bar", "from", "/baz"));

    assertEquals("copy", optimizedPatches.map("/bar").s("op"));
    assertEquals("/baz", optimizedPatches.map("/bar").s("from"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "remove"));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/bar", "from", "/baz"));

    assertEquals("copy", optimizedPatches.map("/bar").s("op"));
    assertEquals("/baz", optimizedPatches.map("/bar").s("from"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "move", "from", "/foo"));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/bar", "from", "/baz"));

    assertEquals("copy", optimizedPatches.map("/bar").s("op"));
    assertEquals("/baz", optimizedPatches.map("/bar").s("from"));
  }

  @Test
  public void shouldUseValueOfAddOrReplaceFromOrigin() {
    optimizedPatches.add("/bar", new SMap("op", "add", "value", "something"));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/foo", "from", "/bar"));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals("add", optimizedPatches.map("/foo").s("op"));
    assertEquals("something", optimizedPatches.map("/foo").s("value"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "replace", "value", "something"));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/foo", "from", "/bar"));

    assertEquals("replace", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals("add", optimizedPatches.map("/foo").s("op"));
    assertEquals("something", optimizedPatches.map("/foo").s("value"));
  }
}
