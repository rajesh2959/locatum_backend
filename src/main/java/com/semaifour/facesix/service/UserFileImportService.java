package com.semaifour.facesix.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.account.rest.UserAccountRestController;
import com.semaifour.facesix.account.role.RoleService;
import com.semaifour.facesix.beacon.rest.BeaconDeviceRestController;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.domain.Restponse;
import com.semaifour.facesix.util.ValidationUtil;

@Service
public class UserFileImportService {

	static Logger LOG = LoggerFactory.getLogger(BeaconDeviceRestController.class.getName());
	
	@Autowired
	RoleService roleService;
	
	@Autowired
	CustomerService customerService;
	
	@Autowired
	UserAccountService userAccountService;
	
	@Autowired
	UserAccountRestController userAccountRestController;
	
	@Autowired
	ValidationUtil validationUtil;
	
	DataFormatter dataTypeFormatter = new DataFormatter();
	boolean success = true;
	int code 		= 200;
	String body 	= "File found was empty !! Add users to be saved.";
	boolean isSaved = false;
	boolean isFileEmpty   = true;
	int countSkippedUsers = 0;
	
	private final String HEADER_STR_0 = "CustomerID";
	private final String HEADER_STR_1 = "FirstName";
	private final String HEADER_STR_2 = "LastName";
	private final String HEADER_STR_3 = "Email";
	private final String HEADER_STR_4 = "MobileNumber";
	private final String HEADER_STR_5 = "Designation";
	private final String HEADER_STR_6 = "Role";
	
	
	/**
	 * Used to validate file format and returns boolean value 
	 * 
	 * @param multiFile
	 * @throws IOException
	 * @return isValidFileFormat boolean value
	 */
	public boolean fileValidation(MultipartFile multiFile) throws IOException {

		/*
		 * content-type for CSV  --> text/plain (or) application/vnd.ms-excel (or) text/x-csv
		 * content-type for XLS  --> application/vnd.ms-excel
		 * content-type for XLSX --> application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
		 */
		boolean isValidFileFormat = false;

		if (multiFile.getContentType().equals("text/x-csv")
				|| multiFile.getContentType().equals("application/vnd.ms-excel")
				|| multiFile.getContentType().equals("text/plain")) {
			isValidFileFormat = true;
		} else if (multiFile.getContentType().equals("application/vnd.ms-excel")) {
			isValidFileFormat = true;
		} else if (multiFile.getContentType()
				.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
			isValidFileFormat = true;
		} else if (multiFile.getContentType().equals("application/octet-stream") && multiFile.getSize() == 0){
			isValidFileFormat = true;			// Edge Case happens for Blank CSV file
		}

		return isValidFileFormat;
	}
	
	/**
	 * Used to create workbook for excel file
	 * 
	 * @param multiFile
	 * @throws IOException
	 * @return newly created workbook
	 * @throws InvalidFormatException 
	 * @throws EncryptedDocumentException 
	 */
	public Workbook workBookCreation(MultipartFile multiFile) throws IOException, EncryptedDocumentException, InvalidFormatException {
		Workbook workbook = null;
		if (multiFile.getContentType().equals("application/vnd.ms-excel") || multiFile.getContentType()
				.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
			InputStream inputStream = multiFile.getInputStream();
			workbook = WorkbookFactory.create(inputStream);
		}
		return workbook;
	}
	
