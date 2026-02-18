package se.sundsvall.messaging.model;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InternalDeliveryBatchResultTest {

	private static final String BATCH_ID = "batchId";
	private static final List<InternalDeliveryResult> DELIVERIES = List.of(InternalDeliveryResult.builder().build());
	private static final String MUNICIPALITY_ID = "municipalityId";

	@Test
	void testConstructor() {
		final var bean = new InternalDeliveryBatchResult(BATCH_ID, DELIVERIES, MUNICIPALITY_ID);

		assertBean(bean);
	}

	@Test
	void testBuilder() {
		final var bean = InternalDeliveryBatchResult.builder()
			.withBatchId(BATCH_ID)
			.withDeliveries(DELIVERIES)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		assertBean(bean);
	}

	private void assertBean(final InternalDeliveryBatchResult bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.batchId()).isEqualTo(BATCH_ID);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(bean.deliveries()).isEqualTo(DELIVERIES);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(InternalDeliveryBatchResult.builder().build()).hasAllNullFieldsOrProperties();
	}
}
