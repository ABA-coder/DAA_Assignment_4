# Student tests and benchmark

I use `StudentChecks.java` to compare Java results with independent small-input oracles and to validate eight maximum-domain cases. It uses a fixed seed so the random cases are repeatable.

From the repository root:

```powershell
javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/StudentChecks.java
java -Xmx384m -cp Java/build StudentChecks
```

I use `SearchBenchmark.java` to compare the three search implementations on identical random and repetitive inputs. It prints five measurements and a median for each method and case. The setup, raw results, and explanations are in [ANALYSIS.md](../../ANALYSIS.md).