	/**
	 * Used to process excel files
	 * 
	 * @param workbook
	 * @param cid
	 * @return responseBody with code,bodyMessage
	 * @throws IOException 
	 */
	public Restponse<String> excelFileProcessing(Workbook workbook, String cid, HttpServletRequest request, HttpServletResponse response) throws IOException {

		countSkippedUsers = 0;
		isSaved 	= false;
		isFileEmpty = true;
		Sheet dataSheet = workbook.getSheetAt(0);
		int rowCount = dataSheet.getLastRowNum() - dataSheet.getFirstRowNum() + 1; // getting total row counts
		int rowIterator = 0;
		// check if header exists
		if (rowCount != 0) {
			if (isHeaderExists(dataSheet)) { 									   
				rowIterator = 1;
			}
			for (; rowIterator <= rowCount; rowIterator++) {
				String customerId 	= null;
				String customerName	= null;
				String fname 		= null;
				String lname 		= null;
				String mailId 		= null;
				String mobileNum 	= null;
				String designation 	= null;
				String role		 	= null;
				String password 	= "Quberc0mm!";
				String mailAlert    = "false";
				String smsAlert   	= "false";
				
				if (dataSheet.getRow(rowIterator) == null) {	//row is empty
					continue;
				}
				isFileEmpty = false;
				Row row     = dataSheet.getRow(rowIterator);
				
				/*
				 * Get the customer name and find the customer by customerName
				 */
				Cell cusName_cell = row.getCell(0); 			
				if (!(cusName_cell == null || dataTypeFormatter.formatCellValue(cusName_cell).trim().isEmpty())) {
					customerName = cusName_cell.toString();
					List<Customer> customerList = customerService.findByCustomerName(customerName);

					if (customerList != null && customerList.size()>0) {
						customerId = customerList.get(0).getPkid();
					}
				}
				
				/*
				 * Get the role of the user
				 * If role is valid 
				 * 	1) If it is not a superadmin and customer ID is null, Skip that user 
				 * If role is not valid 
				 * 	1) If customer ID is not null, Set role as appadmin to that customer
				 * 	2) If customer ID is null, Skip that user
				 */
				Cell cell    = row.getCell(6);	
				if (!(cell == null || dataTypeFormatter.formatCellValue(cell).trim().isEmpty())) {

					role = cell.toString();

					if (roleService.isRoleAvailable(role)) {
						if (!role.equalsIgnoreCase("superadmin") && customerId == null) {
							countSkippedUsers++;
							continue;
						}
					} else if(customerId != null){
						role = "appadmin";
					} else {
						countSkippedUsers++;
						continue;
					}

				} else if(customerId != null){
					role = "appadmin";
				}
				else {
					countSkippedUsers++;
					continue;
				}
				
				//getting first name of the user
				cell = row.getCell(1);				
				if (!(cell == null || dataTypeFormatter.formatCellValue(cell).trim().isEmpty())) {
					fname = cell.toString();
				} else {
					countSkippedUsers++;
					continue;
				}
				
				//getting last name of the user	
				cell = row.getCell(2);			
				if (!(cell == null || dataTypeFormatter.formatCellValue(cell).trim().isEmpty())) {
					lname = cell.toString();
				} else {
					countSkippedUsers++;
					continue;
				}
				
				//getting Email ID of the user
				cell = row.getCell(3);				
				if (!(cell == null || dataTypeFormatter.formatCellValue(cell).trim().isEmpty())) {
					mailId = cell.toString();
					if(!validationUtil.isValidMailId(mailId)){
						countSkippedUsers++;
						continue;
					} 
				} else {
					countSkippedUsers++;
					continue;
				}
				
				//getting mobile number of the user	
				cell = row.getCell(4);			
				if (!(cell == null || dataTypeFormatter.formatCellValue(cell).trim().isEmpty())) {
					double mob  = cell.getNumericCellValue();
					long mobnum = (new Double(mob)).longValue();
					mobileNum   = String.valueOf(mobnum);
					if (!validationUtil.isValidMobileNumber(mobileNum)) {
						countSkippedUsers++;
						continue;
					}
				} 
				
				//getting designation of the user
				cell = row.getCell(5);				
				if (!(cell == null || dataTypeFormatter.formatCellValue(cell).trim().isEmpty())) {
					designation = cell.toString();
				} 
				UserAccount user = userAccountService.findOneByEmail(mailId);
				if (user == null) {
					user = creatingNewUser(customerId, fname, lname, mailId, mobileNum, designation, role, password,
							mailAlert, smsAlert);
					userAccountRestController.save(user, request, response);
					isSaved = true;
				} else {
					countSkippedUsers++;
				}
			}
			if (countSkippedUsers > 0) {
				body 	= countSkippedUsers + " Invalid Users that were found are skipped ";
			} else if (countSkippedUsers == 0 && isSaved) {
				body 	= "Users had been imported and saved successfully";
			} else if(countSkippedUsers == 0 && isFileEmpty){
				body 	= "File found was empty !! Add users to be saved.";
				success = false;
				code 	= 412;
			}
		} else {
			body = "File found was empty !! Add users to be saved.";
			success = false;
			code = 412;
		}
		return new Restponse<String>(success, code, body);
	}
	
