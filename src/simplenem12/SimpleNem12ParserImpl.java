package simplenem12;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by w18749 on 19/05/17.
 */
public class SimpleNem12ParserImpl implements SimpleNem12Parser {

    @Override
    public Collection<MeterRead> parseSimpleNem12(File simpleNem12File) {
        Collection<MeterRead> meterReads = new ArrayList<>();
        BufferedReader file;
        String line;
        MeterRead mr = null;

        try {
            file = new BufferedReader(new FileReader(simpleNem12File));
            while((line = file.readLine()) != null) {
                String[] parts = line.split(",");
                System.out.println(line);
                String recordType = parts[0];
                switch(recordType) {
                    case "100":
                        break;
                    case "200":
                        mr = new MeterRead(parts[1],EnergyUnit.valueOf(parts[2]));
                        meterReads.add(mr);
                        break;
                    case "300":
                        Quality quality = Quality.valueOf(parts[3]);
                        BigDecimal volume = new BigDecimal(parts[2]);
                        MeterVolume meterVolume = new MeterVolume(volume, quality);
                        LocalDate localDate = LocalDate.parse(parts[1],DateTimeFormatter.BASIC_ISO_DATE);
                        mr.appendVolume(localDate,meterVolume);
                        break;
                    case "900":
                        break;
                }
            }
            file.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return meterReads;
    }
}
