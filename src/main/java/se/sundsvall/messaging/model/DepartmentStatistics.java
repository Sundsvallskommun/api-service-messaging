package se.sundsvall.messaging.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder(setterPrefix = "with")
@Schema(name = "DepartmentStatistics")
public record DepartmentStatistics(
        
        @JsonProperty("ORIGIN")
        String origin,
        @JsonProperty("DEPARTMENT_STATISTICS")
        List<DepartmentLetter> departmentLetters) {
}
