package name.ignat.minerva.io.write.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import name.ignat.minerva.io.write.HeaderRowWriter;
import name.ignat.minerva.util.PoiUtils;

/**
 * @author Dan Ignat
 */
class ExcelHeaderRowWriter extends HeaderRowWriter<Sheet>
{
    @Override
    public void accept(String[] headers, Sheet sheet)
    {
        Row row = sheet.createRow(0);

        PoiUtils.writeRow(row, headers);
    }
}
