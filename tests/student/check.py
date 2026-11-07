import functools
import random
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
from run import build, execute
from public_validate import check

randomizer = random.Random(4217)
cases = []


def add(name, request, expected):
    cases.append((name, request, expected))


def lcs_length(a, b):
    previous = [0] * (len(b) + 1)
    for character in a:
        current = [0] * (len(b) + 1)
        for j, other in enumerate(b, 1):
            current[j] = previous[j - 1] + 1 if character == other else max(previous[j], current[j - 1])
        previous = current
    return previous[-1]


def merge_cost(sizes):
    @functools.cache
    def solve(left, right):
        if left == right:
            return 0
        return sum(sizes[left:right + 1]) + min(
            solve(left, middle) + solve(middle + 1, right)
            for middle in range(left, right)
        )
    return solve(0, len(sizes) - 1) if sizes else 0


def tree_weight(weights, edges):
    best = 0
    for mask in range(1 << len(weights)):
        if all(not (mask >> u & 1 and mask >> v & 1) for u, v in edges):
            best = max(best, sum(weight for i, weight in enumerate(weights) if mask >> i & 1))
    return best


def positions(text, pattern):
    if not pattern:
        return list(range(len(text) + 1))
    return [i for i in range(len(text) - len(pattern) + 1) if text.startswith(pattern, i)]


def suffix_result(text):
    sa = sorted(range(len(text)), key=lambda i: text[i:])
    lcp = [0] * len(sa)
    for i in range(1, len(sa)):
        while (sa[i - 1] + lcp[i] < len(text) and sa[i] + lcp[i] < len(text)
               and text[sa[i - 1] + lcp[i]] == text[sa[i] + lcp[i]]):
            lcp[i] += 1
    best = max(lcp, default=0)
    return {"sa": sa, "lcp": lcp, "repeated": text[sa[lcp.index(best)]:sa[lcp.index(best)] + best] if best else ""}


def aho_counts(patterns, texts):
    return [[len(positions(text, pattern)) for pattern in patterns] for text in texts]


for a, b in [("", ""), ("ABC", ""), ("ABCA", "CABA"), ("tea_time", "team")]:
    add("lcs-boundary", {"task": "lcs", "a": a, "b": b}, {"length": lcs_length(a, b)})
for _ in range(30):
    a = "".join(randomizer.choices("ABC", k=randomizer.randrange(9)))
    b = "".join(randomizer.choices("ABC", k=randomizer.randrange(9)))
    add("lcs-random", {"task": "lcs", "a": a, "b": b}, {"length": lcs_length(a, b)})

for sizes in [[], [0], [0, 0, 0], [10**9] * 4, [2, 3, 4], [9, 1, 9, 1]]:
    add("merge-boundary", {"task": "merge", "sizes": sizes}, {"cost": merge_cost(sizes)})
for _ in range(30):
    sizes = [randomizer.randrange(10) for _ in range(randomizer.randrange(1, 8))]
    add("merge-random", {"task": "merge", "sizes": sizes}, {"cost": merge_cost(sizes)})

for weights, edges in [([], []), ([-4], []), ([0], []), ([10**9] * 4, [[0, 1], [1, 2], [2, 3]])]:
    add("tree-boundary", {"task": "tree", "n": len(weights), "weights": weights, "edges": edges},
        {"weight": tree_weight(weights, edges)})
for _ in range(30):
    n = randomizer.randrange(1, 11)
    weights = [randomizer.randrange(-5, 8) for _ in range(n)]
    edges = [[i, randomizer.randrange(i)] for i in range(1, n)]
    add("tree-random", {"task": "tree", "n": n, "weights": weights, "edges": edges},
        {"weight": tree_weight(weights, edges)})

for text, pattern in [("", ""), ("aAa!aA", "aA"), ("abdj", "ab"), ("aaaaa", "aaa"), ("ab", "abc")]:
    for task in ("naive", "kmp", "rk"):
        add("search-boundary", {"task": task, "text": text, "pattern": pattern},
            {"positions": positions(text, pattern)})
