package name.ignat.minerva.io.write.excel;

import org.apache.poi.ss.usermodel.Row;

final class ExcelUtils
{
    public static void writeRow(Row row, String[] cellValues)
    {
        for (int i = 0; i < cellValues.length; i++)
        {
            row.createCell(i).setCellValue(cellValues[i]);
        }
    }

    private ExcelUtils() { }
}
