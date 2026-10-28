# ⚡ Sorting Algorithm Performance Evaluator

## 📌 Overview

A Java desktop application that loads real-world CSV datasets, runs five classic sorting algorithms on any numeric column, and benchmarks their execution time side-by-side — all through a clean, intuitive GUI.

No command-line. No guesswork. Just load, sort, and compare.

---

## 🖼️ Application Preview

The interface is divided into three key areas:

| Zone | Purpose |
|---|---|
| **Upload Panel** | Select and load any `.csv` file |
| **CSV Data Preview** | Browse up to 100 rows of your dataset |
| **Performance Results** | Compare execution times across all five algorithms |

The fastest algorithm is highlighted automatically at the end of each run.

---

## 🚀 Features

- 📂 **CSV Upload** — Load any `.csv` file; BOM-safe, supports quoted fields
- 🔍 **Smart Column Detection** — Only numeric columns appear in the sort selector
- 🔢 **Five Sorting Algorithms** — All implemented from scratch (no `Arrays.sort`)
- ⏱️ **Nanosecond Benchmarking** — Includes a JVM warm-up pass for fair timing
- 🏆 **Best Algorithm Highlight** — Fastest algorithm is identified and displayed
- 👁️ **Sorted Values Preview** — See the first 100 sorted values instantly
- ⚠️ **Robust Error Handling** — Invalid files, empty columns, and non-numeric values are all caught gracefully

---

## 🧠 Sorting Algorithms Implemented

All algorithms operate on `double[]` arrays and are located in [`SortingAlgorithms.java`](src/sortingapp/SortingAlgorithms.java).

### 1. Insertion Sort
Builds the sorted array one element at a time by shifting larger elements rightward.  
- **Best:** O(n) · **Average/Worst:** O(n²)  
- Efficient for small or nearly-sorted datasets.

### 2. Shell Sort
A generalization of Insertion Sort that sorts elements far apart first, then narrows the gap.  
- **Best:** O(n log n) · **Worst:** O(n²)  
- A strong mid-tier performer.

### 3. Merge Sort
Recursively divides the array in half, sorts each half, then merges them.  
- **All cases:** O(n log n)  
- Consistent and stable. Uses a pre-allocated temp array for efficiency.

### 4. Quick Sort
Partitions the array around a pivot (Lomuto scheme), then recursively sorts each partition.  
- **Best/Average:** O(n log n) · **Worst:** O(n²) on sorted input  
- Typically the fastest in practice.

### 5. Heap Sort
Builds a max-heap, then repeatedly extracts the maximum to sort in-place.  
- **All cases:** O(n log n)  
- In-place and reliable with no extra memory.

---

## 📁 Project Structure

```
SortingPerformanceApp/
│
├── src/
│   └── sortingapp/
│       ├── Main.java                  # Entry point — launches the Swing GUI on the EDT
│       ├── MainFrame.java             # Full GUI layout and user interaction logic
│       ├── SortingAlgorithms.java     # All five sorting algorithm implementations
│       ├── PerformanceEvaluator.java  # Benchmarks each algorithm with JVM warm-up
│       ├── CSVReader.java             # Parses CSV files with quote and BOM handling
│       ├── Dataset.java               # Immutable data model for loaded CSV content
│       └── SortingTest.java           # Quick standalone test for all algorithms
│
├── sample/                            # Sample CSV files for testing
├── SortingApp.jar                     # Executable Application
└── README.md

```

---

## 🛠️ How to Run

**Option A — Run the Executable Application (Windows)**

### Prerequisites
- Windows operating system
- Java Runtime Environment (JRE) or JDK compatible with the application (Java 21 recommended)

### Steps


1. Download or clone this repository.
2. Open the SortingPerformance/ folder.
3. Double-click SortingApp.exe to launch the application.
4. Upload a CSV file, select a numeric column, and run the sorting algorithms.

**Note:** *The executable application created using a standard Java JAR launcher requires Java to be installed on your computer. If you receive a Java-related error, install a compatible Java runtime and try again.*

**Option B — Run Using an IDE**

### Prerequisites
- Java 21 JDK recommended
- Any IDE (IntelliJ IDEA, Eclipse, NetBeans, VS Code)

### Steps

1. Open the project in your IDE.
2. Run Main.java as a Java application.
3. The GUI will launch automatically.

**Option C — Run Using the Command Line**
```bash
# Compile
javac -d out src/sortingapp/*.java

# Run
java -cp out sortingapp.Main
```

### Quick Algorithm Test (no GUI)
```bash
javac -d out src/sortingapp/*.java
java -cp out sortingapp.SortingTest
```
Expected output:
```
Insertion: [19.0, 19.0, 22.0, 25.0, 31.0]
Shell:     [19.0, 19.0, 22.0, 25.0, 31.0]
Merge:     [19.0, 19.0, 22.0, 25.0, 31.0]
Quick:     [19.0, 19.0, 22.0, 25.0, 31.0]
Heap:      [19.0, 19.0, 22.0, 25.0, 31.0]
```

---

## 📊 Sample Dataset

You can download free real-world CSV datasets from:  
🔗 [https://www.kaggle.com/datasets](https://www.kaggle.com/datasets)

Good columns to test with: population figures, prices, scores, distances, ages.

---

## 🏗️ Architecture & Design Decisions

| Decision | Rationale |
|---|---|
| Separate `CSVReader` class | Single responsibility; easy to test independently |
| `Dataset` returns defensive copies | Prevents accidental mutation of loaded data |
| JVM warm-up run in `PerformanceEvaluator` | Reduces JIT compilation noise for fairer timing |
| `Long.class` column type in results table | Ensures numeric sort order (not lexicographic) |
| `SwingUtilities.invokeLater` in `Main` | Keeps all UI work on the Event Dispatch Thread |

---

## ⚙️ Performance Evaluation Methodology

For each algorithm:
1. A **warm-up sort** is run on a separate copy of the data (not timed) to let the JVM JIT-compile the method.
2. The **actual timed sort** runs on a fresh clone of the original data.
3. Execution time is measured in **nanoseconds** using `System.nanoTime()`.
4. The algorithm with the **lowest time** is declared the winner.

---

## 👥 Group Contribution Breakdown

This project is made possible through the collaboration of the following team members:

| Member | Responsibility |
|---|---|
| **[Chamara Perera ](https://github.com/Chamaracperera)** | CSV file upload, parsing (`CSVReader.java`), and dataset model (`Dataset.java`) |
| **[Sathsarani Geethamali](https://github.com/Sathsarani2002)** | All five sorting algorithm implementations (`SortingAlgorithms.java`) |
| **[Chanuli Sandanayake](https://github.com/Chanuli-Sandanayake)** | Performance evaluation (`PerformanceEvaluator.java`) and full GUI (`MainFrame.java`) |

--

## Git Workflow:

* 🔄 Branching (`main`, `Developing`, `member1`, `member2`, `member3`)
* ✅ Commit logs for each implementation step
* 🔀 Pull requests and code review for merging
* 👥 Contributor history for tracking individual work

--
 
## 📝 License

This project was developed for academic purposes as part of the Programming module at the University of Colombo.