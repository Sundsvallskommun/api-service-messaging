package se.sundsvall.messaging.integration.db.projection.support;

import io.hypersistence.utils.hibernate.type.util.ClassImportIntegrator;
import java.util.List;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;

public class ClassImportIntegratorIntegratorProvider implements IntegratorProvider {

	@Override
	public List<Integrator> getIntegrators() {
		return List.of(new ClassImportIntegrator(List.of(StatsEntry.class)));
	}
}
