package opjpa;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import opca.model.User;

public class TestEmailTransform {

	public static void main(String[] args) throws Exception {
		new TestEmailTransform().run();
	}
	private void run() throws Exception {
		User user = new User();
		user.setEmail("test@test.com");
		user.setFirstName("First");
		user.setLastName("Lastname");
		user.setOptoutKey("optoutkey");
		TransformerFactory tf = TransformerFactory.newInstance();  
		JAXBContext jc = JAXBContext.newInstance(User.class);
		// jaxbContext is a JAXBContext object from which 'o' is created.
		JAXBSource source = new JAXBSource(jc, user);
		// set up XSLT transformation
		InputStream is = getClass().getResourceAsStream("/xsl/welcome.xsl");
		StreamSource streamSource = new StreamSource(is);
		StringWriter htmlContent = null;
		try {
			htmlContent = new StringWriter();
			synchronized(this) {
				Transformer t = tf.newTransformer(streamSource);
				// run transformation
				t.transform(source, new StreamResult(htmlContent));
			}
		} catch (TransformerException e) {
			throw new RuntimeException(e); 
		} finally {
			htmlContent.close();
		}
		System.out.println(htmlContent);
/*
		MimeMessage message = new MimeMessage(mailSession);

		Multipart multiPart = new MimeMultipart("alternative");

		// Sets up the contents of the email message
		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setText("");

		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(htmlContent.toString(), "text/html; charset=utf-8");

		multiPart.addBodyPart(textPart); // <-- first
		multiPart.addBodyPart(htmlPart); // <-- second

		message.setContent(multiPart);
		message.setFrom(new InternetAddress("no-reply@op-opca.b9ad.pro-us-east-1.openshiftapps.com"));
		message.setSubject("Welcome to Court Opinions");
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
		// This is not mandatory, however, it is a good
		// practice to indicate the software which
		// constructed the message.
		message.setHeader("X-Mailer", "Court Opinions");

		// Adjust the date of sending the message
		message.setSentDate(new Date());

		// Sends the email
		Transport.send(message);
		
		userService.setWelcomedTrue(user);
*/		
	}
}
