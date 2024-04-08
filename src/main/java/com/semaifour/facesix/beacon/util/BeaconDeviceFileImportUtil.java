package com.semaifour.facesix.beacon.util;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import com.semaifour.facesix.beacon.rest.BeaconDeviceRestController;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.mongo.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.spring.SpringComponentUtils;

@Service
public class BeaconDeviceFileImportUtil {

	@Autowired
	BeaconDeviceService beaconDeviceService;

	DataFormatter dataTypeFormatter = new DataFormatter();
	boolean isSaved = true;
	int countSkippedDevices = 0;
	boolean isInvalidDevice = false;
	boolean isImported = false;
	int code = 200;
	boolean success = true;
	String body = "File found was empty !! ";
	private final String GATEWAY_MAC_VALIDATION_STR = "^([0-9A-Fa-f]{2}[:]){5}([0-9A-Fa-f]{2})$";
	private final String HEADER_STR = "location";
	private final String HEADER_STR1 = "Uid";

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
	 * @return boolean value of valid macAddress or not
	 */
	public boolean isValidMacAddress(String macAddress) {

		Pattern pattern = Pattern.compile(GATEWAY_MAC_VALIDATION_STR);
		Matcher matcher = pattern.matcher(macAddress);
		if (matcher.matches()) {
			return true;
		}

		return false;
	}

	/**
	 * Used to process excel files
	 * 
	 * @param workbook
	 * @param cid
	 * @return responseBody with code,bodyMessage
	 */
	public Restponse<String> excelFileProcessing(Workbook workbook, String cid, String createdBy) {

		countSkippedDevices = 0;
		isInvalidDevice = false;
		isImported = false;
		isSaved = true;
		Sheet dataSheet = workbook.getSheetAt(0);
		int rowCount = dataSheet.getLastRowNum() - dataSheet.getFirstRowNum() + 1; // getting total row counts
		int rowIterator = 0;
		if (rowCount != 0) {
			if (isHeaderExists(dataSheet)) { 											// check if header exists
				rowIterator = 1;
			}
			for (; rowIterator <= rowCount; rowIterator++) {
				String deviceUid = null;
				String location = deviceUid;
				if (dataSheet.getRow(rowIterator) == null) { 						   // empty row
					continue;
				}
				Row currentRow = dataSheet.getRow(rowIterator);
				Cell firstCell = currentRow.getCell(0);
				if (!(firstCell == null || dataTypeFormatter.formatCellValue(firstCell).trim().isEmpty())) {		//checking first cell
					deviceUid = currentRow.getCell(0).toString();
					if (isValidMacAddress(deviceUid)) {																//if valid mac address in firstcell
						Cell secondCell = currentRow.getCell(1);
						if (!(secondCell == null || dataTypeFormatter.formatCellValue(secondCell).trim().isEmpty())) {  //first col uid secon col location
							location = currentRow.getCell(1).toString();
						} else {
							location = deviceUid;
						}
					} else {
						Cell secondCell = currentRow.getCell(1);													//first col with no valid mac address
						if (!(secondCell == null || dataTypeFormatter.formatCellValue(secondCell).trim().isEmpty())) {
							deviceUid = currentRow.getCell(1).toString();
							if (isValidMacAddress(deviceUid)) {														//second cell validation
								firstCell = currentRow.getCell(0);
								if (!(firstCell == null
										|| dataTypeFormatter.formatCellValue(firstCell).trim().isEmpty())) {
									location = currentRow.getCell(0).toString();									//second col uid first col location
								} else {
									location = deviceUid;															//second col uid first col empty
								}
							} else {																				//first col location secon col invalid uid
								countSkippedDevices++;
								isInvalidDevice = true;
								continue;
							}
						} else {																				  //first col invalid uid second col empty
							countSkippedDevices++;
							isInvalidDevice = true;
							continue;
						}
					}
				} else {																						//first col empty 
					Cell secondCell = currentRow.getCell(1);
					if (!(secondCell == null || dataTypeFormatter.formatCellValue(secondCell).trim().isEmpty())) {
						deviceUid = currentRow.getCell(1).toString();
						if (isValidMacAddress(deviceUid)) {														//second cell uid validation
								location = deviceUid;															//first col empty second col valid uid
						} else {
							countSkippedDevices++;																//first col empty second col invalid uid
							isInvalidDevice = true;
							continue;
						}
					} else {
						continue;																				//empty row
					}
				}
				BeaconDevice beaconDevice = null;
				isSaved = isDeviceAlreadyExists(deviceUid);
				if (isSaved) {
					beaconDevice = creatingNewDevice(deviceUid, cid, createdBy, location);
					isImported = true;
					beaconDevice = beaconDeviceService.save(beaconDevice, true);
				}
			}
			body = bodyMessage(isSaved, isInvalidDevice, countSkippedDevices, isImported);
		} else {
			code = 412;
			success = false;
			body = "File found was empty !! ";

		}
		return new Restponse<String>(success, code, body);
	}

	/**
	 * Used to check if Header is present in excelFIle
	 * 
	 * @param dataSheet
	 * @return true if header exists else false
	 */
	public boolean isHeaderExists(Sheet dataSheet) {
		boolean isHeader = false;
		Row firstRow = dataSheet.getRow(0); // Header validation
		if (firstRow != null) {
			Cell getHeaderCell = firstRow.getCell(0);
			String header = null;
			isSaved = true;
			if (!(getHeaderCell == null || dataTypeFormatter.formatCellValue(getHeaderCell).trim().isEmpty())) {
				header = getHeaderCell.toString().replaceAll("\\s+", "");
				if (header.equalsIgnoreCase(HEADER_STR) || header.equalsIgnoreCase(HEADER_STR1)) {
					isHeader = true;
				}
			}
		}
		return isHeader;
	}

