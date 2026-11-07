# Assignment 4 — Dynamic Programming and String Processing

The project implements the eight required commands in Java 17: `lcs`, `merge`, `tree`, `naive`, `kmp`, `rk`, `suffix`, and `aho`. The program reads one JSON request per line and writes one JSON result per line. It uses the template's transport and launcher.

Only `Java/source` contains the algorithm implementation. The Python files in `tools` are the supplied build and validation utilities; they run the Java solution.

The complete task contracts, limits, and marking rules are in the [English specification](docs/Assignment_4_EN.md), [contracts](docs/guide/CONTRACTS.md), and [rubric](docs/guide/RUBRIC.md). The [interactive lesson and ungraded practice](https://rzhaniya.github.io/daa-assignment-4-archive/) helped identify the important edge cases. The assessed defence in Moodle is separate.

## Requirements

- JDK 17 or newer
- Python 3.12 or newer for the supplied launcher

`submission.json` selects Java. Run these commands from the repository root:

```powershell
python tools/run.py --setup
python tools/run.py --check
python tools/run.py --input examples/lcs.jsonl
javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/StudentChecks.java
java -Xmx384m -cp Java/build StudentChecks
javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/SearchBenchmark.java
java -Xmx384m -cp Java/build SearchBenchmark
```

On a Windows machine where `python` is only a Microsoft Store alias, use `& (uv python find 3.12)` in place of `python`. For example:

```powershell
& (uv python find 3.12) tools/run.py --check
```

The public check passed all 44 cases and the repeated mixed batch. `StudentChecks.java` passed 571 small and boundary checks plus 13 scale checks. GitHub Actions also runs `StudentChecks.java` after the public checks. [ANALYSIS.md](ANALYSIS.md) explains the algorithms, verification, memory usage, and measured search comparison.
