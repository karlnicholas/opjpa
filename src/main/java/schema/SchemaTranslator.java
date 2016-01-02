package schema;

import java.io.*;

import opinions.model.OpinionKey;
import opinions.model.SlipOpinion;
import opinions.model.OpinionBase;
import opinions.model.OpinionSummary;
import opinions.model.StatuteCitation;
import opinions.model.StatuteKey;

import org.hibernate.cfg.*;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.*;

public class SchemaTranslator {
	private Configuration config = null;

	public SchemaTranslator() {
		config = new Configuration();
	}

	public SchemaTranslator setDialect(String dialect) {
		config.setProperty(AvailableSettings.DIALECT, dialect);
		return this;
	}

	/**
	 * Method determines classes which will be used for DDL generation. 
	 * @param annotatedClasses - entities annotated with Hibernate annotations.
	 */
	public SchemaTranslator addAnnotatedClasses(Class<?>[] annotatedClasses) {
		for (Class<?> clazz : annotatedClasses)
			config.addAnnotatedClass(clazz);
		return this;
	}

	/**
	 * Method performs translation of entities in table schemas.
	 * It generates 'CREATE' and 'DELETE' scripts for the Hibernate entities.
	 * Current implementation involves usage of {@link #write(FileOutputStream, String[], Formatter)} method.
	 * @param outputStream - stream will be used for *.sql file creation.
	 * @throws IOException
	 */
	public SchemaTranslator translate(FileOutputStream outputStream) throws IOException {
		Dialect requiredDialect = Dialect.getDialect(config.getProperties());
		String[] query = null;

		query = config.generateDropSchemaScript(requiredDialect);
		write(outputStream, query, FormatStyle.DDL.getFormatter());

		query = config.generateSchemaCreationScript(requiredDialect);
		write(outputStream, query, FormatStyle.DDL.getFormatter());

		return this;
	}

	/**
	 * Method writes line by line DDL scripts in the output stream.
	 * Also each line logs in the console.
	 * @throws IOException
	 */
	private void write(FileOutputStream outputStream, String[] lines, Formatter formatter) 
			throws IOException {
		String tempStr = null;

		for (String line : lines) {
			tempStr = formatter.format(line)+";";
			System.out.println(tempStr);
			outputStream.write(tempStr.getBytes());
		}
	}

	public static void main(String[] args) throws IOException {
		SchemaTranslator translator = new SchemaTranslator();
		Class<?>[] entityClasses = {
				OpinionBase.class, 
				SlipOpinion.class, 
				OpinionSummary.class, 
				OpinionKey.class, 
				StatuteCitation.class, 
				StatuteKey.class
			};

//		translator.setDialect("org.hibernate.dialect.HSQLDialect")
//		translator.setDialect("org.hibernate.dialect.PostgreSQL82Dialect")
		
		translator.setDialect("org.hibernate.dialect.PostgreSQLDialect")
			.addAnnotatedClasses(entityClasses)
			.translate(new FileOutputStream(new File("db-schema.sql")));

	}

}