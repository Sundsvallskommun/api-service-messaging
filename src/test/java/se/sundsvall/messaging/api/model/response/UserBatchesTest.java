package se.sundsvall.messaging.api.model.response;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createBatch;
import static se.sundsvall.messaging.TestDataFactory.createPagingMetaData;

class UserBatchesTest {

	private static final PagingMetaData PAGING_META_DATA = createPagingMetaData();
	private static final List<Batch> USER_BATCHES = List.of(createBatch());

	@Test
	void userBatchesConstructor() {
		final var userBatches = new UserBatches(PAGING_META_DATA, USER_BATCHES);

		assertThat(userBatches).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(userBatches.metaData()).isEqualTo(PAGING_META_DATA);
		assertThat(userBatches.batches()).isEqualTo(USER_BATCHES);
		assertThat(userBatches).hasOnlyFields("metaData", "batches");
	}

	@Test
	void userBatchesBuilder() {
		final var userBatches = UserBatches.builder()
			.withMetaData(PAGING_META_DATA)
			.withBatches(USER_BATCHES)
			.build();

		assertThat(userBatches).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(userBatches.metaData()).isEqualTo(PAGING_META_DATA);
		assertThat(userBatches.batches()).isEqualTo(USER_BATCHES);
		assertThat(userBatches).hasOnlyFields("metaData", "batches");
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(UserBatches.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new UserBatches(null, null)).hasAllNullFieldsOrProperties();
	}
}
