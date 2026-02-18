package se.sundsvall.messaging.api.model.response;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.Count;

import static se.sundsvall.messaging.test.assertj.Assertions.assertThat;

class DepartmentStatsTest {

	@Test
	void testBuilderAndGetters() {
		var digitalMail = new Count(1, 1);
		var sms = new Count(2, 2);
		var snailmail = new Count(3, 3);
		var origin = "origin";
		var department = "department";

		final var departmentStats = DepartmentStats.builder()
			.withDigitalMail(digitalMail)
			.withSms(sms)
			.withSnailMail(snailmail)
			.withOrigin(origin)
			.withDepartment(department)
			.build();

		assertThat(departmentStats).isNotNull();
		assertThat(departmentStats.digitalMail()).isEqualTo(digitalMail);
		assertThat(departmentStats.sms()).isEqualTo(sms);
		assertThat(departmentStats.snailMail()).isEqualTo(snailmail);
		assertThat(departmentStats.origin()).isEqualTo(origin);
		assertThat(departmentStats.department()).isEqualTo(department);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DepartmentStats.builder().build()).hasAllNullFieldsOrProperties();
	}
}
