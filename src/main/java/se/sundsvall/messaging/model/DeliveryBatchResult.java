package se.sundsvall.messaging.model;

import java.util.List;

import lombok.Builder;
import lombok.With;

@With
@Builder(setterPrefix = "with")
public record DeliveryBatchResult(String batchId, List<DeliveryResult> deliveries) { }