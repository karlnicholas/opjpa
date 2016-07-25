package schema;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class SchemaGenerator {

    public static void main(String[] args) throws Exception {
    	new SchemaGenerator().run();
    }
    private void run() throws Exception {

        File f = new File("c:/users/karl/scsb/src/main/java");
        String directory = f.getAbsoluteFile().toString();

        String packageName[] = { "opca.model"};

        generate(directory, packageName);

    }


    @SuppressWarnings("rawtypes")
    private List<Class> getClasses(String packageName) throws Exception {
        File directory = null;
        try {
            ClassLoader cld = getClassLoader();
            URL resource = getResource(packageName, cld);
            directory = new File(resource.getFile());
        } catch (NullPointerException ex) {
            throw new ClassNotFoundException(packageName + " (" + directory + ") does not appear to be a valid package");
        }
        return collectClasses(packageName, directory);
    }

    private ClassLoader getClassLoader() throws ClassNotFoundException {
        ClassLoader cld = Thread.currentThread().getContextClassLoader();
        if (cld == null) {
            throw new ClassNotFoundException("Can't get class loader.");
        }
        return cld;
    }

    private URL getResource(String packageName, ClassLoader cld) throws ClassNotFoundException {
        String path = packageName.replace('.', '/');
        URL resource = cld.getResource(path);
        if (resource == null) {
            throw new ClassNotFoundException("No resource for " + path);
        }
        return resource;
    }

    @SuppressWarnings("rawtypes")
    private List<Class> collectClasses(String packageName, File directory) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
        if (directory.exists()) {
            String[] files = directory.list();
            for (String file : files) {
                if (file.endsWith(".class")) {
                    // removes the .class extension
                    classes.add(Class.forName(packageName + '.' + file.substring(0, file.length() - 6)));
                }
            }
        } else {
            throw new ClassNotFoundException(packageName + " is not a valid package");
        }
        return classes;
    }

	private void generate(String directory, String[] packagesName) throws Exception {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("hibernate.hbm2ddl.auto", "create");
		settings.put("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
		settings.put("hibernate.id.new_generator_mappings", "true");
		settings.put("hibernate.implicit_naming_strateg", "opca.ejb.util.ImprovedImplicitNamingStrategy");
		settings.put("hibernate.physical_naming_strategy", "opca.ejb.util.ImprovedNamingStrategy");

        
	    MetadataSources metadata = new MetadataSources(
	            new StandardServiceRegistryBuilder()
	                    .applySettings(settings)
	                    .build());

	    for (String packageName : packagesName) {
	        System.out.println("packageName: " + packageName);
	        for (Class clazz : getClasses(packageName)) {
	        	System.out.println("Class: " + clazz);
	            metadata.addAnnotatedClass(clazz);
	        }
	    }

	    SchemaExport export = new SchemaExport(
	            (MetadataImplementor) metadata.buildMetadata()
	    );

	    export.setDelimiter(";");
	    export.setOutputFile("c:/users/karl/scsb/opjpa/" + "db-schema.sql");
	    export.setFormat(true);
	    export.execute(true, false, false, false);
	}	

}