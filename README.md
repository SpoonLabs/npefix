# NPEFix [![Build Status](https://travis-ci.org/Spirals-Team/npefix.svg?branch=master)](https://travis-ci.org/Spirals-Team/npefix) [![Coverage Status](https://coveralls.io/repos/github/Spirals-Team/npefix/badge.svg?branch=master)](https://coveralls.io/github/Spirals-Team/npefix?branch=master)

This is the repository of NPEFix.

NPEFix is a system that systematically explores and assesses a set of possible runtime patches developed at Inria Lille.

This code is research code, released under the GPL licence.


## Getting Started

### Run the tests

```Bash
mvn test
```

### Run the evaluation

1. Gets the NPE Dataset: https://github.com/Spirals-Team/npe-dataset
2. Installs each bug on your system, in order to download the dependencies
3. Configures the location of the dataset in src/main/resources/config.ini
4. Creates the Bandit Repair jar with all dependencies: `mvn clean compile assembly:single`
5. Executes Bandit Repair `java -jar target/npefix-0.3-jar-with-dependencies.jar` (see the execution usage below)

#### Execution usage
```Bash
java -jar target/npefix-0.2-jar-with-dependencies.jar
                          (-p|--project) <math-1117...> [(-m|--mode) <mode>] [(-x|--working) <workingDirectory>] [(-k|--m2) <~/.m2>] [(-e|--epsilon) <0.2>] [(-s|--seed) <randomSeed>] [(-l|--laps) <nbLaps>] [(-t|--timeout) <testTimeout>]

  (-p|--project) <math-1117...>
        The name of the buggy program to execute (e.g. collection-360, math-1117, ...).

  [(-m|--mode) <mode>]
        The execution mode of Bandit Repair:
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