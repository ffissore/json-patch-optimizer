package org.fissore.jsonpatchoptimizer;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fissore.steroids.SMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

/**
 * It optimizes a JSON patch, provided as an instance of {@link ArrayNode}.<br>
 * It discards "test" operations, which may be generated passing a {@code true} to {@link #optimize(ArrayNode, boolean)}<br>
 * <br>
 * List of optimizations
 * <table>
 *   <caption></caption>
 *   <tr>
 *     <th>Previous operation</th>
 *     <th>Current operation</th>
 *     <th>Optimize to</th>
 *   </tr>
 *   <tr>
 *     <td>none</td>
 *     <td>add</td>
 *     <td>no changes</td>
 *   </tr>
 *   <tr>
 *     <td>add</td>
 *     <td>add</td>
 *     <td>add new value</td>
 *   </tr>
 *   <tr>
 *     <td>add</td>
 *     <td>remove</td>
 *     <td>delete both</td>
 *   </tr>
 *   <tr>
 *     <td>add</td>
 *     <td>replace</td>
 *     <td>add replaced value</td>
 *   </tr>
 *   <tr>
 *     <td>add</td>
 *     <td>copy</td>
 *     <td>copy</td>
 *   </tr>
 *   <tr>
 *     <td>add</td>
 *     <td>move</td>
 *     <td>move</td>
 *   </tr>
 *   <tr>
 *     <td>none</td>
 *     <td>remove</td>
 *     <td>no changes</td>
 *   </tr>
 *   <tr>
 *     <td>remove</td>
 *     <td>add</td>
 *     <td>replace</td>
 *   </tr>
 *   <tr>
 *     <td>remove</td>
 *     <td>remove</td>
 *     <td>ignore</td>
 *   </tr>
 *   <tr>
 *     <td>remove</td>
 *     <td>replace</td>
 *     <td>replace</td>
 *   </tr>
 *   <tr>
 *     <td>remove</td>
 *     <td>copy</td>
 *     <td>copy</td>
 *   </tr>
 *   <tr>
 *     <td>remove</td>
 *     <td>move</td>
 *     <td>move</td>
 *   </tr>
 *   <tr>
 *     <td>none</td>
 *     <td>replace</td>
 *     <td>replace</td>
 *   </tr>
 *   <tr>
 *     <td>replace</td>
 *     <td>add</td>
 *     <td>error</td>
 *   </tr>
 *   <tr>
 *     <td>replace</td>
 *     <td>remove</td>
 *     <td>remove</td>
 *   </tr>
 *   <tr>
 *     <td>replace</td>
 *     <td>replace</td>
 *     <td>replace new value</td>
 *   </tr>
 *   <tr>
 *     <td>replace</td>
 *     <td>copy</td>
 *     <td>copy</td>
 *   </tr>
 *   <tr>
 *     <td>replace</td>
 *     <td>move</td>
 *     <td>move</td>
 *   </tr>
 *   <tr>
 *     <td>none</td>
 *     <td>copy</td>
 *     <td>copy</td>
 *   </tr>
 *   <tr>
 *     <td>copy</td>
 *     <td>add</td>
 *     <td>add</td>
 *   </tr>
 *   <tr>
 *     <td>copy</td>
 *     <td>remove</td>
 *     <td>delete both</td>
 *   </tr>
 *   <tr>
 *     <td>copy</td>
 *     <td>replace</td>
 *     <td>add</td>
 *   </tr>
 *   <tr>
 *     <td>copy</td>
 *     <td>copy</td>
 *     <td>update 'from'</td>
 *   </tr>
 *   <tr>
 *     <td>copy</td>
 *     <td>move</td>
 *     <td>move</td>
 *   </tr>
 *   <tr>
 *     <td>none</td>
 *     <td>move</td>
 *     <td>move</td>
 *   </tr>
 *   <tr>
 *     <td>move</td>
 *     <td>add</td>
 *     <td>add</td>
 *   </tr>
 *   <tr>
 *     <td>move</td>
 *     <td>remove</td>
 *     <td>delete 'from'</td>
 *   </tr>
 *   <tr>
 *     <td>move</td>
 *     <td>replace</td>
 *     <td>delete 'from', add new value</td>
 *   </tr>
 *   <tr>
 *     <td>move</td>
 *     <td>copy</td>
 *     <td>copy</td>
 *   </tr>
 *   <tr>
 *     <td>move</td>
 *     <td>move</td>
 *     <td>merge in one move</td>
 *   </tr>
 * </table>
 */
public class JsonPatchOptimizer {

  private final Map<String, Optimizer> optimizers;

  public JsonPatchOptimizer() {
    this.optimizers = new HashMap<>();
    this.optimizers.put("add", new AddOptimizer());
    this.optimizers.put("remove", new RemoveOptimizer());
    this.optimizers.put("replace", new ReplaceOptimizer());
    this.optimizers.put("copy", new CopyOptimizer());
    this.optimizers.put("move", new MoveOptimizer());
  }

  /**
   * Optimizes the given {@link ArrayNode} of patches
   *
   * @param patches  the list of patches to optimize
   * @param addTests true to add "test" operations below all optimized operation with a "value"
   * @return an {@link ArrayNode} of optimized patches
   */
  public ArrayNode optimize(ArrayNode patches, boolean addTests) {
    SMap optimizedPatches = new SMap();

    AtomicInteger index = new AtomicInteger(-1);
    StreamSupport.stream(patches.spliterator(), false)
      .filter(patch -> !"test".equals(patch.get("op").asText()))
      .map(patch -> {
        SMap newPatch = new SMap()
          .add("path", patch.get("path").asText())
          .add("op", patch.get("op").asText())
          .add("value", patch.get("value"))
          .add("index", index.incrementAndGet());
        if (patch.has("from")) {
          newPatch.add("from", patch.get("from").asText());
        }

        return newPatch;
      })
      .forEach(patch -> optimizers.get(patch.s("op")).optimize(optimizedPatches, patch));

    List<SMap> unsortedOptimizedPatches = new ArrayList<>();
    optimizedPatches.forEach((key, v) -> {
      SMap patch = (SMap) v;
      patch.add("path", key);
      unsortedOptimizedPatches.add(patch);
    });

    ArrayNode resultingOptimizedPatches = patches.arrayNode();
    unsortedOptimizedPatches.stream()
      .sorted(Comparator.comparingInt(o -> o.i("index")))
      .forEach(patch -> {
        boolean addTestOp = false;

        ObjectNode optimizedPatch = resultingOptimizedPatches.objectNode();
        resultingOptimizedPatches.add(optimizedPatch);
        optimizedPatch.put("path", patch.s("path"));
        optimizedPatch.put("op", patch.s("op"));
        if (patch.valued("value")) {
          optimizedPatch.set("value", patch.o("value"));
          addTestOp = addTests;
        }
        if (patch.valued("from")) {
          optimizedPatch.put("from", patch.s("from"));
        }

        if (addTestOp) {
          ObjectNode testOp = resultingOptimizedPatches.objectNode();
          resultingOptimizedPatches.add(testOp);
          testOp.put("path", patch.s("path"));
          testOp.put("op", "test");
          testOp.set("value", patch.o("value"));
        }
      });

    return resultingOptimizedPatches;
  }

}
