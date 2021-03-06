package test;

import org.junit.Test;
import simplenem12.MeterRead;
import simplenem12.ParsingException;
import simplenem12.SimpleNem12ParserImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by David on 21/05/2017.
 */
public class SimpleNem12ParserImplTest {

    private SimpleNem12ParserImpl simpleNem12Parser = new SimpleNem12ParserImpl();

    @Test
    public void testGetBothVolumes() {
        //set up
        File simpleNem12File = new File("SimpleNem12.csv");
        Collection<MeterRead> meterReads = simpleNem12Parser.parseSimpleNem12(simpleNem12File);
        //do
        MeterRead read6123456789 = meterReads.stream().filter(mr -> mr.getNmi().equals("6123456789")).findFirst().get();
        MeterRead read6987654321 = meterReads.stream().filter(mr -> mr.getNmi().equals("6987654321")).findFirst().get();
        //Assert
        assertEquals(read6123456789.getTotalVolume(),new BigDecimal("-36.84"));
        assertEquals(read6987654321.getTotalVolume(),new BigDecimal("14.33"));
    }

    @Test(expected = FileNotFoundException.class)
    public void testCsvFileNotExist() throws FileNotFoundException {
        File simpleNem12File = new File("NotExist.csv");
        simpleNem12Parser.loadCsvFile(simpleNem12File);
    }

    @Test
    public void testWhenFirstLineHasNo100() {
        List<String[]> lines = generateLines(new String[]{"200","300","900"});

        try {
            simpleNem12Parser.parseLines(lines);
        } catch (ParsingException ex) {
            assertEquals(ex.getMessage() , "Parse Error at line 1: First line must be a 100 record type");
        }
    }


    @Test
    public void testWhen100NotOnFirstLine() {
        List<String[]> lines = generateLines(new String[]{"100","200,6123456789,KWH","100","900"});

        try {
            simpleNem12Parser.parseLines(lines);
        } catch (ParsingException ex) {
            assertEquals(ex.getMessage() , "Parse Error at line 3: File must only have a 100 record type on the first line");
        }
    }

    @Test
    public void testWhenLastLineHasNo900() {
        List<String[]> lines = generateLines(new String[]{"100","200,6123456789,KWH","300,20161113,-50.8,A"});
        try {
            simpleNem12Parser.parseLines(lines);
        } catch (ParsingException ex) {
            assertEquals(ex.getMessage() , "Parse Error at line 3: Last line must be a 900 record type");
        }
    }

    @Test
    public void testWhen900NotLastLine() {
        List<String[]> lines = generateLines(new String[]{"100","200,6123456789,KWH","300,20161113,-50.8,A","900","100"});
        try {
            simpleNem12Parser.parseLines(lines);
        } catch (ParsingException ex) {
            assertEquals(ex.getMessage() , "Parse Error at line 4: Last line must be a 900 record type");
        }
    }

    @Test
    public void testWhenNmiNot10CharactersLong() {
        List<String[]> lines = generateLines(new String[]{"100","200,612345678,KWH","300,20161113,-50.8,A","900"});
        try {
            simpleNem12Parser.parseLines(lines);
        } catch (ParsingException ex) {
            assertEquals(ex.getMessage() , "Parse Error at line 2: nmi must be 10 digits long");
        }
    }

    @Test
    public void testWhenEnergyUnitNotKWH() {
        List<String[]> lines = generateLines(new String[]{"100","200,6123456789,KPH","300,20161113,-50.8,A","900"});
        try {
            simpleNem12Parser.parseLines(lines);
        } catch (ParsingException ex) {
            assertEquals(ex.getMessage() , "Parse Error at line 2: Unknown energy unit type KPH");
        }
    }

    @Test
    public void testInvalidLocalDate() {
        List<String[]> lines = generateLines(new String[]{"100","200,6123456789,KWH","300,20162113,-50.8,A","900"});
        try {
            simpleNem12Parser.parseLines(lines);
        } catch (ParsingException ex) {
            assertEquals(ex.getMessage() , "Parse Error at line 3: Date 20162113 has incorrect format");
        }
    }

    @Test
    public void testInvalidVolume() {
        List<String[]> lines = generateLines(new String[]{"100","200,6123456789,KWH","300,20161113,x50.8,A","900"});
        try {
            simpleNem12Parser.parseLines(lines);
        } catch (ParsingException ex) {
            assertEquals(ex.getMessage() , "Parse Error at line 3: Volume x50.8 has incorrect format");
        }
    }

    @Test
    public void testInvalidQuality() {
        List<String[]> lines = generateLines(new String[]{"100","200,6123456789,KWH","300,20161113,-50.8,X","900"});
        try {
            simpleNem12Parser.parseLines(lines);
        } catch (ParsingException ex) {
            assertEquals(ex.getMessage() , "Parse Error at line 3: Unknown Quality type X");
        }
    }

    @Test
    public void testWhen200RecordHasNot2Arguments() {
        List<String[]> lines = generateLines(new String[]{"100","200,6123456789","300,20161113,-50.8,A","900"});
        try {
            simpleNem12Parser.parseLines(lines);
        } catch (ParsingException ex) {
            assertEquals(ex.getMessage() , "Parse Error at line 2: Wrong number of arguments for record type 200");
        }
    }

    @Test
    public void testWhen300RecordHasNot3Arguments() {
        List<String[]> lines = generateLines(new String[]{"100","200,6123456789,KWH","300,20161113,-50.8","900"});
        try {
            simpleNem12Parser.parseLines(lines);
        } catch (ParsingException ex) {
            assertEquals(ex.getMessage() , "Parse Error at line 3: Wrong number of arguments for record type 300");
        }
    }

    private List<String[]> generateLines(String parts[]) {
        List<String[]> lines = new ArrayList<>();
        for(int i=0;i<parts.length;i++) {
            lines.add(parts[i].split(","));
        }
        return lines;
    }
}
