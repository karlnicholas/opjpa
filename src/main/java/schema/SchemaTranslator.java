package schema;

import java.io.IOException;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import opca.model.OpinionBase;
import opca.model.OpinionKey;
import opca.model.OpinionSummary;
import opca.model.SlipOpinion;
import opca.model.StatuteCitation;
import opca.model.StatuteKey;
import opca.model.User;
import opca.model.Role;

public class SchemaTranslator {
	public static void main(String[] args) throws IOException {

		Class<?>[] entityClasses = { 
			OpinionBase.class, 
			SlipOpinion.class, 
			OpinionSummary.class, 
			OpinionKey.class,
			StatuteCitation.class, 
			StatuteKey.class, 
			User.class, 
			Role.class 
		};

		MetadataSources metadata = new MetadataSources(new StandardServiceRegistryBuilder()
			.applySetting("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect")
			// .applySetting("hibernate.implicit_naming_strategy",
			// "opca.xxx.util.ImprovedImplicitNamingStrategy")
			.applySetting("hibernate.physical_naming_strategy", "opca.ejb.util.ImprovedNamingStrategy")
			.build()
		);

		// [...] adding annotated classes to metadata here...
		for (Class<?> clazz : entityClasses)
			metadata.addAnnotatedClass(clazz);

		SchemaExport export = new SchemaExport((MetadataImplementor) metadata.buildMetadata())
			// .setHaltOnError( haltOnError )
			.setOutputFile("db-schema.sql")
			.setDelimiter(";");

		export.create(true, false);

	}

}
