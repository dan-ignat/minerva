package name.ignat.minerva.io.read.excel;

import javax.annotation.Nonnull;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import name.ignat.minerva.io.read.ContentRowReader;
import name.ignat.minerva.io.read.ReadMapper;

class ExcelContentRowReader<O> extends ContentRowReader<Row, Cell, O>
{
    public ExcelContentRowReader(Integer[] columnIndexes, ReadMapper<O> objectMapper)
    {
        super(columnIndexes, objectMapper);
    }

    @Override
    protected Cell getCell(@Nonnull Row row, int index)
    {
        return row.getCell(index);
    }

    @Override
    protected String getCellValue(@Nonnull Cell cell)
    {
        return cell.getStringCellValue();
    }
}
