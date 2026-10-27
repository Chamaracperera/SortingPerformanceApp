
package sortingapp;

import java.util.ArrayList;
import java.util.List;

public class PerformanceEvaluator {

    public static class Result {
        private final String algorithm;
        private final long timeNs;
        private final double[] sortedData;

        public Result(
                String algorithm,
                long timeNs,
                double[] sortedData) {

            this.algorithm = algorithm;
            this.timeNs = timeNs;
            this.sortedData = sortedData.clone();
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public long getTimeNs() {
            return timeNs;
        }

        public double[] getSortedData() {
            return sortedData.clone();
        }
    }

    public List<Result> evaluate(double[] original) {

        List<Result> results = new ArrayList<>();

        String[] names = {
            "Insertion Sort",
            "Shell Sort",
            "Merge Sort",
            "Quick Sort",
            "Heap Sort"
        };

        for (String name : names) {

            double[] copy = original.clone();

            // Warm up the JVM using a separate copy.
            // Do not include this time in the results.
            double[] warmup = original.clone();
            sort(name, warmup);

            long start = System.nanoTime();

            sort(name, copy);

            long end = System.nanoTime();

            results.add(new Result(
                    name,
                    end - start,
                    copy
            ));
        }

        return results;
    }

    private void sort(String name, double[] arr) {

        switch (name) {

            case "Insertion Sort":
                SortingAlgorithms.insertionSort(arr);
                break;

            case "Shell Sort":
                SortingAlgorithms.shellSort(arr);
                break;

            case "Merge Sort":
                SortingAlgorithms.mergeSort(arr);
                break;

            case "Quick Sort":
                SortingAlgorithms.quickSort(arr);
                break;

            case "Heap Sort":
                SortingAlgorithms.heapSort(arr);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown algorithm: " + name
                );
        }
    }

    public Result findBest(List<Result> results) {

        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException(
                    "No performance results available."
            );
        }

        Result best = results.get(0);

        for (Result result : results) {
            if (result.getTimeNs() < best.getTimeNs()) {
                best = result;
            }
        }

        return best;
    }
}
