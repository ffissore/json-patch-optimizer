package org.fissore.jsonpatchoptimizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class JsonPatchOptimizerTest {

  private JsonPatchOptimizer jsonPatchOptimizer;

  @Before
  public void setUp() {
    jsonPatchOptimizer = new JsonPatchOptimizer();
  }

  private ArrayNode readJson(String input) throws Exception {
    try (InputStream is = JsonPatchOptimizerTest.class.getResourceAsStream(input)) {
      return (ArrayNode) new ObjectMapper().readTree(is);
    }
  }

  private void assertPatchOptimized(String input, String expectedOutput, boolean addTests) throws Exception {
    ArrayNode patch = readJson(input);

    ArrayNode optimized = jsonPatchOptimizer.optimize(patch, addTests);
    ArrayNode expectedOptimized = readJson(expectedOutput);

    assertThat(optimized, equalTo(expectedOptimized));
  }

  @Test
  public void shouldOptimizeSequenceAddRemove() throws Exception {
    assertPatchOptimized("/add_remove.json", "/add_remove.optimized.json", false);
  }

  @Test
  public void shouldOptimizeSequenceCopyRemove() throws Exception {
    assertPatchOptimized("/copy_remove.json", "/copy_remove.optimized.json", false);
  }

  @Test
  public void shouldOptimizeSequenceAddCopyRemove() throws Exception {
    assertPatchOptimized("/add_copy_remove.json", "/add_copy_remove.optimized.json", false);
  }

  @Test
  public void shouldOptimizeSequenceAddMove() throws Exception {
    assertPatchOptimized("/add_move.json", "/add_move.optimized.json", false);
  }

  @Test
  public void shouldOptimizeSequenceAddMoveRemove() throws Exception {
    assertPatchOptimized("/add_move_remove.json", "/add_move_remove.optimized.json", false);
  }

  @Test
  public void shouldOptimizeSequenceAddReplace() throws Exception {
    assertPatchOptimized("/add_replace.json", "/add_replace.optimized.json", false);
  }

  @Test
  public void shouldOptimizeMoveCopyMoveSequence() throws Exception {
    assertPatchOptimized("/move_copy_move.json", "/move_copy_move.optimized.json", false);
  }

  @Test
  public void shouldOptimizeComplexSequence() throws Exception {
    assertPatchOptimized("/all_together_now.json", "/all_together_now.optimized.json", false);
  }

  @Test
  public void shouldOptimizeComplexSequenceAndAddTests() throws Exception {
    assertPatchOptimized("/all_together_now.json", "/all_together_now.with_tests.optimized.json", true);
  }

}
