package se.sundsvall.messaging;

public final class Constants {

	public static final String STATISTICS_PATH = "/{municipalityId}/statistics";

	public static final String CONVERSATION_HISTORY_PATH = "/{municipalityId}/conversation-history/{partyId}";

	public static final String STATISTICS_FOR_DEPARTMENTS_PATH = STATISTICS_PATH + "/departments";

	public static final String STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH = STATISTICS_FOR_DEPARTMENTS_PATH + "/{department}";

	public static final String BATCH_STATUS_PATH = "/{municipalityId}/status/batch/{batchId}";

	public static final String MESSAGE_STATUS_PATH = "/{municipalityId}/status/message/{messageId}";

	public static final String MESSAGE_AND_DELIVERY_PATH = "/{municipalityId}/message/{messageId}";

	public static final String DELIVERY_STATUS_PATH = "/{municipalityId}/status/delivery/{deliveryId}";

	public static final String USER_MESSAGES_PATH = "/{municipalityId}/users/{userId}/messages";

	public static final String MESSAGE_ATTACHMENT_PATH = "/{municipalityId}/messages/{messageId}/attachments/{fileName}";

	private Constants() {}

}
