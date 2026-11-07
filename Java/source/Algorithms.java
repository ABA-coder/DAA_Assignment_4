import java.util.*;

public final class Algorithms {
    private Algorithms() {}

    static Map<String, Object> lcs(Map<String, Object> input) {
        String a = (String) input.get("a"), b = (String) input.get("b");
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) for (int j = 1; j <= m; j++)
            dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                ? dp[i - 1][j - 1] + 1 : Math.max(dp[i - 1][j], dp[i][j - 1]);
        StringBuilder sequence = new StringBuilder();
        int i = n, j = m;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sequence.append(a.charAt(i - 1)); i--; j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) i--;
            else j--;
        }
        return Map.of("length", dp[n][m], "sequence", sequence.reverse().toString());
    }

    static Map<String, Object> merge(Map<String, Object> input) {
        List<?> sizes = (List<?>) input.get("sizes");
        int n = sizes.size();
        if (n < 2) return Map.of("cost", 0L, "plan", List.of());
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) prefix[i + 1] = prefix[i] + ((Number) sizes.get(i)).longValue();
        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];
        for (int length = 2; length <= n; length++) for (int left = 0; left + length <= n; left++) {
            int right = left + length - 1;
            long best = Long.MAX_VALUE;
            for (int middle = left; middle < right; middle++) {
                long candidate = dp[left][middle] + dp[middle + 1][right]
                    + prefix[right + 1] - prefix[left];
                if (candidate < best) { best = candidate; split[left][right] = middle; }
            }
            dp[left][right] = best;
        }
        List<int[]> plan = new ArrayList<>(n - 1);
        buildMergePlan(0, n - 1, split, plan);
        return Map.of("cost", dp[0][n - 1], "plan", plan);
    }

    private static void buildMergePlan(int left, int right, int[][] split, List<int[]> plan) {
        if (left == right) return;
        int middle = split[left][right];
        buildMergePlan(left, middle, split, plan);
        buildMergePlan(middle + 1, right, split, plan);
        plan.add(new int[] {left, middle, right});
    }

    static Map<String, Object> tree(Map<String, Object> input) {
        int n = ((Number) input.get("n")).intValue();
        if (n == 0) return Map.of("weight", 0L, "selected", List.of());
        List<?> weights = (List<?>) input.get("weights"), edges = (List<?>) input.get("edges");
        int[] head = new int[n];
        Arrays.fill(head, -1);
        int[] to = new int[2 * (n - 1)], next = new int[to.length];
        int edgeCount = 0;
        for (Object edgeObject : edges) {
            List<?> edge = (List<?>) edgeObject;
            int u = ((Number) edge.get(0)).intValue(), v = ((Number) edge.get(1)).intValue();
            to[edgeCount] = v; next[edgeCount] = head[u]; head[u] = edgeCount++;
            to[edgeCount] = u; next[edgeCount] = head[v]; head[v] = edgeCount++;
        }
        int[] parent = new int[n], order = new int[n];
        Arrays.fill(parent, -2);
        parent[0] = -1;
        int count = 1;
        for (int index = 0; index < count; index++) {
            int u = order[index];
            for (int edge = head[u]; edge != -1; edge = next[edge]) {
                int v = to[edge];
                if (v == parent[u]) continue;
                parent[v] = u; order[count++] = v;
            }
        }
        long[] take = new long[n], skip = new long[n];
        for (int index = n - 1; index >= 0; index--) {
            int u = order[index];
            take[u] = ((Number) weights.get(u)).longValue();
            for (int edge = head[u]; edge != -1; edge = next[edge]) {
                int v = to[edge];
                if (parent[v] == u) { take[u] += skip[v]; skip[u] += Math.max(take[v], skip[v]); }
            }
        }
        boolean[] picked = new boolean[n];
        List<Integer> selected = new ArrayList<>();
        for (int index = 0; index < n; index++) {
            int u = order[index];
            if ((parent[u] < 0 || !picked[parent[u]]) && take[u] > skip[u]) {
                picked[u] = true; selected.add(u);
            }
        }
        return Map.of("weight", Math.max(take[0], skip[0]), "selected", selected);
    }

    private static List<Integer> boundaries(int n) {
        List<Integer> result = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) result.add(i);
        return result;
    }

    static Map<String, Object> naive(Map<String, Object> input) {
        String text = (String) input.get("text"), pattern = (String) input.get("pattern");
        int n = text.length(), m = pattern.length();
        if (m == 0) return Map.of("positions", boundaries(n));
        List<Integer> positions = new ArrayList<>();
        for (int start = 0; start + m <= n; start++) {
            int j = 0;
            while (j < m && text.charAt(start + j) == pattern.charAt(j)) j++;
            if (j == m) positions.add(start);
        }
        return Map.of("positions", positions);
    }

    static Map<String, Object> kmp(Map<String, Object> input) {
        String text = (String) input.get("text"), pattern = (String) input.get("pattern");
        int n = text.length(), m = pattern.length();
        if (m == 0) return Map.of("positions", boundaries(n));
        int[] pi = new int[m];
        for (int i = 1; i < m; i++) {
            int j = pi[i - 1];
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) j = pi[j - 1];
            if (pattern.charAt(i) == pattern.charAt(j)) j++;
            pi[i] = j;
        }
        List<Integer> positions = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < n; i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) j = pi[j - 1];
            if (text.charAt(i) == pattern.charAt(j)) j++;
            if (j == m) { positions.add(i - m + 1); j = pi[j - 1]; }
        }
        return Map.of("positions", positions);
    }

    static Map<String, Object> rk(Map<String, Object> input) {
        String text = (String) input.get("text"), pattern = (String) input.get("pattern");
        int n = text.length(), m = pattern.length();
        if (m == 0) return Map.of("positions", boundaries(n));
        List<Integer> positions = new ArrayList<>();
        if (m > n) return Map.of("positions", positions);
        int highest = 1, target = 0, window = 0;
        for (int i = 0; i < m; i++) {
            target = (target * 31 + pattern.charAt(i)) % 101;
            window = (window * 31 + text.charAt(i)) % 101;
            if (i + 1 < m) highest = highest * 31 % 101;
        }
        for (int start = 0; start + m <= n; start++) {
            if (window == target) {
                int j = 0;
                while (j < m && text.charAt(start + j) == pattern.charAt(j)) j++;
                if (j == m) positions.add(start);
            }
            if (start + m < n) {
                window = (window - text.charAt(start) * highest) % 101;
                if (window < 0) window += 101;
                window = (window * 31 + text.charAt(start + m)) % 101;
            }
        }
        return Map.of("positions", positions);
    }

    private static void sortSuffixes(int[] suffixes, int[] buffer, int[] rank,
                                     int offset, int classes, int[] counts) {
        int n = suffixes.length;
        Arrays.fill(counts, 0, classes + 2, 0);
        for (int suffix : suffixes) counts[suffix + offset < n ? rank[suffix + offset] + 1 : 0]++;
        int total = 0;
        for (int key = 0; key <= classes + 1; key++) {
            int frequency = counts[key]; counts[key] = total; total += frequency;
        }
        for (int suffix : suffixes) {
            int key = suffix + offset < n ? rank[suffix + offset] + 1 : 0;
            buffer[counts[key]++] = suffix;
        }
    }

    static Map<String, Object> suffix(Map<String, Object> input) {
        String text = (String) input.get("text");
        int n = text.length();
        if (n == 0) return Map.of("sa", new int[0], "lcp", new int[0], "repeated", "");
        int[] sa = new int[n], buffer = new int[n], rank = new int[n], newRank = new int[n];
        int[] counts = new int[Math.max(n + 2, 129)];
        for (int i = 0; i < n; i++) { sa[i] = i; rank[i] = text.charAt(i); }
        int classes = 127;
        for (int width = 1; width < n; width *= 2) {
            sortSuffixes(sa, buffer, rank, width, classes, counts);
            sortSuffixes(buffer, sa, rank, 0, classes, counts);
            int nextClass = 0;
            newRank[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                int previous = sa[i - 1], current = sa[i];
                int previousSecond = previous + width < n ? rank[previous + width] : -1;
                int currentSecond = current + width < n ? rank[current + width] : -1;
                if (rank[previous] != rank[current] || previousSecond != currentSecond) nextClass++;
                newRank[current] = nextClass;
            }
            int[] swap = rank; rank = newRank; newRank = swap;
            classes = nextClass;
            if (classes == n - 1) break;
        }
        int[] inverse = new int[n], lcp = new int[n];
        for (int i = 0; i < n; i++) inverse[sa[i]] = i;
        int common = 0, best = 0, bestStart = 0;
        for (int i = 0; i < n; i++) {
            int position = inverse[i];
            if (position == 0) { common = 0; continue; }
            int previous = sa[position - 1];
            while (i + common < n && previous + common < n
                   && text.charAt(i + common) == text.charAt(previous + common)) common++;
            lcp[position] = common;
            if (common > best) { best = common; bestStart = i; }
            if (common > 0) common--;
        }
        return Map.of("sa", sa, "lcp", lcp, "repeated", text.substring(bestStart, bestStart + best));
    }

    static Map<String, Object> aho(Map<String, Object> input) {
        List<?> patterns = (List<?>) input.get("patterns"), texts = (List<?>) input.get("texts");
        int p = patterns.size();
        List<int[]> transitions = new ArrayList<>();
        transitions.add(new int[95]);
        int[] terminal = new int[p];
        for (int i = 0; i < p; i++) {
            String pattern = (String) patterns.get(i);
            int state = 0;
            for (int j = 0; j < pattern.length(); j++) {
                int code = pattern.charAt(j) - 32;
                int[] row = transitions.get(state);
                if (row[code] == 0) {
                    row[code] = transitions.size();
                    transitions.add(new int[95]);
                }
                state = row[code];
            }
            terminal[i] = state;
        }
        int states = transitions.size();
        int[] failure = new int[states];
        int[] queue = new int[states];
        int count = 0;
        for (int code = 0; code < 95; code++) {
            int child = transitions.get(0)[code];
            if (child != 0) queue[count++] = child;
        }
        for (int index = 0; index < count; index++) {
            int state = queue[index];
            int[] row = transitions.get(state);
            int[] fallback = transitions.get(failure[state]);
            for (int code = 0; code < 95; code++) {
                int child = row[code];
                if (child != 0) {
                    failure[child] = fallback[code];
                    queue[count++] = child;
                } else row[code] = fallback[code];
            }
        }
        long[][] rows = new long[texts.size()][p];
        for (int row = 0; row < texts.size(); row++) {
            String text = (String) texts.get(row);
            long[] visits = new long[states];
            int state = 0;
            for (int i = 0; i < text.length(); i++) {
                state = transitions.get(state)[text.charAt(i) - 32]; visits[state]++;
            }
            for (int index = count - 1; index >= 0; index--) {
                int node = queue[index]; visits[failure[node]] += visits[node];
            }
            for (int i = 0; i < p; i++) rows[row][i] = visits[terminal[i]];
        }
        return Map.of("counts", rows);
    }
}
