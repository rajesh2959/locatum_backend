package com.semaifour.facesix.beacon.util;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.beacon.rest.BeaconDeviceRestController;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.domain.Restponse;

@Service
public class BeaconFileImportUtil {

	@Autowired
	BeaconDeviceService beaconDeviceService;

	@Autowired
	private BeaconService beaconService;

	DataFormatter dataTypeFormatter = new DataFormatter();
	boolean success = true;
	int code = 200;
	String body = "File found was empty !! Add tagids to checkout.";
	boolean isValidFileFormat = false;
	boolean hasInValidTagId = false;
	boolean isAlreadyExists = false;
	int countSkippedTags = 0;
	boolean isImported = false;
	private final String TAGID_VALIDATION_STR = "^([0-9A-Fa-f]{2}[:]){5}([0-9A-Fa-f]{2})$";
	private final String HEADER_STR = "tagid";

	static Logger LOG = LoggerFactory.getLogger(BeaconDeviceRestController.class.getName());

	/**
	 * Used to validate file format and returns boolean value
	 * 
	 * @param multiFile
	 * @throws IOException
	 * @return isValidFileFormat boolean value
	 */
	public boolean fileValidation(MultipartFile multiFile) throws IOException {
		boolean isValidFileFormat = false;
		if (multiFile.getContentType().equals("text/csv")) {
			isValidFileFormat = true;
		} else if (multiFile.getContentType().equals("application/vnd.ms-excel")) {
			isValidFileFormat = true;
		} else if (multiFile.getContentType()
				.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
			isValidFileFormat = true;
		}
		return isValidFileFormat;
	}

	/**
	 * Used to create workbook for excel file
	 * 
	 * @param multiFile
	 * @throws IOException
	 * @return newly created workbook
	 */
	public Workbook workBookCreation(MultipartFile multiFile) throws IOException {
		Workbook workbook = null;
		if (multiFile.getContentType().equals("application/vnd.ms-excel")) {
			workbook = new HSSFWorkbook(multiFile.getInputStream());
		} else if (multiFile.getContentType()
				.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
			workbook = new XSSFWorkbook(multiFile.getInputStream());
		}
		return workbook;
	}

