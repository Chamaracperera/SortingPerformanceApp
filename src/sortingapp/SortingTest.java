
package sortingapp;

import java.util.Arrays;

public class SortingTest {

    public static void main(String[] args) {

        double[] original = {25, 19, 31, 22, 19};

        double[] a = original.clone();
        double[] b = original.clone();
        double[] c = original.clone();
        double[] d = original.clone();
        double[] e = original.clone();

        SortingAlgorithms.insertionSort(a);
        SortingAlgorithms.shellSort(b);
        SortingAlgorithms.mergeSort(c);
        SortingAlgorithms.quickSort(d);
        SortingAlgorithms.heapSort(e);

        System.out.println("Insertion: " + Arrays.toString(a));
        System.out.println("Shell:     " + Arrays.toString(b));
        System.out.println("Merge:     " + Arrays.toString(c));
        System.out.println("Quick:     " + Arrays.toString(d));
        System.out.println("Heap:      " + Arrays.toString(e));
    }
}
