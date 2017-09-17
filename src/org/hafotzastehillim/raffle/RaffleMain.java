package org.hafotzastehillim.raffle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RaffleMain {

	static HashMap<Integer, Integer> times = new HashMap<>();
	static HashMap<Integer, Entry> tickets = new HashMap<>();
	static List<Integer> winners = new ArrayList<>();
	static Set<Entry> uniqueWinners = new HashSet<>();

	static {
		times.put(1, 1);
		times.put(2, 2);
		times.put(3, 5);
		times.put(4, 6);
		times.put(5, 8);
		times.put(6, 10);
		times.put(7, 11);
		times.put(8, 12);
		times.put(9, 15);
		times.put(10, 18);
		times.put(11, 20);
		times.put(12, 21);
		times.put(13, 22);
		times.put(14, 25);
	}

	public static void main(String[] args) throws InvalidFormatException, IOException {
		File input = new File("C:/users/yossel/desktop/Shavous Data.xlsx");

		Scanner scn = new Scanner(new File("C:/users/yossel/desktop/winning tickets.txt"));
		while (scn.hasNextLine()) {
			winners.add(scn.nextInt());
		}
		System.out.println(winners);
		scn.close();

		File f = new File("C:/users/yossel/desktop/Shavuos Winners.xlsx");

		try (Workbook inputBook = new XSSFWorkbook(input)) {// ; Workbook outputBook = new XSSFWorkbook(output)) {

			int timesColumn = 8;
			int unique = 0;

			for (int sheetNum = 0; sheetNum < inputBook.getNumberOfSheets(); sheetNum++) {
				Sheet s = inputBook.getSheetAt(sheetNum);

				for (int i = 1; i < s.getPhysicalNumberOfRows(); i++) {
					if (s.getRow(i).getCell(16) == null)
						continue;

					Cell c = s.getRow(i).getCell(timesColumn);

					int repeat = c == null ? 1
							: times.get((int) c.getNumericCellValue()) == null ? 30
									: times.get((int) c.getNumericCellValue());

					for (int j = 0; j < repeat; j++) {
						tickets.put(unique++, new Entry(sheetNum, i));
					}

				}
			}

			int listIndex = 0;
			while (uniqueWinners.size() < 150)
				uniqueWinners.add(tickets.get(winners.get(listIndex++)));

			try (Workbook winnerBook = new XSSFWorkbook()) {
				Sheet s = winnerBook.createSheet();

				DataFormatter formatter = new DataFormatter();

				for (Entry e : uniqueWinners) {
					Row r = s.createRow(s.getPhysicalNumberOfRows());
					Row source = inputBook.getSheetAt(e.sheet).getRow(e.row);
					for(int cell = 0; cell < source.getPhysicalNumberOfCells(); cell++) {
						Cell data = r.createCell(cell);
						data.setCellValue(formatter.formatCellValue(source.getCell(cell)));
					}
				}

				winnerBook.write(new FileOutputStream(f));
			}
		}

	}

	private static class Entry {
		int sheet;
		int row;

		Entry(int s, int r) {
			sheet = s;
			row = r;
		}

		public boolean equals(Object other) {
			Entry o = (Entry) other;
			return o.sheet == sheet && o.row == row;
		}
	}

}