	/**
	 * Used to check deviceUid already exists
	 * 
	 * @param deviceUid
	 * @return isSaved i.e.,if device is new device return false
	 *//*
		*/
	public boolean isDeviceAlreadyExists(String deviceUid) {
		BeaconDevice beaconDevice = beaconDeviceService.findOneByUid(deviceUid);
		if (beaconDevice != null) {
			String status = BeaconDevice.STATUS.REGISTERED.name();
			String beaconDeviceStatus = beaconDevice.getStatus();
			if (status.equalsIgnoreCase(beaconDeviceStatus)) { 						// checking if the device is registered
				isSaved = true;
			} else { 
				isSaved = false;
				countSkippedDevices++;
			}
		} else { 																	// if the deviceUid is new
			beaconDevice = new BeaconDevice();
			beaconDevice.setUid(deviceUid);
			beaconDevice.setCreatedOn(new Date());
		}

		return isSaved;
	}

	/**
	 * Used to create new BeaconDevice
	 * 
	 * @param deviceUid
	 * @param cid
	 * @param createdBy
	 * @param location
	 * @return BeaconDevice
	 */
	public BeaconDevice creatingNewDevice(String deviceUid, String cid, String createdBy, String location) {

		BeaconDevice beaconDevice = null;
		String conf = SpringComponentUtils.getApplicationMessages()
				.getMessage("facesix.beacon.device.template.default");
		String template = String.valueOf(conf);

		beaconDevice = new BeaconDevice();
		beaconDevice.setUid(deviceUid);
		beaconDevice.setCid(cid);
		beaconDevice.setStatus(BeaconDevice.STATUS.CONFIGURED.name());
		beaconDevice.setState("active");
		beaconDevice.setTemplate(template);
		beaconDevice.setConf(template);
		beaconDevice.setType("receiver");
		beaconDevice.setIp("0.0.0.0");
		beaconDevice.setKeepAliveInterval("3");
		beaconDevice.setTlu(1);
		beaconDevice.setSource("qubercomm");
		beaconDevice.setTypefs("sensor");
		beaconDevice.setDescription("bukimported");
		beaconDevice.setName(location);

		beaconDevice.setModifiedBy(createdBy);
		beaconDevice.setModifiedOn(new Date());
		beaconDevice = beaconDeviceService.save(beaconDevice, true);

		return beaconDevice;
	}

	/**
	 * Used to return proper responseBody
	 * 
	 * @param isSaved
	 * @param isInvalidDevice
	 * @param countSkippedDevices
	 * @return
	 */
	public String bodyMessage(boolean isSaved, boolean isInvalidDevice, int countSkippedDevices, boolean isImported) {

		body = "File found was empty !! ";
		if ((!isSaved) && isInvalidDevice) {
			body = countSkippedDevices
					+ " Gateway MAC addresses that already existed  and Invalid Gateway MAC address were found are skipped";
		} else if (!isSaved) {
			body = countSkippedDevices + " Gateway MAC addresses that already existed are skipped";
		} else if (isInvalidDevice) {
			body = countSkippedDevices + " Invalid gateway MAC addresses were found that are skipped.";
		} else if (isImported) {
			body = "Gateways has been imported successfully";
		}
		return body;
	}

	/**
	 * Used to process .csv files
	 * 
	 * @param content
	 * @param cid
	 * @return
	 */
	public Restponse<String> csvFileProcessing(String content, String cid, String createdBy) {

		countSkippedDevices = 0;
		isInvalidDevice = false;
		isImported = false;
		isSaved = true;
		String rowSplit[] = content.split("\n");

		if (rowSplit.length != 0) {
			String location = null;
			String deviceUid = location;
			for (String rowSplitUp : rowSplit) {
				if (rowSplitUp.isEmpty()) {
					continue;
				}
				String csvSplit[] = rowSplitUp.split(",");
				if (csvSplit.length == 0) {
					continue;
				}
				if (csvSplit.length == 1) {													//id only one column
					deviceUid = csvSplit[0];
					if (!isValidMacAddress(deviceUid)) {									//the column with no valid uid
						countSkippedDevices++;
						isInvalidDevice = true;
						continue;
					} else {																//valid uid with no location
						location = csvSplit[0];
						deviceUid = csvSplit[0];
					}
				} else if (csvSplit.length > 1) {											//2 columns present
					deviceUid = csvSplit[0];
					if (isValidMacAddress(deviceUid)) {										//first col uid second col location
						location = csvSplit[1];
					} else {
						if (isValidMacAddress(csvSplit[1])) {								//second col uid and first col location
							if (!csvSplit[0].isEmpty()) {
								location = csvSplit[0];
							} else {
								location = csvSplit[1];
							}
							deviceUid = csvSplit[1];
						} else {															//invalid uid in second col
							countSkippedDevices++;
							isInvalidDevice = true;
							continue;
						}
					}
				}
				BeaconDevice beaconDevice = null;
				isSaved = isDeviceAlreadyExists(deviceUid);
				if (isSaved) { 
					beaconDevice = creatingNewDevice(deviceUid, cid, createdBy, location);
					isImported = true;
					beaconDevice = beaconDeviceService.save(beaconDevice, true);
				}
			}
			body = bodyMessage(isSaved, isInvalidDevice, countSkippedDevices, isImported);
		} else {
			code = 412;
			success = false;
			body = " File found was empty !!";
		}
		return new Restponse<String>(success, code, body);
	}

}
