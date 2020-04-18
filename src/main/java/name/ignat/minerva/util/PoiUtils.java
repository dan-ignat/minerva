package name.ignat.minerva.util;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;
import static name.ignat.minerva.util.Doubles.toMinimalString;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import name.ignat.commons.exception.UnexpectedCaseException;

/**
 * @author Dan Ignat
 */
public final class PoiUtils
{
    public static List<String[]> sheetToStrings(Sheet sheet)
    {
        return stream(sheet).map(
            row -> stream(row).map(PoiUtils::getCellValueAsString).toArray(String[]::new)
        ).collect(toImmutableList());
    }

    @SuppressWarnings("preview")
    private static String getCellValueAsString(Cell cell)
    {
        return switch (cell.getCellType())
        {
            case STRING: yield cell.getStringCellValue();
            case NUMERIC: yield toMinimalString(cell.getNumericCellValue());
            case BLANK: yield "";
            default: throw new UnexpectedCaseException(cell.getCellType());
        };
    }

    /*
    public static List<List<String>> sheetToStringLists(Sheet sheet)
    {
        return stream(sheet).map(
            row -> stream(row).map(PoiUtils::getCellValueAsString).collect(toImmutableList())
        ).collect(toImmutableList());
    }
    */

    public static void writeRow(Row row, String[] cellValues)
    {
        for (int i = 0; i < cellValues.length; i++)
        {
            row.createCell(i).setCellValue(cellValues[i]);
        }
    }

    public static CellStyle createHeaderStyle(Workbook workbook)
    {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);

        return headerStyle;
    }

    public static void emboldenHeaderRow(Sheet sheet, CellStyle headerStyle)
    {
        Row headerRow = sheet.iterator().next();

        // This doesn't seem to work.  Maybe it only works if you set it before the cells are created.
        //headerRow.setRowStyle(headerStyle);

        stream(headerRow).forEach(headerCell -> headerCell.setCellStyle(headerStyle));
    }

    public static void setAutoFilter(Sheet sheet)
    {
        sheet.setAutoFilter(getCellRangeAddress(sheet));
    }

    public static CellRangeAddress getCellRangeAddress(Sheet sheet)
    {
        Cell firstCell = getFirstCell(sheet);
        Cell lastCell = getLastCell(sheet);

        return new CellRangeAddress(
            firstCell.getRowIndex(), lastCell.getRowIndex(), firstCell.getColumnIndex(), lastCell.getColumnIndex());
    }

    public static Cell getFirstCell(Sheet sheet)
    {
        Row firstRow = getFirstRow(sheet);

        return firstRow.getCell(firstRow.getFirstCellNum());
    }

    public static Row getFirstRow(Sheet sheet)
    {
        return sheet.getRow(sheet.getFirstRowNum());
    }

    public static Cell getLastCell(Sheet sheet)
    {
        Row lastRow = getLastRow(sheet);

        return lastRow.getCell(lastRow.getLastCellNum() - 1);
    }

    public static Row getLastRow(Sheet sheet)
    {
        return sheet.getRow(sheet.getLastRowNum());
    }

    public static void autoSizeColumns(Sheet sheet)
    {
        Row headerRow = sheet.iterator().next();

        stream(headerRow).forEach(headerCell -> sheet.autoSizeColumn(headerCell.getColumnIndex()));
    }

    public static void freezeHeaderRow(Sheet sheet)
    {
        sheet.createFreezePane(0, 1);
        //sheet.createFreezePane(0, 1, 0, 1);
    }

    private PoiUtils() { }
}
