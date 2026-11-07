# Assignment 4 — Analysis

## Environment and commands

I selected Java in `submission.json`. The solution compiles with `javac --release 17`. The measured runtime was Java 21.0.12.1 on Microsoft Windows 11 Pro 10.0.26200, an Intel Core i5-12450HX, and 16 GB physical RAM. Python 3.12.16 ran the supplied launcher. On this machine the bare `python` command is a Microsoft Store alias, so I used the installed Python found by `uv`.

I executed these commands from the repository root:

```powershell
& (uv python find 3.12) tools/run.py --setup
& (uv python find 3.12) tools/run.py --check
& (uv python find 3.12) tools/run.py --input examples/lcs.jsonl
& (uv python find 3.12) tests/student/check.py
javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/SearchBenchmark.java
java -Xmx384m -cp Java/build SearchBenchmark
```

The setup ping passed. The input example returned `{"sequence":"team","length":4}`. The public checker passed 44 cases and its repeated complete mixed batch. My independent student checker passed 279 small and boundary cases plus 8 scale cases. The Java compiler printed only the template `Main.java` unchecked-operation note.

## Algorithm explanations

### LCS

`dp[i][j]` is the optimum subsequence length for the first `i` characters of `a` and the first `j` of `b`. The zero row and column are zero. When the final characters match, I extend `dp[i-1][j-1]`; otherwise I take the larger of `dp[i-1][j]` and `dp[i][j-1]`. Increasing `i` and `j` makes every dependency available. An optimum either includes matching final characters or discards at least one mismatched final character, which proves the recurrence. I walk backward through the table, append a matched character, and reverse the result. Ties choose the upper cell; any optimal path is valid. Time and auxiliary table memory are `O((|a|+1)(|b|+1))`; the sequence output takes `O(min(|a|,|b|))`.

### Ordered merging

`dp[l][r]` is the minimum cost to merge original blocks `l..r` into one block; a single block costs zero. If the last merge joins `[l,k]` and `[k+1,r]`, its cost is `dp[l][k] + dp[k+1][r] + sum(l..r)`. A prefix sum obtains the interval sum in constant time. I fill intervals by increasing length and record the best `k`. Every legal final merge has one such split, and replacing either child plan by a cheaper one cannot hurt, so taking the minimum is optimal. Reconstruction recursively emits both child plans before the parent `[l,k,r]`, making the plan chronological and adjacent at every step. Time is `O(n^3)`; auxiliary table and split memory are `O(n^2)`, with `O(n)` plan output and recursion depth at most 200. Empty and one-block inputs return zero without constructing tables. Totals use `long`.

### Tree independent set

I root the tree at vertex zero and build a parent-first order without recursion. For each vertex in reverse order, `take[u]` is its weight plus `skip[v]` for each child, while `skip[u]` sums `max(take[v],skip[v])`. Leaves have `take=weight` and `skip=0`. A selected parent forbids its child; an unselected parent permits the better child state. These exhaust the independent-set choices, so the root optimum is `max(take[0],skip[0])`. A forward pass reconstructs one set using the parent decision. Ties select `skip`, allowing the empty set for nonpositive weights. The adjacency arrays, order, parent, DP, and selected flags use `O(n)` auxiliary memory; time is `O(n)`, and the selected output is `O(n)`. The iterative passes handle a 100,000-vertex chain. Sums use `long`.

### Naive search

For every start `0..n-m`, I compare pattern characters until mismatch or full match. Every valid start is visited, including overlapping ones. An empty pattern returns all `n+1` boundaries; a longer pattern returns no starts. Worst-case time is `O(nm+n+1)`, auxiliary memory is `O(1)`, and the result list uses `O(z+1)` for `z` positions.

### KMP

`pi[i]` is the length of the longest proper prefix of `pattern[0..i]` that is also its suffix. During prefix construction and text scanning, a mismatch falls back to `pi[j-1]`, retaining the longest viable prefix without moving the text index backward. After a complete match, the same fallback preserves overlaps. The empty pattern is handled before indexing. Each fallback shortens `j`, so total time is `O(n+m+z+1)`; auxiliary memory is `O(m+1)` for `pi`, with `O(z+1)` output memory.

### Exact Rabin–Karp