	/**
	 * Used to check if Header is present for Excel files
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
				if (header.equalsIgnoreCase(HEADER_STR_0) || header.equalsIgnoreCase(HEADER_STR_1)
						|| header.equalsIgnoreCase(HEADER_STR_2) || header.equalsIgnoreCase(HEADER_STR_3)
						|| header.equalsIgnoreCase(HEADER_STR_4) || header.equalsIgnoreCase(HEADER_STR_5)
						|| header.equalsIgnoreCase(HEADER_STR_6)) {
					isHeader = true;
				}
			}
		}
		return isHeader;
	}
	
	/**
	 * Used to check if Header is present for CSV files
	 * 
	 * @param header
	 * @return true if header exists else false
	 */
	public boolean isHeaderExists(String header) {
		boolean isHeader = false;
		if(header != null) {
			header = header.replaceAll("\\s", "");
		}
		if (header != null && header.equalsIgnoreCase(HEADER_STR_0) || header.equalsIgnoreCase(HEADER_STR_1)
				|| header.equalsIgnoreCase(HEADER_STR_2) || header.equalsIgnoreCase(HEADER_STR_3)
				|| header.equalsIgnoreCase(HEADER_STR_4) || header.equalsIgnoreCase(HEADER_STR_5)
				|| header.equalsIgnoreCase(HEADER_STR_6)) {
			isHeader = true;
		}
		return isHeader;
	}
	
	/**
	 * Used to create a User
	 * @param customerId
	 * @param fname
	 * @param lname
	 * @param mailId
	 * @param mobileNum
	 * @param designation
	 * @param role
	 * @param password
	 * @param mailAlert
	 * @param smsAlert
	 * @return newly created UserAccount 
	 */
	public UserAccount creatingNewUser(String customerId, String fname, String lname, String mailId, String mobileNum,
			String designation, String role, String password, String mailAlert, String smsAlert) {

		UserAccount user = new UserAccount();
		/*
		 * If role is not superadmin then set the customer ID
		 */
		if(!role.equalsIgnoreCase("superadmin")){
			user.setCustomerId(customerId);
		}
		user.setFname(fname);
		user.setLname(lname);
		user.setEmail(mailId);
		user.setPhone(mobileNum);
		user.setDesignation(designation);
		user.setRole(role);
		user.setPassword(password);
		user.setMailalert(mailAlert);
		user.setIsSmsalert(smsAlert);

		return user;
	}
	

