# Student tests and benchmark

I use `check.py` to compare Java results with independent small-input oracles and to validate eight maximum-domain cases. It uses a fixed seed so the random cases are repeatable.

From the repository root:

```powershell
python tests/student/check.py
```

I use `SearchBenchmark.java` to compare the three search implementations on identical random and repetitive inputs. It prints five measurements and a median for each method and case. The setup, raw results, and explanations are in [ANALYSIS.md](../../ANALYSIS.md).
