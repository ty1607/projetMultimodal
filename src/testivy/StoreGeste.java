/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testivy;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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
    public List<Geste> readGestesCSV(String filePATH) {
        Reader in;
        List<Geste> gestes = null;
        try {
            in = new FileReader(filePATH);
            Iterable<CSVRecord> records = CSVFormat.EXCEL
          .withDelimiter(';')
          .withFirstRecordAsHeader()
          .parse(in);
        gestes = new ArrayList<>();
        for (CSVRecord record : records) {
            /*  0: points
                1: name
            */
            gestes.add(new Geste(StringtoArray(record.get(1)), record.get(0)));
        }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StoreGeste.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("FILE NOT FOUND");
        } catch (IOException ex) {
            Logger.getLogger(StoreGeste.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("CANNOT READ FILE");
        }
        
        return gestes;
    }
    
    /*
     * Store the gestes object in CSV File
     */
    public void storeWorkflowCSV(List<Geste> gestes, String filePATH) {
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
        } catch (IOException ex) {
            Logger.getLogger(StoreGeste.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addGestetoCSV(Geste geste, String path) {
        List<Geste> gestes;
        gestes = readGestesCSV(path);
        gestes.add(geste);
        storeWorkflowCSV(gestes, path);
    }
    
    private List<Point2D.Double> StringtoArray(String arr) {
        System.out.println("arr" + arr);
        String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").
                replaceAll("Point2D.Double", "").replaceAll("\\s", "").split(",");

        List<Point2D.Double> data = new ArrayList<>();

        for (int i = 0; i < items.length-1; i = i + 2) {
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
