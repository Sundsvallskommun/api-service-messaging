package se.sundsvall.messaging.integration.businessrules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.businessrules.DistributionRuleEvaluationResponse;

@ExtendWith(MockitoExtension.class)
class BusinessRulesIntegrationTests {

    @Mock
    private BusinessRulesClient mockClient;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private BusinessRulesIntegrationMapper mockMapper;

    @InjectMocks
    private BusinessRulesIntegration integration;

    @Test
    void test_evaluate() {
        when(mockClient.evaluate(ArgumentMatchers.any(String.class)))
            .thenReturn(new ResponseEntity<>(new DistributionRuleEvaluationResponse(), HttpStatus.OK));

        var response = integration.evaluate("someRule");
        assertThat(response.getResult()).isNotNull();

        verify(mockClient, times(1)).evaluate(any(String.class));
        verify(mockMapper, times(1)).toDistributionRuleEvaluationDto(any(DistributionRuleEvaluationResponse.class));
    }

    @Test
    void test_evaluate_empty_response() {
        when(mockClient.evaluate(ArgumentMatchers.any(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThatExceptionOfType(ThrowableProblem.class).isThrownBy(() -> integration.evaluate("someRule"));

        verify(mockClient, times(1)).evaluate(any(String.class));
        verify(mockMapper, never()).toDistributionRuleEvaluationDto(any(DistributionRuleEvaluationResponse.class));
    }

}
