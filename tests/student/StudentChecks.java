import java.util.*;

public final class StudentChecks {
    private static final Random RANDOM = new Random(4217);
    private static int count;

    private static void require(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        count++;
    }

    private static String randomString(String alphabet, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) result.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        return result.toString();
    }

    private static boolean subsequence(String sequence, String text) {
        int index = 0;
        for (int i = 0; i < text.length() && index < sequence.length(); i++) {
            if (text.charAt(i) == sequence.charAt(index)) index++;
        }
        return index == sequence.length();
    }

    private static int lcsLength(String a, String b) {
        int[] previous = new int[b.length() + 1];
        for (int i = 1; i <= a.length(); i++) {
            int[] current = new int[b.length() + 1];
            for (int j = 1; j <= b.length(); j++) {
                current[j] = a.charAt(i - 1) == b.charAt(j - 1)
                    ? previous[j - 1] + 1 : Math.max(previous[j], current[j - 1]);
            }
            previous = current;
        }
        return previous[b.length()];
    }

    private static void testLcs(String a, String b, int expected, String name) {
        Map<String, Object> result = Algorithms.lcs(Map.of("a", a, "b", b));
        String sequence = (String) result.get("sequence");
        int length = ((Number) result.get("length")).intValue();
        require(length == expected && sequence.length() == expected
            && subsequence(sequence, a) && subsequence(sequence, b), name);
    }

    private static long mergeCost(List<Integer> sizes, int left, int right, long[][] memo) {
        if (left == right) return 0;
        if (memo[left][right] >= 0) return memo[left][right];
        long sum = 0;
        for (int i = left; i <= right; i++) sum += sizes.get(i);
        long best = Long.MAX_VALUE;
        for (int middle = left; middle < right; middle++) {
            best = Math.min(best, mergeCost(sizes, left, middle, memo)
                + mergeCost(sizes, middle + 1, right, memo) + sum);
        }
        return memo[left][right] = best;
    }

    private static long mergeCost(List<Integer> sizes) {
        if (sizes.size() < 2) return 0;
        long[][] memo = new long[sizes.size()][sizes.size()];
        for (long[] row : memo) Arrays.fill(row, -1);
        return mergeCost(sizes, 0, sizes.size() - 1, memo);
    }

    private static void testMerge(List<Integer> sizes, long expected, String name) {
        Map<String, Object> result = Algorithms.merge(Map.of("sizes", sizes));
        long cost = ((Number) result.get("cost")).longValue();
        @SuppressWarnings("unchecked")
        List<int[]> plan = (List<int[]>) result.get("plan");
        List<int[]> blocks = new ArrayList<>();
        for (int i = 0; i < sizes.size(); i++) blocks.add(new int[] {i, i});
        long observed = 0;
        boolean valid = plan.size() == Math.max(0, sizes.size() - 1);
        for (int[] step : plan) {
            if (step.length != 3) { valid = false; break; }
            int index = -1;
            for (int i = 0; i + 1 < blocks.size(); i++) {
                if (blocks.get(i)[0] == step[0] && blocks.get(i)[1] == step[1]
                    && blocks.get(i + 1)[0] == step[1] + 1
                    && blocks.get(i + 1)[1] == step[2]) {
                    index = i;
                    break;
                }
            }
            if (index < 0) { valid = false; break; }
            for (int i = step[0]; i <= step[2]; i++) observed += sizes.get(i);
            blocks.set(index, new int[] {step[0], step[2]});
            blocks.remove(index + 1);
        }
        require(valid && blocks.size() <= 1 && cost == expected && observed == cost, name);
    }

    private static long treeWeight(List<Integer> weights, List<List<Integer>> edges) {
        long best = 0;
        for (int mask = 0; mask < 1 << weights.size(); mask++) {
            boolean valid = true;
            for (List<Integer> edge : edges) {
                if ((mask & 1 << edge.get(0)) != 0 && (mask & 1 << edge.get(1)) != 0) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                long sum = 0;
                for (int i = 0; i < weights.size(); i++) if ((mask & 1 << i) != 0) sum += weights.get(i);
                best = Math.max(best, sum);
            }
        }
        return best;
    }

    private static void testTree(List<Integer> weights, List<List<Integer>> edges, long expected, String name) {
        Map<String, Object> request = Map.of("n", weights.size(), "weights", weights, "edges", edges);
        Map<String, Object> result = Algorithms.tree(request);
        long weight = ((Number) result.get("weight")).longValue();
        @SuppressWarnings("unchecked")
        List<Integer> selected = (List<Integer>) result.get("selected");
        Set<Integer> chosen = new HashSet<>(selected);
        boolean valid = chosen.size() == selected.size();
        long sum = 0;
        for (int vertex : selected) {
            if (vertex < 0 || vertex >= weights.size()) valid = false;
            else sum += weights.get(vertex);
        }
        for (List<Integer> edge : edges) {
            if (chosen.contains(edge.get(0)) && chosen.contains(edge.get(1))) valid = false;
        }
        require(valid && weight == expected && sum == weight, name);
    }

    private static List<Integer> positions(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        if (pattern.isEmpty()) {
            for (int i = 0; i <= text.length(); i++) result.add(i);
        } else {
            for (int i = 0; i + pattern.length() <= text.length(); i++) {
                if (text.startsWith(pattern, i)) result.add(i);
            }
        }
        return result;
    }

    private static void testSearch(String task, String text, String pattern, List<Integer> expected, String name) {
        Map<String, Object> request = Map.of("text", text, "pattern", pattern);
        Map<String, Object> result;
        switch (task) {
            case "naive": result = Algorithms.naive(request); break;
            case "kmp": result = Algorithms.kmp(request); break;
            default: result = Algorithms.rk(request);
        }
        require(expected.equals(result.get("positions")), name);
    }

    private static int[] suffixArray(String text) {
        Integer[] order = new Integer[text.length()];
        for (int i = 0; i < order.length; i++) order[i] = i;
        Arrays.sort(order, Comparator.comparing(text::substring));
        int[] result = new int[order.length];
        for (int i = 0; i < order.length; i++) result[i] = order[i];
        return result;
    }

    private static int[] lcpArray(String text, int[] sa) {
        int[] result = new int[sa.length];
        for (int i = 1; i < sa.length; i++) {
            while (sa[i - 1] + result[i] < text.length() && sa[i] + result[i] < text.length()
                && text.charAt(sa[i - 1] + result[i]) == text.charAt(sa[i] + result[i])) result[i]++;
        }
        return result;
    }

    private static void testSuffix(String text, int[] expectedSa, int[] expectedLcp, String name) {
        Map<String, Object> result = Algorithms.suffix(Map.of("text", text));
        int[] sa = (int[]) result.get("sa");
        int[] lcp = (int[]) result.get("lcp");
        String repeated = (String) result.get("repeated");
        int best = 0;
        for (int value : expectedLcp) best = Math.max(best, value);
        int occurrences = positions(text, repeated).size();
        require(Arrays.equals(sa, expectedSa) && Arrays.equals(lcp, expectedLcp)
            && repeated.length() == best && (best == 0 || occurrences >= 2), name);
    }

    private static long[][] ahoCounts(List<String> patterns, List<String> texts) {
        long[][] expected = new long[texts.size()][patterns.size()];
        for (int i = 0; i < texts.size(); i++) {
            for (int j = 0; j < patterns.size(); j++) {
                expected[i][j] = positions(texts.get(i), patterns.get(j)).size();
            }
        }
        return expected;
    }

    private static void testAho(List<String> patterns, List<String> texts, long[][] expected, String name) {
        Map<String, Object> result = Algorithms.aho(Map.of("patterns", patterns, "texts", texts));
        require(Arrays.deepEquals((long[][]) result.get("counts"), expected), name);
    }

    private static void smallCases() {
        String[][] lcsCases = {{"", ""}, {"ABC", ""}, {"ABCA", "CABA"}, {"tea_time", "team"}};
        for (String[] pair : lcsCases) testLcs(pair[0], pair[1], lcsLength(pair[0], pair[1]), "lcs-boundary");
        for (int i = 0; i < 30; i++) {
            String a = randomString("ABC", RANDOM.nextInt(9));
            String b = randomString("ABC", RANDOM.nextInt(9));
            testLcs(a, b, lcsLength(a, b), "lcs-random");
        }

        List<List<Integer>> mergeCases = List.of(List.of(), List.of(0), List.of(0, 0, 0),
            List.of(1000000000, 1000000000, 1000000000, 1000000000),
            List.of(2, 3, 4), List.of(9, 1, 9, 1));
        for (List<Integer> sizes : mergeCases) testMerge(sizes, mergeCost(sizes), "merge-boundary");
        for (int i = 0; i < 30; i++) {
            int n = RANDOM.nextInt(7) + 1;
            List<Integer> sizes = new ArrayList<>();
            for (int j = 0; j < n; j++) sizes.add(RANDOM.nextInt(10));
            testMerge(sizes, mergeCost(sizes), "merge-random");
        }

        testTree(List.of(), List.of(), 0, "tree-empty");
        testTree(List.of(-4), List.of(), 0, "tree-negative");
        testTree(List.of(0), List.of(), 0, "tree-zero");
        List<Integer> largeWeights = List.of(1000000000, 1000000000, 1000000000, 1000000000);
        List<List<Integer>> chain = List.of(List.of(0, 1), List.of(1, 2), List.of(2, 3));
        testTree(largeWeights, chain, treeWeight(largeWeights, chain), "tree-large-values");
        for (int i = 0; i < 30; i++) {
            int n = RANDOM.nextInt(10) + 1;
            List<Integer> weights = new ArrayList<>();
            List<List<Integer>> edges = new ArrayList<>();
            for (int j = 0; j < n; j++) weights.add(RANDOM.nextInt(13) - 5);
            for (int j = 1; j < n; j++) edges.add(List.of(j, RANDOM.nextInt(j)));
            testTree(weights, edges, treeWeight(weights, edges), "tree-random");
        }

        String[][] searchCases = {{"", ""}, {"aAa!aA", "aA"}, {"abdj", "ab"},
            {"aaaaa", "aaa"}, {"ab", "abc"}};
        for (String[] pair : searchCases) {
            for (String task : List.of("naive", "kmp", "rk")) {
                testSearch(task, pair[0], pair[1], positions(pair[0], pair[1]), "search-boundary");
            }
        }
        for (int i = 0; i < 30; i++) {
            String text = randomString("ab? ", RANDOM.nextInt(16));
            String pattern = randomString("ab? ", RANDOM.nextInt(7));
            for (String task : List.of("naive", "kmp", "rk")) {
                testSearch(task, text, pattern, positions(text, pattern), "search-random");
            }
        }

        for (String text : List.of("", "a", "banana", "aaaaaa", "abcdef", "abababab")) {
            int[] sa = suffixArray(text);
            testSuffix(text, sa, lcpArray(text, sa), "suffix-boundary");
        }
        for (int i = 0; i < 30; i++) {
            String text = randomString("abc", RANDOM.nextInt(14));
            int[] sa = suffixArray(text);
            testSuffix(text, sa, lcpArray(text, sa), "suffix-random");
        }

        testAho(List.of(), List.of("", "abc"), new long[][] {{}, {}}, "aho-empty");
        testAho(List.of("a", "aa", "a"), List.of("aaaa", "", "aaaa"),
            new long[][] {{4, 3, 4}, {0, 0, 0}, {4, 3, 4}}, "aho-duplicate");
        testAho(List.of("he", "she", "he"), List.of("sheshe"),
            new long[][] {{2, 2, 2}}, "aho-suffix");
        testAho(List.of("!", "?!"), List.of("?!?!!"),
            ahoCounts(List.of("!", "?!"), List.of("?!?!!")), "aho-punctuation");
        for (int i = 0; i < 30; i++) {
            List<String> patterns = new ArrayList<>(), texts = new ArrayList<>();
            int p = RANDOM.nextInt(6), q = RANDOM.nextInt(4);
            for (int j = 0; j < p; j++) patterns.add(randomString("ab?", RANDOM.nextInt(4) + 1));
            for (int j = 0; j < q; j++) texts.add(randomString("ab?", RANDOM.nextInt(12)));
            testAho(patterns, texts, ahoCounts(patterns, texts), "aho-random");
        }
    }

    private static void scaleCases() {
        testLcs("A".repeat(1000), "A".repeat(1000), 1000, "lcs-maximum");
        testMerge(Collections.nCopies(200, 0), 0, "merge-maximum");
        List<Integer> weights = Collections.nCopies(100000, 1);
        List<List<Integer>> edges = new ArrayList<>();
        for (int i = 0; i < 99999; i++) edges.add(List.of(i, i + 1));
        testTree(weights, edges, 50000, "tree-deep-chain");
        List<Integer> expected = new ArrayList<>();
        for (int i = 0; i <= 100000; i++) expected.add(i);
        testSearch("kmp", "a".repeat(200000), "a".repeat(100000), expected, "kmp-maximum");
        testSearch("rk", "a".repeat(10000), "a".repeat(1999) + "b", List.of(), "rk-maximum");
        String text = "a".repeat(50000);
        int[] sa = new int[50000], lcp = new int[50000];
        for (int i = 0; i < 50000; i++) { sa[i] = 49999 - i; lcp[i] = i; }
        testSuffix(text, sa, lcp, "suffix-maximum");
        List<String> densePatterns = Collections.nCopies(10000, "a".repeat(10));
        long[][] denseExpected = new long[1][10000];
        Arrays.fill(denseExpected[0], 199991);
        testAho(densePatterns, List.of("a".repeat(200000)), denseExpected, "aho-dense");
        List<String> widePatterns = new ArrayList<>();
        for (int i = 0; i < 10000; i++) widePatterns.add(randomString("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 10));
        testAho(widePatterns, List.of(""), new long[1][10000], "aho-wide-trie");
    }

    public static void main(String[] args) {
        smallCases();
        System.out.println("Student cases: " + count + "/" + count + " passed");
        count = 0;
        scaleCases();
        System.out.println("Scale cases: " + count + "/" + count + " passed");
    }
}