I use base 31, modulus 101, and ASCII character codes. The leftmost character has power `31^(m-1)`. After checking one window, I subtract its leftmost weighted character, normalize a negative remainder, multiply by 31, and add the incoming character. A matching hash is only a candidate: I compare the actual characters before adding a start. Therefore collisions cannot create false matches. Empty and longer patterns are handled before a window is formed. For `c` equal-hash windows, time is `O(n+m+c*m)`; the permitted worst case is `O(nm+n+m+1)`. Hash state uses `O(1)` auxiliary memory and the result uses `O(z+1)`. The small teaching modulus can cause many candidate windows, so this is not a deterministic linear-time method.

### Suffix array and Kasai LCP

For doubling width `k`, each suffix is ordered by the pair `(rank[i],rank[i+k])`, with `-1` beyond the text. Two stable counting sorts order the second and first pair components; equal pairs receive the same next rank. After doubling, ranks describe prefixes of twice the old width. Once all ranks differ, the order is the suffix array. Only starts `0..n-1` appear, so no empty suffix or sentinel leaks into the result. Counting sort gives `O(n log(n+1))` time and `O(n+1)` auxiliary memory.

The inverse array maps a text start to its suffix-array position. Kasai compares each suffix with its predecessor and reuses the previous common-prefix length minus one, so its total work is `O(n)`. `lcp[0]=0`; `lcp[i]` compares `sa[i-1]` and `sa[i]`. Any repeated substring is a common prefix of two suffixes; the maximum such prefix occurs between adjacent suffixes in sorted order. I take a substring of maximum LCP length, allowing overlap. LCP and inverse arrays add `O(n)` auxiliary memory; `sa`, `lcp`, and the repeated string are output.

### Aho–Corasick

Trie states represent dictionary prefixes. I allocate one 95-character transition row per actual state. BFS gives each state a failure link to the longest proper suffix that is also a trie prefix and fills missing transitions from that link. For each text, I start at the root and increment the visit count of the reached state per character. Reverse BFS propagates each state's visits to its failure state. Thus a terminal pattern receives counts both when its own state is reached and when it is a suffix of another reached state. Duplicate dictionary entries share a terminal state but retain separate output columns. I allocate fresh visits and reset the automaton state for every text.

With total pattern length `L`, pattern count `p`, and actual trie-state count `S`, construction costs `O(L+95S+p)` time and `O(95S+p)` auxiliary memory. Each text costs `O(|text|+S+p)` time and `O(S)` temporary visits; the result takes `O(qp)` output memory for `q` texts. Reverse propagation avoids enumerating every dense match. An empty dictionary yields one empty row per text.

## Own tests

`tests/student/check.py` uses seed 4217. It computes independent small expected values with a separate LCS table, exhaustive merge splits, exhaustive tree subsets, direct string matching, direct suffix sorting/LCP, and direct pattern counts. The supplied validator checks witness validity against these independently computed optimum values. All 279 small/boundary cases passed. It then checks eight published-domain scale cases: 1000×1000 LCS, 200 zero blocks, a 100,000-vertex chain, 200,000-character KMP text with a 100,000-character pattern, a 10,000-character RK text, a 50,000-character suffix input, 10,000 duplicate Aho patterns against 200,000 text characters, and a wide 10,000-pattern trie. All eight passed in one mixed JSON Lines process.

| Command | Input | Expected output | Actual output | Defect targeted |
|---|---|---|---|---|
| `lcs` | `a="tea_time", b="team"` | length 4, `"team"` | length 4, `"team"` | Confusing subsequence with substring |
| `merge` | `sizes=[2,3,4]` | cost 14, `[[0,0,1],[0,1,2]]` | cost 14, `[[0,0,1],[0,1,2]]` | Wrong split or nonchronological plan |
| `tree` | Four-vertex chain, every weight `10^9` | weight 2,000,000,000, one nonadjacent pair | weight 2,000,000,000, `[1,3]` | 32-bit overflow and adjacent selection |
| `naive` | `text="aaaaa", pattern="aaa"` | `[0,1,2]` | `[0,1,2]` | Missed overlaps |
| `kmp` | `text="abababa", pattern="aba"` | `[0,2,4]` | `[0,2,4]` | Missing full-match fallback |
| `rk` | `text="abdj", pattern="ab"` | `[0]` | `[0]` | Hash collision reported as a match |
| `suffix` | `text="banana"` | `sa=[5,3,1,0,4,2]`, `lcp=[0,1,3,0,0,2]`, `"ana"` | Same | Wrong suffix order, LCP, or overlap handling |
| `aho` | `patterns=["he","she","he"], texts=["sheshe",""]` | `[[2,2,2],[0,0,0]]` | Same | Suffix matches, duplicates, and reset |

