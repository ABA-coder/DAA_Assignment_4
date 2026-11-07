import java.util.*;

public final class SearchBenchmark {
    private static final String[] TASKS = {"naive", "kmp", "rk"};

    private static String randomText(int length, long seed) {
        Random random = new Random(seed);
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) result.append("ABCD".charAt(random.nextInt(4)));
        return result.toString();
    }

    private static Object search(String task, Map<String, Object> request) {
        switch (task) {
            case "naive": return Algorithms.naive(request).get("positions");
            case "kmp": return Algorithms.kmp(request).get("positions");
            default: return Algorithms.rk(request).get("positions");
        }
    }

    private static void measure(String kind, int n, int m, long seed) {
        String text = kind.equals("random") ? randomText(n, seed) : "a".repeat(n);
        String pattern = kind.equals("random") ? text.substring(n / 3, n / 3 + m) : "a".repeat(m);
        Map<String, Object> request = Map.of("text", text, "pattern", pattern);
        Object expected = search("naive", request);
        for (String task : TASKS) {
            if (!expected.equals(search(task, request))) throw new IllegalStateException("Search mismatch");
            for (int i = 0; i < 3000; i++) search(task, request);
            long[] times = new long[5];
            for (int i = 0; i < times.length; i++) {
                long start = System.nanoTime();
                Object result = null;
                for (int repetition = 0; repetition < 100; repetition++) result = search(task, request);
                times[i] = (System.nanoTime() - start) / 100;
                if (!expected.equals(result)) throw new IllegalStateException("Search mismatch");
            }
            long[] sorted = times.clone();
            Arrays.sort(sorted);
            System.out.printf(Locale.ROOT, "%s,%d,%d,%d,%s,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f%n",
                kind, n, m, seed, task, times[0] / 1000.0, times[1] / 1000.0,
                times[2] / 1000.0, times[3] / 1000.0, times[4] / 1000.0,
                sorted[2] / 1000.0);
        }
    }

    public static void main(String[] args) {
        System.out.println("kind,n,m,seed,task,run1_us,run2_us,run3_us,run4_us,run5_us,median_us");
        for (int size = 1; size <= 3; size++) {
            int n = size * 1000;
            int m = size * 32;
            long seed = 4217 + size;
            measure("random", n, m, seed);
            measure("repetitive", n, m, seed);
        }
    }
}
