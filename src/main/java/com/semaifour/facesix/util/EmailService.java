package com.semaifour.facesix.util;



import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;

@Service
public class EmailService {

	static Logger LOG = LoggerFactory.getLogger(EmailService.class.getName());
	
	private static String classname = EmailService.class.getName();
	
	@Autowired
	private UserAccountService userAccountService;
	
	@Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;
    
    @Autowired
	private CustomerService customerService;
    
    @Autowired
	private SiteService siteService;
	
	@Autowired
	private PortionService portionService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private CustomerUtils customerUtils;
	
	@Value("${spring.mail.host}")
	private String mailHost;
	
	@Value("${spring.mail.port}")
	private String mailPort;
	
	@Value("${spring.mail.username}")
	private String mailUserName;
	
	@Value("${spring.mail.password}")
	private String mailPassword;
	
    public void sendSimpleMessage(MailEntity mail) throws MessagingException, IOException {
    	
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

       //helper.addAttachment("logo.png", new ClassPathResource("static/qcom/img/logo.png"));

        Context context = new Context();
        context.setVariables(mail.getPayload());
        
        String html = templateEngine.process("crash-dump-email-notify-event", context);

        helper.setTo(mail.getTo());
        helper.setText(html, true);
        helper.setSubject(mail.getSubject());
        helper.setFrom(mail.getFrom());

        
        emailSender.send(message);
    }

    /**
     * Used to sent email alert for notify crash dump
     * @param uid
     * @param fileName
     * @return
     */

	public boolean notifyCrashDumpAlert(String uid,String cid, String alias,String sid,String spid,String fileName) throws IOException {
		try {
			
			final String ismailalert = "true";
			final String role 		 = UserAccount.ROLE.superadmin.name();
			final String subject 	 = "Crash Dump Notification";
		    
			 MimeMessage message = emailSender.createMimeMessage();
		        MimeMessageHelper helper = new MimeMessageHelper(message,MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
		                StandardCharsets.UTF_8.name());
		    
		        final String rootPath 		 = request.getContextPath();
				
			List<UserAccount> userAccountList = userAccountService.findByRoleAndIsMailAlert(role, ismailalert);

			if (CollectionUtils.isNotEmpty(userAccountList)) {

				String venue = null;
				String floor = null;
				
				if (sid != null) {
					Site site = siteService.findById(sid);
					if (site != null) {
						venue = site.getUid();
					}
				}
				
				if (spid != null) {
					Portion portion = portionService.findById(spid);
					if (portion != null) {
						floor = portion.getUid();
					}
				}
				
				String customerName = "-";
				Customer customer = customerService.findById(cid);
				if (customer != null) {
					customerName = customer.getCustomerName();
				}
				
				for (UserAccount userAccoun : userAccountList) {

					try {

						
			        	String name  	 = userAccoun.getFname().substring(0, 1).toUpperCase() +userAccoun.getFname().substring(1).toLowerCase();;
						String toEmailId = userAccoun.getEmail();
						
						final String fileDownloadApi = "/rest/site/portion/networkdevice/GW_Device_crash_dump_dowmload?filename="+fileName;
						final String appUrl 		 = customerUtils.cloudUrl()+ rootPath +fileDownloadApi;
						
						
						MailEntity mail = new MailEntity();
						mail.setFrom(mailUserName);
						mail.setTo(toEmailId);
						mail.setSubject(subject);

						Map<String, Object> model = new HashMap<String, Object>();
						model.put("customerName", customerName);
						model.put("userName", 	name);
						model.put("alias",      alias);
						model.put("uid",	   uid);
						model.put("venue",     venue);
						model.put("floor",     floor);
						model.put("signature", customerUtils.cloudUrl());
						model.put("url", 		appUrl);
						
						mail.setPayload(model);
						
						Context context = new Context();
				        context.setVariables(mail.getPayload());
				        
				        String html = templateEngine.process("crash-dump-email-notify-event", context);
				        
			        	helper.setTo(mail.getTo());
						helper.setText(html, true);
				        helper.setSubject(mail.getSubject());
				        helper.setFrom(mail.getFrom());
				        
				        emailSender.send(message);
				        
					} catch (MessagingException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
		
	}

	
	public void sendMailToUsers(StringBuilder mailBody, List<UserAccount> useraccountlist) {
		try {

			String cid  = useraccountlist.get(0).getCustomerId();
			
			Customer cx = customerService.findById(cid);

			String emailId    = null;
			Boolean enablelog = true;

			if (cx != null) {
				if (cx.getLogs() == null || cx.getLogs().equals("false")) {
					enablelog = false;
				}
			}

			StringBuilder username = new StringBuilder();

			String subject = "Qubercomm Notification For Tags Not in Assigned Location";

			if (mailBody.toString().contains("detailed list of inactive tags")) {
				subject = "Qubercomm Notification For Inactive Tags";
			}else if(mailBody.toString().contains("GeoFence")) {
				subject = "Geofence Alert";
			}

			for (UserAccount user : useraccountlist) {
				username = new StringBuilder();
				username.append("<div style=\"padding:0px\">")
						.append("Hi "+user.getFname()+" "+user.getLname()+", <br/> <br/>");
				emailId = user.getEmail();
				customerUtils.logs(enablelog, classname, " mail sent to email id " + emailId);
				customerUtils.customizeSupportEmail(cid, emailId, subject, username.toString()+mailBody.toString(), null);
			}

		} catch (Exception e) {
			LOG.info("iam sendMailToUsers " +e.getMessage());
		}
	}
}
