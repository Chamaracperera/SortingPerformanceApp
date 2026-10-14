
package sortingapp;

public class Dataset {

    private final String[] columnNames;
    private final String[][] data;

    public Dataset(String[] columnNames, String[][] data) {
        this.columnNames = columnNames;
        this.data = data;
    }

    public String[] getColumnNames() {
        return columnNames.clone();
    }

    public String[][] getData() {
        String[][] copy = new String[data.length][];

        for (int i = 0; i < data.length; i++) {
            copy[i] = data[i].clone();
        }

        return copy;
    }
}
