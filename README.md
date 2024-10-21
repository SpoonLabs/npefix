# NPEFix [![Build Status](https://travis-ci.org/Spirals-Team/npefix.svg?branch=master)](https://travis-ci.org/Spirals-Team/npefix) [![Coverage Status](https://coveralls.io/repos/github/Spirals-Team/npefix/badge.svg?branch=master)](https://coveralls.io/github/Spirals-Team/npefix?branch=master)

NPEFix is a system that automatically generates patches for NullPointerException, aka automatic bug fixing for NPE.
This code is research code, released under the GPL licence, developed at Inria Lille.

If you use this code, please cite:

Thomas Durieux, Benoit Cornu, Lionel Seinturier and Martin Monperrus, "[Dynamic Patch Generation for Null Pointer Exceptions Using Metaprogramming](https://hal.archives-ouvertes.fr/hal-01419861/document)", In IEEE International Conference on Software Analysis, Evolution and Reengineering, 2017, [doi:10.1109/SANER.2017.7884635](https://doi.org/10.1109/SANER.2017.7884635).


    @inproceedings{durieuxNpeFix,
        title = {{Dynamic Patch Generation for Null Pointer Exceptions Using Metaprogramming}},
        author = {Durieux, Thomas and Cornu, Benoit and Seinturier, Lionel and Monperrus, Martin},
        url = {https://hal.archives-ouvertes.fr/hal-01419861/document},
        booktitle = {{IEEE International Conference on Software Analysis, Evolution and Reengineering}},
        doi = {10.1109/SANER.2017.7884635},
        year = {2017},
    }


## Command line

As of [August 2024](https://github.com/SpoonLabs/npefix/commit/9512a41942926b255f1d441de7867c50b159c035), NpeFix expects Java 17:

```
$ export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/
$± java --version
openjdk 11.0.24 2024-07-16
OpenJDK Runtime Environment (build 11.0.24+8-post-Ubuntu-1ubuntu324.04.1)
OpenJDK 64-Bit Server VM (build 11.0.24+8-post-Ubuntu-1ubuntu324.04.1, mixed mode, sharing)
```

Install:
```
git clone https://github.com/Spirals-Team/npefix/
cd npefix
mvn install
```

Run:
```
java -jar target/npefix.jar \\
    --test failing_test
    --iteration 100
    --complianceLevel 8
    --workingdirectory . 
    --source path_source:path_test
    --classpath a.jar:b.jar;
    --repairStrategy <fully qualified name of a repair strategy>
```

## Reproduce the Scientific Evaluation

To reproduce the evaluation from the [paper]():
1. Gets the NPE Dataset: https://github.com/Spirals-Team/npe-dataset
2. Installs each bug on your system, in order to download the dependencies
3. Configures the location of the dataset in src/main/resources/config.ini
4. Creates the jar with all dependencies: `mvn clean compile assembly:single`
5. Execute `java -jar target/npefix-0.3-jar-with-dependencies.jar` (see the execution usage below)

#### Execution usage
```Bash
java -jar target/npefix-0.2-jar-with-dependencies.jar
                          (-p|--project) <math-1117...> [(-m|--mode) <mode>] [(-x|--working) <workingDirectory>] [(-k|--m2) <~/.m2>] [(-e|--epsilon) <0.2>] [(-s|--seed) <randomSeed>] [(-l|--laps) <nbLaps>] [(-t|--timeout) <testTimeout>]

  (-p|--project) <math-1117...>
        The name of the buggy program to execute (e.g. collection-360, math-1117, ...).

  [(-m|--mode) <mode>]
        The execution mode:
            * normal: Executes n times (the option --laps) the program and use the Epsilon Greedy algorithm to select the decision.
            * exploration: Explores all possible decision sequences with a limit of n laps (defined by --laps)

  [(-x|--working) <workingDirectory>]
        The path to the evaluation working directory.

  [(-k|--m2) <~/.m2>]
        The m2 folder. (default: ~/.m2)

  [(-e|--epsilon) <0.2>]
        The Epsilon-Greedy epsilon (the probability to use the exploration vs exploitation). (default: 0.2)

  [(-s|--seed) <randomSeed>]
        The seed of the random generator.

  [(-l|--laps) <nbLaps>]
        Defines the number of laps. (default: 100)

  [(-t|--timeout) <testTimeout>]
        Defines the timeout in second of the each test execution. (default: 5)
```

#### Execution output
```js
{
  "executions": [
    /* all laps */
    {
      "result": {
        "error": "<the exception>",
        "type": "<the oracle type>",
        "success": true
      },
      /* all decisions points */
      "locations": [{
        "sourceEnd": 12234,
        "executionCount": 0,
        "line": 352,
        "class": "org.apache.commons.collections.iterators.CollatingIterator",
        "sourceStart": 12193
      }],
      /* the runned test */
      "test": {
        "name": "testNullComparator",
        "class": "org.apache.commons.collections.iterators.TestCollatingIterator"
      },
      /* all decision made during the laps */
      "decisions": [{
        /* the location of the laps */
        "location": {
          "sourceEnd": 12234,
          "line": 352,
          "class": "org.apache.commons.collections.iterators.CollatingIterator",
          "sourceStart": 12193
        },
        /* the value used by the decision */
        "value": {
          "variableName": "leastObject",
          "value": "leastObject",
          "type": "int"
        },
        /* the value of the epsilon */
        "epsilon": 0.4,
        // the name of the strategy
        "strategy": "Strat4 VAR",
        "used": true,
        /* the decision type (new, best, random) */
        "decisionType": "new"
      }],
      "startDate": 1453918743999,
      "endDate": 1453918744165,
      "metadata": {"seed": 10}
    },
    ...
  ],
  "searchSpace": [
    /* all detected decisions */
    {
      "location": {
        "sourceEnd": 12234,
        "line": 352,
        "class": "org.apache.commons.collections.iterators.CollatingIterator",
        "sourceStart": 12193
      },
      "value": {
        "value": "1",
        "type": "int"
      },
      "epsilon": 0,
      "strategy": "Strat4 NEW",
      "used": false,
      "decisionType": "random"
    },
    ...
  ],
  "date": "Wed Jan 27 19:19:37 CET 2016"
}
```

## Other usages 

Maven-repair is a Maven plugin for bug-fixing, see <https://github.com/Spirals-Team/repairnator/tree/master/maven-repair>
