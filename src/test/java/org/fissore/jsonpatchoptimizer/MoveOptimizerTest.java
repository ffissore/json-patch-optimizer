package org.fissore.jsonpatchoptimizer;

import org.fissore.steroids.SMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MoveOptimizerTest {

  private MoveOptimizer moveOptimizer;
  private SMap optimizedPatches;

  @Before
  public void setUp() {
    moveOptimizer = new MoveOptimizer();
    optimizedPatches = new SMap();
  }

  @Test
  public void shouldCollapseSequenceOfMovesIfStartingWithAnAddOrCopy() {
    optimizedPatches.add("/bar", new SMap("op", "add", "value", "something", "index", 1));

    moveOptimizer.optimize(optimizedPatches, new SMap("op", "move", "path", "/foo", "from", "/bar").add("index", 2));

    assertFalse(optimizedPatches.valued("/bar"));
    assertEquals("add", optimizedPatches.map("/foo").s("op"));
    assertEquals("something", optimizedPatches.map("/foo").s("value"));
    assertEquals(1, optimizedPatches.map("/foo").i("index"));
  }
}
