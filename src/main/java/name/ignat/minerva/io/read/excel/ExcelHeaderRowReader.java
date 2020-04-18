package name.ignat.minerva.io.read.excel;

import javax.annotation.Nonnull;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import name.ignat.minerva.io.read.HeaderRowReader;

/**
 * @author Dan Ignat
 */
class ExcelHeaderRowReader extends HeaderRowReader<Row, Cell>
{
    public ExcelHeaderRowReader(String[] desiredColumnHeaders)
    {
        super(desiredColumnHeaders);
    }

    @Override
    protected String getCellValue(@Nonnull Cell headerCell)
    {
        return headerCell.getStringCellValue();
    }
}
