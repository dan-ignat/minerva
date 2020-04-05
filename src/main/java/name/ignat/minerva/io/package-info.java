/**
 * Uses the GoF Bridge pattern:
 * 
 * <pre>
 *                  ┌───────────────────────────────────────────┐
 *                  │                                           │
 *   SpreadsheetReader ────────────> HeaderRowReader            └────────> ContentRowReader
 *     Δ          Δ                    Δ          Δ                            Δ          Δ
 *     │          │                    │          │                            │          │
 * CsvReader ExcelReader  CsvHeaderRowReader ExcelHeaderRowReader  CsvContentRowReader ExcelContentRowReader
 * 
 * 
 * 
 *                  ┌───────────────────────────────────────────┐
 *                  │                                           │
 *   SpreadsheetWriter ────────────> HeaderRowWriter            └────────> ContentRowWriter
 *     Δ          Δ                    Δ          Δ                            Δ          Δ
 *     │          │                    │          │                            │          │
 * CsvWriter ExcelWriter  CsvHeaderRowWriter ExcelHeaderRowWriter  CsvContentRowWriter ExcelContentRowWriter
 * </pre> 
 * 
 * @author Dan Ignat
 */
package name.ignat.minerva.io;
