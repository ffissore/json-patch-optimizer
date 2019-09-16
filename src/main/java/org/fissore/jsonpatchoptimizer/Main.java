package org.fissore.jsonpatchoptimizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Main {

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.out.println("Required argument: path to a JSON Patch file");
      System.out.println("Optional argument: true/false to trigger/not trigger 'test' operations generation");
      System.exit(1);
    }

    File patchFile = Path.of(args[0]).toFile();

    if (!patchFile.canRead()) {
      System.out.println("File " + args[0] + " does not exists or is not readable");
      System.exit(1);
    }

    boolean addTests = args.length > 1 && Boolean.parseBoolean(args[1]);

    ObjectMapper objectMapper = new ObjectMapper();

    ArrayNode patches = (ArrayNode) objectMapper.readTree(patchFile);
    ArrayNode optimizedPatches = new JsonPatchOptimizer().optimize(patches, addTests);

    objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, optimizedPatches);
  }
}
