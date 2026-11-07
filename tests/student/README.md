# Student tests and benchmark

`StudentChecks.java` compares Java results with independent small-input oracles and validates 12 scale algorithm cases plus the RK collision fixture. It uses fixed seeds so the random cases are repeatable. The latest local run passed 571 small and boundary checks plus 13 scale checks.

From the repository root:

```powershell
javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/StudentChecks.java
java -Xmx384m -cp Java/build StudentChecks
```

`SearchBenchmark.java` compares the three search implementations on identical random and repetitive inputs. It prints five measurements and a median for each method and case. The setup, raw results, and explanations are in [ANALYSIS.md](../../ANALYSIS.md).
