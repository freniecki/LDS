package pl.frot.data;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import weka.core.Instances;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class ArffFileReader {
    private static final Logger log = Logger.getLogger(ArffFileReader.class.getName());

    private ArffFileReader() {}

    public static void readAndSaveArff(String directoryPath, String toReadFile, String toSaveFile) {
        try {
            Instances data = DataSource.read(directoryPath + toReadFile);
            data.setClassIndex(data.numAttributes() - 1); // jeśli chcesz ustawić klasę

            Remove remove = new Remove();

            // Ustaw atrybuty do usunięcia albo do zachowania
            remove.setAttributeIndices("1,2,5,13,14,15,19,22,25,33,37"); // indeksy atrybutów do zachowania lub usunięcia (1-based)
            remove.setInvertSelection(true); // oznacza: zachowaj te kolumny, zamiast je usuwać

            // Załaduj instancje do filtra
            remove.setInputFormat(data);

            // Filtruj dane
            Instances filteredData = Filter.useFilter(data, remove);

            saveToFile(filteredData, directoryPath + toSaveFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveToFile(Instances data, String path) throws IOException {
        CSVSaver saver = new CSVSaver();
        saver.setInstances(data);
        saver.setFile(new File(path));
        saver.writeBatch();
        log.info("Zapisano dane do pliku CSV.");
    }
}
