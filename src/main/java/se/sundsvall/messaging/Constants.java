package se.sundsvall.messaging;

public final class Constants {

	public static final String STATISTICS_PATH = "/{municipalityId}/statistics";

	public static final String CONVERSATION_HISTORY_PATH = "/{municipalityId}/conversation-history/{partyId}";

	public static final String STATISTICS_FOR_DEPARTMENTS_PATH = STATISTICS_PATH + "/departments";

	public static final String STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH = STATISTICS_FOR_DEPARTMENTS_PATH + "/{department}";

	public static final String BATCH_STATUS_PATH = "/{municipalityId}/status/batch/{batchId}";

	public static final String MESSAGES_STATUS_PATH = "/{municipalityId}/status/messages/{messageId}";

	public static final String MESSAGES_AND_DELIVERY_PATH = "/{municipalityId}/messages/{messageId}";

	public static final String MESSAGES_AND_DELIVERY_METADATA_PATH = "/{municipalityId}/messages/{messageId}/metadata";

	public static final String DELIVERY_STATUS_PATH = "/{municipalityId}/status/delivery/{deliveryId}";

	public static final String USER_MESSAGE_PATH = "/{municipalityId}/users/{userId}/messages/{messageId}";

	public static final String USER_MESSAGES_PATH = "/{municipalityId}/users/{userId}/messages";

	public static final String MESSAGES_ATTACHMENT_PATH = "/{municipalityId}/messages/{messageId}/attachments/{fileName}";

	public static final String X_ISSUER_HEADER_KEY = "x-issuer";

	public static final String X_SENT_BY_HEADER_KEY = "X-Sent-By";

	public static final String X_ORIGIN_HEADER_KEY = "x-origin";

	public static final String OEP_INSTANCE_EXTERNAL = "EXTERNAL";

	public static final String OEP_INSTANCE_INTERNAL = "INTERNAL";

	private Constants() {}

}
