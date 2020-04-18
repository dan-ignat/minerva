package name.ignat.minerva.io.write.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import name.ignat.minerva.io.write.ContentRowWriter;
import name.ignat.minerva.io.write.WriteMapper;
import name.ignat.minerva.util.PoiUtils;

/**
 * @author Dan Ignat
 */
class ExcelContentRowWriter<O> extends ContentRowWriter<O, Sheet>
{
    private int rowIndex = 1;

    public ExcelContentRowWriter(WriteMapper<O> objectMapper, int numColumns)
    {
        super(objectMapper, numColumns);
    }

    @Override
    protected void writeRow(String[] values, Sheet sheet)
    {
        Row row = sheet.createRow(rowIndex++);

        PoiUtils.writeRow(row, values);
    }
}
