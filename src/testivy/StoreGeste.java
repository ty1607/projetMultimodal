/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testivy;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Thomas
 */
public class StoreGeste {

    public StoreGeste() {
    }
    
    /*
     * Read CSV file format in EXCEL style with ';' separator
     */
    public List<Geste> readGestesCSV(String filePATH)
            throws IOException {
        Reader in = new FileReader(filePATH);
        Iterable<CSVRecord> records = CSVFormat.EXCEL
          .withDelimiter(';')
          .withFirstRecordAsHeader()
          .parse(in);
        List<Geste> gestes = new ArrayList<>();
        for (CSVRecord record : records) {
            /*  0: points
                1: name
            */
            gestes.add(new Geste(StringtoArray(record.get(1)), record.get(0)));
        }
        return gestes;
    }
    
    /*
     * Store the gestes object in CSV File
     */
    public void storeWorkflowCSV(List<Geste> gestes, String filePATH) throws IOException {
        try (
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePATH));

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL
                    .withDelimiter(';')
                    .withHeader("Name", "ListPoint"));
        ) {
            gestes.forEach(geste -> {
                try {
                    csvPrinter.printRecord(geste.nom, Arrays.toString(geste.points.toArray()));
                } catch (IOException ex) {
                    Logger.getLogger(StoreGeste.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            csvPrinter.flush();
        }
    }
    
    private List<Point2D.Double> StringtoArray(String arr) {
        String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").
                replaceAll("Point2D.Double", "").replaceAll("\\s", "").split(",");
        
        for (String item : items) {
            System.out.println("items " + item);
        }

        List<Point2D.Double> data = new ArrayList<>();

        for (int i = 0; i < items.length-1; i++) {
            try {
                data.add(new Point2D.Double(
                        Double.parseDouble(items[i]),
                        Double.parseDouble(items[i+1])));
            } catch (NumberFormatException nfe) {
                
            }
        }
        return data;
    }
}
