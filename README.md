[![security status](https://www.meterian.com/badge/gh/ffissore/json-patch-optimizer/security)](https://www.meterian.com/report/gh/ffissore/json-patch-optimizer)
[![stability status](https://www.meterian.com/badge/gh/ffissore/json-patch-optimizer/stability)](https://www.meterian.com/report/gh/ffissore/json-patch-optimizer)

# JSON Patch Optimizer

A java library and command line program that takes a JSON patch and reduces it to a smaller form.

Example:
```json 
[
  {
    "op": "add",
    "path": "/foo",
    "value": "bar"
  },
  {
    "op": "add",
    "path": "/bar",
    "value": "baz"
  },
  {
    "op": "remove",
    "path": "/foo"
  }
]
```

becomes

```json
[
  {
    "op": "add",
    "path": "/bar",
    "value": "baz"
  }
]
```

because the third operation negates the first one.

### Usage

#### As a java library

Load the JSON patch into an `ArrayNode` instance, then pass that instance to `JsonPatchOptimizer.optimize` method. Example:

```java
File jsonPatchFile = ...
ArrayNode jsonPatch = (ArrayNode) new ObjectMapper().readTree(jsonPatchFile);
ArrayNode optimizedJsonPatch = new JsonPatchOptimizer().optimize(patch, false);

// if you want test operations added
ArrayNode optimizedJsonPatch = new JsonPatchOptimizer().optimize(patch, true);
```

#### As a command line utility

First build the project with `mvn clean package assembly:single` then use it like so:

```bash
java -jar target/json-patch-optimizer-1.0-SNAPSHOT-jar-with-dependencies.jar <PATH TO FILE> <true|false if you want test operations> 
```

#### As a command line utility from Docker

Pull the the image and run it, like so:

```bash
docker pull ffissore/json-patch-optimizer
docker run --rm -it -v <PATH TO FILE>:/file.json ffissore/json-patch-optimizer /file.json <true|false if you want test operations>
```

### Optimizations

This table shows how two subsequent operations are optimized

| Previous operation | Current operation | Optimize to                  |
|--------------------|-------------------|------------------------------|
| none               | add               | no changes                   |
| add                | add               | add new value                |
| add                | remove            | delete both                  |
| add                | replace           | add replaced value           |
| add                | copy              | copy                         |
| add                | move              | move                         |
| none               | remove            | no changes                   |
| remove             | add               | replace                      |
| remove             | remove            | ignore                       |
| remove             | replace           | replace                      |
| remove             | copy              | copy                         |
| remove             | move              | move                         |
| none               | replace           | replace                      |
| replace            | add               | error                        |
| replace            | remove            | remove                       |
| replace            | replace           | replace new value            |
| replace            | copy              | copy                         |
| replace            | move              | move                         |
| none               | copy              | copy                         |
| copy               | add               | add                          |
| copy               | remove            | delete both                  |
| copy               | replace           | add                          |
| copy               | copy              | update 'from'                |
| copy               | move              | move                         |
| none               | move              | move                         |
| move               | add               | add                          |
| move               | remove            | delete 'from'                |
| move               | replace           | delete 'from', add new value |
| move               | copy              | copy                         |
| move               | move              | merge in one move            |
