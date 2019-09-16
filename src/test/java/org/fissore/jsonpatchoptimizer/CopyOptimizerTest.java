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
    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/bar", "from", "/foo").add("index", 1));

    assertEquals("copy", optimizedPatches.map("/bar").s("op"));
    assertEquals("/foo", optimizedPatches.map("/bar").s("from"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldUpdateOriginFieldOfExistingCopy() {
    optimizedPatches.add("/bar", new SMap("op", "copy", "from", "/foo", "index", 1));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/bar", "from", "/baz").add("index", 2));

    assertEquals("copy", optimizedPatches.map("/bar").s("op"));
    assertEquals("/baz", optimizedPatches.map("/bar").s("from"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldOverwriteAddRemoveAndMoveWhenCopy() {
    optimizedPatches.add("/bar", new SMap("op", "add", "value", "something", "index", 1));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/bar", "from", "/baz").add("index", 2));

    assertEquals("copy", optimizedPatches.map("/bar").s("op"));
    assertEquals("/baz", optimizedPatches.map("/bar").s("from"));
    assertEquals(2, optimizedPatches.map("/bar").i("index"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "remove", "index", 1));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/bar", "from", "/baz").add("index", 2));

    assertEquals("copy", optimizedPatches.map("/bar").s("op"));
    assertEquals("/baz", optimizedPatches.map("/bar").s("from"));
    assertEquals(2, optimizedPatches.map("/bar").i("index"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "move", "from", "/foo", "index", 1));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/bar", "from", "/baz").add("index", 2));

    assertEquals("copy", optimizedPatches.map("/bar").s("op"));
    assertEquals("/baz", optimizedPatches.map("/bar").s("from"));
    assertEquals(2, optimizedPatches.map("/bar").i("index"));
  }

  @Test
  public void shouldUseValueOfAddOrReplaceFromOrigin() {
    optimizedPatches.add("/bar", new SMap("op", "add", "value", "something", "index", 1));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/foo", "from", "/bar").add("index", 2));

    assertEquals("add", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
    assertEquals("add", optimizedPatches.map("/foo").s("op"));
    assertEquals("something", optimizedPatches.map("/foo").s("value"));
    assertEquals(2, optimizedPatches.map("/foo").i("index"));

    optimizedPatches.clear();

    optimizedPatches.add("/bar", new SMap("op", "replace", "value", "something", "index", 1));

    copyOptimizer.optimize(optimizedPatches, new SMap("op", "copy", "path", "/foo", "from", "/bar").add("index", 2));

    assertEquals("replace", optimizedPatches.map("/bar").s("op"));
    assertEquals("something", optimizedPatches.map("/bar").s("value"));
    assertEquals(1, optimizedPatches.map("/bar").i("index"));
    assertEquals("add", optimizedPatches.map("/foo").s("op"));
    assertEquals("something", optimizedPatches.map("/foo").s("value"));
    assertEquals(2, optimizedPatches.map("/foo").i("index"));
  }
}