The checker also covers empty inputs, zero/negative weights, ties, punctuation, pattern longer than text, and consecutive mixed requests. For `rk`, `ab` and `dj` have the same prescribed hash, making character verification observable.

## Search comparison

`tests/student/SearchBenchmark.java` generates identical inputs for all three algorithms. For random cases it uses Java `Random` with seeds 4218, 4219, and 4220, text alphabet `ABCD`, and a pattern copied from `text[n/3..n/3+m)`. Repetitive cases use `"a"^n` and `"a"^m` at the same sizes. Every case is within the three methods' common domain. I checked equal outputs before measurement and after every timed batch. Each method had 3,000 unmeasured calls per case for JIT warm-up. Five measured runs each time 100 calls to the algorithm method on a prepared request map; the table shows elapsed microseconds per call, obtained by dividing each batch by 100. Compilation, Java startup, input generation, JSON parsing, and output printing are excluded equally. The median is the middle of the five values. The command above reproduces the experiment on this machine; times vary with system load.

| Case | n | m | Method | Five runs (µs/call) | Median |
|---|---:|---:|---|---|---:|
| Random | 1000 | 32 | naive | 1.982, 1.974, 2.153, 2.260, 2.152 | 2.152 |
| Random | 1000 | 32 | kmp | 2.077, 2.028, 2.170, 2.183, 2.167 | 2.167 |
| Random | 1000 | 32 | rk | 6.536, 6.584, 6.316, 6.474, 6.640 | 6.536 |
| Repetitive | 1000 | 32 | naive | 37.185, 29.534, 32.427, 28.452, 28.871 | 29.534 |
| Repetitive | 1000 | 32 | kmp | 12.617, 12.418, 9.297, 10.843, 10.429 | 10.843 |
| Repetitive | 1000 | 32 | rk | 19.393, 17.269, 18.292, 20.110, 23.595 | 19.393 |
| Random | 2000 | 64 | naive | 4.432, 4.240, 4.503, 4.168, 7.369 | 4.432 |
| Random | 2000 | 64 | kmp | 6.470, 4.503, 4.595, 4.661, 4.666 | 4.661 |
| Random | 2000 | 64 | rk | 13.035, 12.961, 13.474, 13.072, 13.146 | 13.072 |
| Repetitive | 2000 | 64 | naive | 105.738, 117.680, 113.806, 104.842, 104.997 | 105.738 |
| Repetitive | 2000 | 64 | kmp | 11.022, 11.072, 11.066, 11.001, 10.582 | 11.022 |
| Repetitive | 2000 | 64 | rk | 60.318, 49.761, 53.679, 68.131, 73.601 | 60.318 |
| Random | 3000 | 96 | naive | 5.904, 6.494, 6.193, 6.251, 6.924 | 6.251 |
| Random | 3000 | 96 | kmp | 6.245, 7.285, 6.731, 7.960, 8.747 | 7.285 |
| Random | 3000 | 96 | rk | 37.437, 27.397, 34.102, 19.533, 19.441 | 27.397 |
| Repetitive | 3000 | 96 | naive | 229.383, 213.196, 215.547, 249.095, 247.855 | 229.383 |
| Repetitive | 3000 | 96 | kmp | 15.532, 15.462, 15.894, 15.452, 32.960 | 15.532 |
| Repetitive | 3000 | 96 | rk | 94.028, 111.593, 95.265, 94.303, 99.171 | 95.265 |

The repetitive inputs make nearly every naive character comparison succeed and make every RK hash window a candidate requiring full verification. KMP keeps its linear bound and was faster on the larger repetitive cases. Random cases create fewer long comparisons for naive and fewer RK candidates. JIT compilation, allocation, scheduling, and short timed calls affect exact medians; these measurements do not prove asymptotic bounds or establish a universal ranking. Modulus 101 deliberately exposes collisions and is not representative of every production rolling-hash design. Exact verification is necessary regardless of hash choice.

## Sources and AI assistance

I used the supplied [README](README.md), [English specification](docs/Assignment_4_EN.md), [contracts](docs/guide/CONTRACTS.md), [testing guide](docs/guide/TESTING.md), [rubric](docs/guide/RUBRIC.md), [interactive lesson and practice](https://rzhaniya.github.io/daa-assignment-4-archive/), template adapter, and public validator. I used OpenAI Codex to write this analysis and README at my request. 