for _ in range(30):
    text = "".join(randomizer.choices("ab? ", k=randomizer.randrange(16)))
    pattern = "".join(randomizer.choices("ab? ", k=randomizer.randrange(7)))
    for task in ("naive", "kmp", "rk"):
        add("search-random", {"task": task, "text": text, "pattern": pattern},
            {"positions": positions(text, pattern)})

for text in ("", "a", "banana", "aaaaaa", "abcdef", "abababab"):
    add("suffix-boundary", {"task": "suffix", "text": text}, suffix_result(text))
for _ in range(30):
    text = "".join(randomizer.choices("abc", k=randomizer.randrange(14)))
    add("suffix-random", {"task": "suffix", "text": text}, suffix_result(text))

for patterns, texts in [([], ["", "abc"]), (["a", "aa", "a"], ["aaaa", "", "aaaa"]),
                        (["he", "she", "he"], ["sheshe"]), (["!", "?!"], ["?!?!!"])]:
    add("aho-boundary", {"task": "aho", "patterns": patterns, "texts": texts},
        {"counts": aho_counts(patterns, texts)})
for _ in range(30):
    patterns = ["".join(randomizer.choices("ab?", k=randomizer.randrange(1, 5)))
                for _ in range(randomizer.randrange(6))]
    texts = ["".join(randomizer.choices("ab?", k=randomizer.randrange(12)))
             for _ in range(randomizer.randrange(4))]
    add("aho-random", {"task": "aho", "patterns": patterns, "texts": texts},
        {"counts": aho_counts(patterns, texts)})

command = build("Java")
answers = execute(command, [request for _, request, _ in cases])
failures = [(name, request, expected, answer) for (name, request, expected), answer in zip(cases, answers)
            if not check(request, answer, expected)]
for name, request, expected, answer in failures[:10]:
    print("FAIL", name, request, expected, answer)
print(f"Student cases: {len(cases) - len(failures)}/{len(cases)} passed")
if failures:
    sys.exit(1)

large_text = "a" * 50000
wide_patterns = ["".join(randomizer.choices("ABCDEFGHIJKLMNOPQRSTUVWXYZ", k=10)) for _ in range(10000)]
scale_cases = [
    ("lcs-maximum", {"task": "lcs", "a": "A" * 1000, "b": "A" * 1000},
     {"length": 1000}),
    ("merge-maximum", {"task": "merge", "sizes": [0] * 200}, {"cost": 0}),
    ("tree-deep-chain", {"task": "tree", "n": 100000, "weights": [1] * 100000,
                          "edges": [[i, i + 1] for i in range(99999)]}, {"weight": 50000}),
    ("kmp-maximum", {"task": "kmp", "text": "a" * 200000, "pattern": "a" * 100000},
     {"positions": list(range(100001))}),
    ("rk-maximum", {"task": "rk", "text": "a" * 10000, "pattern": "a" * 1999 + "b"},
     {"positions": []}),
    ("suffix-maximum", {"task": "suffix", "text": large_text},
     {"sa": list(range(49999, -1, -1)), "lcp": list(range(50000)), "repeated": "a" * 49999}),
    ("aho-dense", {"task": "aho", "patterns": ["a" * 10] * 10000,
                   "texts": ["a" * 200000]}, {"counts": [[199991] * 10000]}),
    ("aho-wide-trie", {"task": "aho", "patterns": wide_patterns, "texts": [""]},
     {"counts": [[0] * 10000]}),
]
scale_answers = execute(command, [request for _, request, _ in scale_cases])
scale_failures = [name for (name, request, expected), answer in zip(scale_cases, scale_answers)
                  if not check(request, answer, expected)]
for name in scale_failures:
    print("FAIL", name)
print(f"Scale cases: {len(scale_cases) - len(scale_failures)}/{len(scale_cases)} passed")
sys.exit(bool(scale_failures))
