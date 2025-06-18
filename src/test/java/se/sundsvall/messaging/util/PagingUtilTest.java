package se.sundsvall.messaging.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Sort;
import se.sundsvall.messaging.api.model.response.Batch;

class PagingUtilTest {

	@ParameterizedTest
	@MethodSource("toPageArgumentProvider")
	void toPage(int page, int limit, List<Batch> matches, int expectedCurrentPage, int expectedTotalPages) {

		final var bean = PagingUtil.toPage(page, limit, matches);

		assertThat(bean.getPageable().getPageNumber()).isEqualTo(expectedCurrentPage - 1); // As current page is zero based
		assertThat(bean.getPageable().getPageSize()).isEqualTo(limit);
		assertThat(bean.getPageable().getSort()).isEqualTo(Sort.unsorted());

		assertThat(bean.getNumber()).isEqualTo(expectedCurrentPage - 1); // As current page is zero based
		assertThat(bean.getSize()).isEqualTo(limit);
		assertThat(bean.getTotalPages()).isEqualTo(expectedTotalPages);
		assertThat(bean.getTotalElements()).isEqualTo(matches.size());
		assertThat(bean.getSort()).isEqualTo(Sort.unsorted());
	}

	private static Stream<Arguments> toPageArgumentProvider() {
		final var matches = List.of(
			Batch.builder().build(),
			Batch.builder().build(),
			Batch.builder().build(),
			Batch.builder().build(),
			Batch.builder().build());

		return Stream.of(
			Arguments.of(1, 1, matches, 1, 5),
			Arguments.of(2, 1, matches, 2, 5),
			Arguments.of(1, 2, matches, 1, 3),
			Arguments.of(5, 1, matches, 5, 5),
			Arguments.of(1, 5, matches, 1, 1),
			Arguments.of(2, 5, matches, 2, 1));
	}

	@Test
	void toPageRequest() {
		final var page = RandomUtils.secure().randomInt(1, 10000);
		final var limit = RandomUtils.secure().randomInt(1, 10000);

		final var bean = PagingUtil.toPageRequest(page, limit);

		assertThat(bean.getPageNumber()).isEqualTo(page - 1); // as PageRequest is zero based
		assertThat(bean.getPageSize()).isEqualTo(limit);
		assertThat(bean.getSort()).isEqualTo(Sort.unsorted());
	}
}