	/**
	 * Used to validate macAddress
	 * 
	 * @param macAddress
	 * @return isValid or Not
	 */
	public boolean isValidMacAddress(String macAddress) {
		Pattern pattern = Pattern.compile(TAGID_VALIDATION_STR);
		Matcher matcher = pattern.matcher(macAddress);
		if (matcher.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * Used to process .xls,.xlsx files
	 * 
	 * @param workbook
	 * @param cid
	 * @return responseBody
	 */
	public Restponse<String> excelFileProcessing(Workbook workbook, String cid) {

		countSkippedTags = 0;
		hasInValidTagId = false;
		isImported = false;
		isAlreadyExists = false;
		Sheet dataSheet = workbook.getSheetAt(0);
		int rowCount = dataSheet.getLastRowNum() - dataSheet.getFirstRowNum() + 1;
		if (rowCount != 0) {
			int rowIterator = 0;
			if (isHeaderExists(dataSheet)) {
				rowIterator = 1;
			}
			for (; rowIterator <= rowCount; rowIterator++) {
				String tagId = null;
				String assignedTo = tagId;
				String tagType = "Contractor";
				String tagModel = "Neck";
				String refTxpwr = "-59";
				if (dataSheet.getRow(rowIterator) == null) {						//row is empty
					continue;
				}
				Row row 	= dataSheet.getRow(rowIterator);
				int rowNum  = row.getLastCellNum();  // validate empty rows
				
				if (rowNum <= 0) {
					LOG.info("empty rows found!!!!!! " + rowNum);
					continue;
				}
				
				Cell cell = row.getCell(0);
				if (!(cell == null || dataTypeFormatter.formatCellValue(cell).trim().isEmpty())) {  //getting first col i.e., tagid
					tagId = row.getCell(0).toString();
					if (!isValidMacAddress(tagId)) {												//validating tagid
						hasInValidTagId = true;
						countSkippedTags++;
						continue;
					}
				} else {																			//first tag id col is empty
					hasInValidTagId = true;
					countSkippedTags++;
					continue;
				}
				cell = row.getCell(1);																//getting other columns respectively
				if (!(cell == null || dataTypeFormatter.formatCellValue(cell).trim().isEmpty())) {
					assignedTo = row.getCell(1).toString();
				} else {
					assignedTo = tagId;
				}
				cell = row.getCell(2);
				if (!(cell == null || dataTypeFormatter.formatCellValue(cell).trim().isEmpty())) {
					tagType = row.getCell(2).toString();
				}
				cell = row.getCell(3);
				if (!(cell == null || dataTypeFormatter.formatCellValue(cell).trim().isEmpty())) {
					tagModel = row.getCell(3).toString();
				}
				cell = row.getCell(4);
				if (!(cell == null)) {
					refTxpwr = String.valueOf((int) cell.getNumericCellValue());
				}
					tagId = tagId.toUpperCase();
					Beacon beacon = beaconService.findOneByMacaddr(tagId);
					if (beacon == null) {
						creatingNewBeacon(tagId, cid, assignedTo, tagType, tagModel, refTxpwr);
						isImported = true;
					} else {																	//already exists device uid
						isAlreadyExists = true;
						countSkippedTags++;
				}
			}
			body = bodyMessage(countSkippedTags, isImported, hasInValidTagId, isAlreadyExists);
		} else {
			body = "File found was empty !! Add tagids to checkout.";
			success = false;
			code = 412;
		}
		return new Restponse<String>(success, code, body);
	}

	/**
	 * Used to create new Beacon
	 * 
	 * @param tagId
	 * @param cid
	 * @param assignedTo
	 * @param tagType
	 * @param tagModel
	 * @param refTxpwr
	 * @return newly created Beacon
	 */

	public Beacon creatingNewBeacon(String tagId, String cid, String assignedTo, String tagType, String tagModel,
			String refTxpwr) {
		Beacon beacon = new Beacon();
		beacon.setMacaddr(tagId);
		beacon.setCid(cid);
		beacon.setAssignedTo(assignedTo);
		beacon.setTag_type(tagType);
		beacon.setTagmodel(tagModel);
		beacon.setReftxpwr(refTxpwr);
		beacon.setStatus(Beacon.STATUS.checkedin.name());
		beacon.setUpdatedstatus(Beacon.STATUS.checkedin.name());
		beacon.setModifiedOn(new Date());
		beacon.setTemplate(null);
		beaconService.save(beacon, false);
		return beacon;
	}

	/**
	 * Used to check if Header is present
	 * 
	 * @param dataSheet
	 * @return true if header exists else false
	 */
	public boolean isHeaderExists(Sheet dataSheet) {
		boolean isHeader = false;
		Row firstRow = dataSheet.getRow(0);
		if (firstRow != null) {
			Cell getHeader = firstRow.getCell(0);
			String header = null;
			if (!(getHeader == null || dataTypeFormatter.formatCellValue(getHeader).trim().isEmpty())) {
				header = getHeader.toString().replaceAll("\\s+", "");
				if (header.equalsIgnoreCase(HEADER_STR)) {
					isHeader = true;
				}
			}
		}
		return isHeader;
	}

	/**
	 * Used to process .csv files
	 * 
	 * @param content
	 * @param cid
	 * @return
	 */
	public Restponse<String> csvFileProcessing(String content, String cid) {

		content = StringUtils.trim(content);
		String rowSplit[] = content.split("\n");
		hasInValidTagId = false;
		countSkippedTags = 0;
		isAlreadyExists = false;
		isImported = false;
		for (String rowSplitUp : rowSplit) {					

			String tagId = null;
			String assignedTo = tagId;
			String tagType = "Contractor";
			String tagModel = "Neck";
			String refTxpwr = "-59";
			String csvSplit[] = rowSplitUp.split(",");

			if (csvSplit.length == 0) {														//empty row
				continue;
			}
			String getHeader = csvSplit[0];													//Header validation
			String header = getHeader.replaceAll("\\s+", "");
			if (header.equalsIgnoreCase(HEADER_STR)) {
				continue;
			}
			int length = csvSplit.length;											//getting no of colummns
			int i = 0;
			tagId = csvSplit[i];
			if (tagId.isEmpty()) {													//if tagId is empty
				countSkippedTags++;
				hasInValidTagId = true;
				continue;
			}
			if (!isValidMacAddress(tagId)) {
				hasInValidTagId = true;
				countSkippedTags++;
				continue;
			}
			i++;
			if (i < length && !csvSplit[i].isEmpty()) {
				assignedTo = csvSplit[i];
			}
			i++;
			if (i < length && !csvSplit[i].isEmpty()) {
				tagType = csvSplit[i];
			}
			i++;
			if (i < length && !csvSplit[i].isEmpty()) {
				tagModel = csvSplit[i];
			}
			i++;
			if (i < length && !csvSplit[i].isEmpty()) {
				refTxpwr = csvSplit[i];
			}
				Beacon beacon = beaconService.findOneByMacaddr(tagId);
				if (beacon == null) {
					creatingNewBeacon(tagId, cid, assignedTo, tagType, tagModel, refTxpwr);
					isImported = true;
				} else {
					isAlreadyExists = true;											//alreadyExists device uid
					countSkippedTags++;
				}
		}
		body = bodyMessage(countSkippedTags, isImported, hasInValidTagId, isAlreadyExists);
		return new Restponse<String>(success, code, body);
	}

	/**
	 * Used to send Back proper Response
	 * 
	 * @param isSaved
	 * @param isInvalidDevice
	 * @param countSkippedDevices
	 * @return
	 */
	public String bodyMessage(int countSkippedDevices, boolean isImported, boolean hasInValidTagId,
			boolean isAlreadyExists) {
		body = "File found was empty !! Add tagids to checkout.";
		if (isAlreadyExists && hasInValidTagId) {
			body = countSkippedTags + " Tag Ids that already existed and invalid Tag Id that were found are skipped";
		} else if (isAlreadyExists) {
			body = countSkippedTags + " Tag Id that already existed are skipped ";
		} else if (hasInValidTagId) {
			body = countSkippedTags + " Invalid Tag Id that were found are skipped ";
		} else if (isImported) {
			body = "Tag(s) have been imported successfully and are present in the \"Available Tags\" table ";
		}

		return body;
	}

}
