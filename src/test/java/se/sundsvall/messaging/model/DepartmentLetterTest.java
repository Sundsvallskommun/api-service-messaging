package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class DepartmentLetterTest {

	private static final String DEPARTMENT = "department";
	private static final Count SNAIL_MAIL = Count.builder().build();
	private static final Count DIGITAL_MAIL = Count.builder().build();

	@Test
	void testConstructor() {
		final var bean = new DepartmentLetter(DEPARTMENT, SNAIL_MAIL, DIGITAL_MAIL);

		assertBean(bean);
	}

	@Test
	void testBuilder() {
		final var bean = DepartmentLetter.builder()
			.withDepartment(DEPARTMENT)
			.withDigitalMail(DIGITAL_MAIL)
			.withSnailMail(SNAIL_MAIL)
			.build();

		assertBean(bean);
	}

	private void assertBean(final DepartmentLetter bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.department()).isEqualTo(DEPARTMENT);
		assertThat(bean.digitalMail()).isEqualTo(DIGITAL_MAIL);
		assertThat(bean.snailMail()).isEqualTo(SNAIL_MAIL);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DepartmentLetter.builder().build()).hasAllNullFieldsOrProperties();
	}
}
