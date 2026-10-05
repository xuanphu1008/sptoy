package com.toyland.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Xuat bang du lieu ra Excel (.xlsx) bang Apache POI: tieu de, ky bao cao, dong tieu de cot to mau,
 * so tien dinh dang #,##0, ty le 0.0%, dong tong cong, co dinh dong tieu de.
 */
public final class ExcelExporter {

	private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	/** Kieu du lieu cua cot. */
	public enum Kind { TEXT, INT, MONEY, PERCENT, DATETIME }

	/** Dinh nghia 1 cot: tieu de, kieu, ham lay gia tri tu dong du lieu. */
	public record Col<T>(String title, Kind kind, Function<T, Object> value) {
	}

	private ExcelExporter() {
	}

	public static <T> byte[] export(String title, String subtitle, List<Col<T>> cols, List<T> rows, T totalRow) {
		try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sh = wb.createSheet("Báo cáo");
			Font bold = wb.createFont();
			bold.setBold(true);
			Font titleFont = wb.createFont();
			titleFont.setBold(true);
			titleFont.setFontHeightInPoints((short) 15);

			CellStyle titleSt = wb.createCellStyle();
			titleSt.setFont(titleFont);
			CellStyle head = wb.createCellStyle();
			head.setFont(bold);
			head.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			head.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			head.setBorderBottom(BorderStyle.THIN);
			head.setWrapText(true);
			short moneyFmt = wb.createDataFormat().getFormat("#,##0");
			short pctFmt = wb.createDataFormat().getFormat("0.0\"%\"");
			CellStyle money = wb.createCellStyle();
			money.setDataFormat(moneyFmt);
			CellStyle pct = wb.createCellStyle();
			pct.setDataFormat(pctFmt);
			CellStyle totalText = wb.createCellStyle();
			totalText.setFont(bold);
			totalText.setBorderTop(BorderStyle.THIN);
			CellStyle totalMoney = wb.createCellStyle();
			totalMoney.cloneStyleFrom(money);
			totalMoney.setFont(bold);
			totalMoney.setBorderTop(BorderStyle.THIN);
			CellStyle totalPct = wb.createCellStyle();
			totalPct.cloneStyleFrom(pct);
			totalPct.setFont(bold);
			totalPct.setBorderTop(BorderStyle.THIN);

			int r = 0;
			Cell t = sh.createRow(r++).createCell(0);
			t.setCellValue(title);
			t.setCellStyle(titleSt);
			sh.addMergedRegion(new CellRangeAddress(0, 0, 0, Math.max(cols.size() - 1, 1)));
			sh.createRow(r++).createCell(0).setCellValue(subtitle + " · Xuất lúc " + LocalDateTime.now().format(TS));
			r++;
			Row h = sh.createRow(r++);
			for (int i = 0; i < cols.size(); i++) {
				Cell c = h.createCell(i);
				c.setCellValue(cols.get(i).title());
				c.setCellStyle(head);
			}
			int headerRow = r - 1;
			for (T row : rows) {
				write(sh.createRow(r++), cols, row, money, pct, null);
			}
			if (totalRow != null) {
				write(sh.createRow(r), cols, totalRow, totalMoney, totalPct, totalText);
			}
			for (int i = 0; i < cols.size(); i++) {
				try {
					sh.autoSizeColumn(i);
				} catch (RuntimeException | Error e) {
					// may chu khong co font (Docker toi gian) -> dung do rong mac dinh
					sh.setColumnWidth(i, 18 * 256);
				}
				sh.setColumnWidth(i, Math.min(Math.max(sh.getColumnWidth(i) + 512, 12 * 256), 60 * 256));
			}
			sh.createFreezePane(0, headerRow + 1);
			wb.write(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static <T> void write(Row x, List<Col<T>> cols, T row, CellStyle money, CellStyle pct, CellStyle text) {
		for (int i = 0; i < cols.size(); i++) {
			Col<T> col = cols.get(i);
			Object v = col.value().apply(row);
			Cell c = x.createCell(i);
			if (v == null) {
				if (text != null) {
					c.setCellStyle(text);
				}
				continue;
			}
			switch (col.kind()) {
			case INT -> c.setCellValue(((Number) v).doubleValue());
			case MONEY -> {
				c.setCellValue(v instanceof BigDecimal b ? b.doubleValue() : ((Number) v).doubleValue());
				c.setCellStyle(money);
			}
			case PERCENT -> {
				c.setCellValue(((Number) v).doubleValue());
				c.setCellStyle(pct);
			}
			case DATETIME -> c.setCellValue(v instanceof LocalDateTime d ? d.format(TS) : v.toString());
			default -> c.setCellValue(v.toString());
			}
			if (text != null && (col.kind() == Kind.TEXT || col.kind() == Kind.INT || col.kind() == Kind.DATETIME)) {
				c.setCellStyle(text);
			}
		}
	}
}
