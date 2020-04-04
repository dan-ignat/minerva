package name.ignat.minerva.io.write.excel;

import static name.ignat.minerva.io.write.excel.ExcelUtils.writeRow;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import name.ignat.minerva.io.write.HeaderRowWriter;

class ExcelHeaderRowWriter extends HeaderRowWriter<Sheet>
{
    @Override
    public void accept(String[] headers, Sheet sheet)
    {
        Row row = sheet.createRow(0);

        writeRow(row, headers);
    }
}