	/**
	 * Used to process .csv files
	 * 
	 * @param content
	 * @param cid
	 * @return
	 * @throws IOException 
	 */
	public Restponse<String> csvFileProcessing(MultipartFile multiFile, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		InputStream inputStream = multiFile.getInputStream();
		Reader reader 			= new InputStreamReader(inputStream, StandardCharsets.UTF_8.name());
		CSVFormat format 		= CSVFormat.RFC4180.withHeader(HEADER_STR_0, HEADER_STR_1, 
															   HEADER_STR_2, HEADER_STR_3,
															   HEADER_STR_4, HEADER_STR_5, HEADER_STR_6)
										.withSkipHeaderRecord(false).withDelimiter(',').withIgnoreEmptyLines();
		CSVParser parser 			 = format.parse(reader);
		Iterator<CSVRecord> iterator = parser.iterator();

		countSkippedUsers 	= 0;
		isSaved 		  	= false;
		isFileEmpty 		= true;
		boolean isHeader    = true;
		String password 	= "Quberc0mm!";
		String mailAlert 	= "false";
		String smsAlert 	= "false";

		while (iterator.hasNext()) {
			CSVRecord record = iterator.next();
			
			if(isHeader && isHeaderExists(record.get(0))){
				isHeader = false;
				continue;
			}

			String customerId 	= null;
			String customerName = null;
			String fname 		= null;
			String lname 		= null;
			String mailId 		= null;
			String mobileNum 	= null;
			String designation 	= null;
			String role 		= null;
			isHeader 			= false;
			isFileEmpty 		= false;
			int length = record.size(); // getting no of columns
			int i = 0;

			/*
			 * Get the customer name and find the customer by customerName
			 */

			customerName = record.get(i);
			if (!customerName.isEmpty()) {
				List<Customer> customerList = customerService.findByCustomerName(customerName);

				if (customerList != null && customerList.size() > 0) {
					customerId = customerList.get(0).getPkid();
				}
			}
			i = i + 1;
			// getting first name of the user
			if (i < length) {
				fname = record.get(i).trim();
				if (fname == null || fname.isEmpty()) {
					countSkippedUsers++;
					continue;
				}
			} else {
				countSkippedUsers++;
				continue;
			}
			i = i + 1;
			// getting last name of the user
			if (i < length) {
				lname = record.get(i).trim();
				if (lname == null || lname.isEmpty()) {
					countSkippedUsers++;
					continue;
				}
			} else {
				countSkippedUsers++;
				continue;
			}
			i = i + 1;
			// getting Email ID of the user
			if (i < length) {
				mailId = record.get(i).trim();
				if (mailId == null || mailId.isEmpty() || !validationUtil.isValidMailId(mailId)) {
					countSkippedUsers++;
					continue;
				}
			} else {
				countSkippedUsers++;
				continue;
			}
			i = i + 1;
			// getting mobile number of the user
			if (i < length) {
				mobileNum = record.get(i).trim();
				if (mobileNum == null || mobileNum.isEmpty() || !validationUtil.isValidMobileNumber(mobileNum)) {
					countSkippedUsers++;
					continue;
				}
			} 
			i = i + 1;
			// getting designation of the user
			if (i < length) {
				designation = record.get(i).trim();
				if (designation == null || designation.isEmpty()) {
					countSkippedUsers++;
					continue;
				}
			} 

			/*
			 * Get the role of the user
			 * If role is valid 
			 * 	1) If it is not a superadmin and customer ID is null, Skip that user 
			 * If role is not valid 
			 * 	1) If customer ID is not null, Set role as appadmin to that customer
			 * 	2) If customer ID is null, Skip that user
			 */
			
			i = i + 1;
			if (i < length) {
				role = record.get(i).trim();

				if (role != null && !role.isEmpty() && roleService.isRoleAvailable(role)) {
					if (!role.equalsIgnoreCase("superadmin") && customerId == null) {
						countSkippedUsers++;
						continue;
					}
				} else if (customerId != null) {
					role = "appadmin";
				} else {
					countSkippedUsers++;
					continue;
				}
			} else if (customerId != null) {
				role = "appadmin";
			} else {
				countSkippedUsers++;
				continue;
			}

			UserAccount user = userAccountService.findOneByEmail(mailId);
			if (user == null) {
				user = creatingNewUser(customerId, fname, lname, mailId, mobileNum, designation, role, password,
						mailAlert, smsAlert);
				userAccountRestController.save(user, request, response);
				isSaved = true;
			} else {
				countSkippedUsers++;
			}
		}
		if (countSkippedUsers > 0) {
			body 	= countSkippedUsers + " Invalid Users that were found are skipped ";
		} else if (countSkippedUsers == 0 && isSaved) {
			body 	= "Users had been imported and saved successfully";
		} else if (countSkippedUsers == 0 && isFileEmpty) {
			code 	= 412;
			success = false;
			body 	= " File found was empty !! Add users to be saved.";
		}
		return new Restponse<String>(success, code, body);
	}
	

}
