package schema;

import java.util.HashMap;
import java.util.List;

import javax.persistence.spi.PersistenceUnitTransactionType;

import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.cfg.Configuration;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class PersistenceParser {
	public static void main(String[] args) {

	    PersistenceXmlParser parser = new PersistenceXmlParser(new ClassLoaderServiceImpl(), PersistenceUnitTransactionType.RESOURCE_LOCAL);
	    List<ParsedPersistenceXmlDescriptor> allDescriptors = parser.doResolve(new HashMap<>());

	    for (ParsedPersistenceXmlDescriptor descriptor : allDescriptors) {

	        Configuration cfg = new Configuration();
	        cfg.setProperty("hibernate.hbm2ddl.auto", "create");
	        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
	        cfg.setProperty("hibernate.id.new_generator_mappings", "true");

	        List<String> managedClassNames = descriptor.getManagedClassNames();
	        for (String className : managedClassNames) {
	            try {
	                cfg.addAnnotatedClass(Class.forName(className));
	            } catch (ClassNotFoundException e) {
	                System.out.println("Class not found: " + className);
	            }
	        }

	        SchemaExport export = new SchemaExport(cfg);
	        export.setDelimiter(";");
	        export.setOutputFile("C:/users/karl/scsb/opjpa/" + descriptor.getName() + "_create_schema.sql");
	        export.setFormat(true);
	        export.execute(true, false, false, false);

	    }
	}
}
