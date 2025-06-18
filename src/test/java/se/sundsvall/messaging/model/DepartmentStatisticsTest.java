package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class DepartmentStatisticsTest {

	private static final String ORIGIN = "origin";
	private static final List<DepartmentLetter> DEPARTMENT_LETTERS = List.of(DepartmentLetter.builder().build());

	@Test
	void testConstructor() {
		final var bean = new DepartmentStatistics(ORIGIN, DEPARTMENT_LETTERS);

		assertBean(bean);
	}

	@Test
	void testBuilder() {
		final var bean = DepartmentStatistics.builder()
			.withDepartmentLetters(DEPARTMENT_LETTERS)
			.withOrigin(ORIGIN)
			.build();

		assertBean(bean);
	}

	private void assertBean(final DepartmentStatistics bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.departmentLetters()).isEqualTo(DEPARTMENT_LETTERS);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DepartmentStatistics.builder().build()).hasAllNullFieldsOrProperties();
	}
}
