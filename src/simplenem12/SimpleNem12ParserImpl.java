package simplenem12;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by David Clifford on 21/05/17.
 */
public class SimpleNem12ParserImpl implements SimpleNem12Parser {

    private Collection<MeterRead> meterReads = null;
    private MeterRead meterRead = null;

    @Override
    public Collection<MeterRead> parseSimpleNem12(File simpleNem12File) {
        List<String[]> lines = null;

        try {
            lines = loadCsvFile(simpleNem12File);
        } catch (FileNotFoundException ex) {
            System.out.printf("File %s not found\n",simpleNem12File.getName());
            System.exit(1);
        }

        try {
            parseLines(lines);
        } catch (ParsingException ex) {
            System.out.println(ex.getMessage());
            System.exit(2);
        }
        return meterReads;
    }

    public List<String[]> loadCsvFile(File simpleNem12File) throws FileNotFoundException {

        List<String[]> lines;

        InputStream inputFS = new FileInputStream(simpleNem12File);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputFS));

        lines = reader
                .lines()
                .map(line -> line.split(","))
                .collect(Collectors.toList());

        return lines;
    }

    public void parseLines(List<String[]> lines) throws ParsingException {

        int currentLine = 0;
        for (String line[] : lines) {

            currentLine++;
            String recordType = line[0];

            if (currentLine == lines.size() && !recordType.equals("900")) {
                throwParsingError(currentLine,"Last line must be a 900 record type");
            }
            if (currentLine == 1) {
                parse100(recordType, currentLine);
            } else if (recordType.equals("200")) {
                parse200(line, currentLine);
            } else if (recordType.equals("300")) {
                parse300(line, currentLine);
            } else if (recordType.equals("900")) {
                parse900(currentLine, lines.size());
            } else {
                throwParsingError(currentLine, String.format("Unknown record type %s", recordType));
            }
        }
    }

    private void parse100(String recordType, int currentLine) throws ParsingException {
        if(!recordType.equals("100")) {
            throwParsingError(currentLine,"File must start with a 100 record type");
        }
        meterReads = new HashSet<>();
    }

    private void parse200(String[] argument, int currentLine) throws ParsingException {
        checkArguments(currentLine, argument,2, "200");
        String nmi = validateNmi(argument[1], currentLine);
        EnergyUnit energyUnit = validateEnergyUnit(argument[2], currentLine);

        meterRead = new MeterRead(nmi, energyUnit);
        meterReads.add(meterRead);
    }

    private void parse300(String[] argument, int currentLine) throws ParsingException {
        checkArguments(currentLine, argument,3, "300");
        checkMeterRead(meterRead, currentLine);
        LocalDate localDate = validateLocalDate(argument[1], currentLine);
        BigDecimal volume = validateVolume(argument[2], currentLine);
        Quality quality = validateQuality(argument[3], currentLine);

        MeterVolume meterVolume = new MeterVolume(volume, quality);
        meterRead.appendVolume(localDate, meterVolume);
    }

    private void parse900(int currentLine, int totalLines)  throws ParsingException {
        if(currentLine != totalLines) {
            throwParsingError(currentLine,"Last line must be a 900 record type");
        }
    }

    private void checkArguments(int currentLine, String[] arguments, int numberOfArguments, String recordType) throws ParsingException {
        if(arguments.length!=numberOfArguments+1) {
            throwParsingError(currentLine,String.format("Wrong number of arguments for record type %s",recordType));
        }
    }

    private String validateNmi(String nmi, int currentLine) throws ParsingException {
        if(nmi.length()!= 10) {
            throw new ParsingException(currentLine, String.format("nmi must be 10 digits long"));
        }
        return nmi;
    }

    private EnergyUnit validateEnergyUnit(String eu, int currentLine) throws ParsingException {
        EnergyUnit energyUnit = null;
        try {
            energyUnit = EnergyUnit.valueOf(eu);
        } catch (IllegalArgumentException ex) {
            throwParsingError(currentLine, String.format("Unknown energy unit type %s",eu));
        }
        return energyUnit;
    }

    private void checkMeterRead(MeterRead meterRead, int currentLine) throws ParsingException {
        if(meterRead==null) {
            throwParsingError(currentLine,"A 300 record type must be preceded by a 200 record type");
        }
    }

    private LocalDate validateLocalDate(String ld, int currentLine) throws ParsingException {
        LocalDate localDate = null;
        try {
            localDate = LocalDate.parse(ld, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException ex) {
            throwParsingError(currentLine, String.format("Date %s has incorrect format",ld));
        }
        return localDate;
    }

    private BigDecimal validateVolume(String argument, int currentLine) throws ParsingException {
        BigDecimal volume = null;
        try {
            volume = new BigDecimal(argument);
        } catch (NumberFormatException ex) {
            throwParsingError(currentLine, String.format("Volume %s has incorrect format", argument));
        }
        return volume;
    }

    private Quality validateQuality(String argument, int currentLine) throws ParsingException {
        Quality quality = null;

        try {
            quality = Quality.valueOf(argument);
        } catch (IllegalArgumentException ex) {
            throwParsingError(currentLine, String.format("Unknown Quality type %s", argument));
        }
        return quality;
    }

    private void throwParsingError(int lineNumber, String message) throws ParsingException {
        throw new ParsingException(lineNumber, message);
    }

//    @Override
//    public Collection<MeterRead> parseSimpleNem12(File simpleNem12File) {
//        Collection<MeterRead> meterReads = new HashSet<>();
//        BufferedReader file;
//        String line;
//        MeterRead mr = null;
//
//        try {
//            file = new BufferedReader(new FileReader(simpleNem12File));
//            while((line = file.readLine()) != null) {
//                String[] parts = line.split(",");
//                System.out.println(line);
//                String recordType = parts[0];
//                switch(recordType) {
//                    case "100":
//                        break;
//                    case "200":
//                        mr = new MeterRead(parts[1],EnergyUnit.valueOf(parts[2]));
//                        meterReads.add(mr);
//                        break;
//                    case "300":
//                        Quality quality = Quality.valueOf(parts[3]);
//                        BigDecimal volume = new BigDecimal(parts[2]);
//                        MeterVolume meterVolume = new MeterVolume(volume, quality);
//                        LocalDate localDate = LocalDate.parse(parts[1],DateTimeFormatter.BASIC_ISO_DATE);
//                        mr.appendVolume(localDate,meterVolume);
//                        break;
//                    case "900":
//                        break;
//                }
//            }
//            file.close();
//        } catch (Exception e) {
//            System.out.println(e.toString());
//            return null;
//        }
//
//        return meterReads;
//    }

}
