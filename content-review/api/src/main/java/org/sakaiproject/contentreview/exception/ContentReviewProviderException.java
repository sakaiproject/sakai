package org.sakaiproject.contentreview.exception;

public class ContentReviewProviderException extends RuntimeException {

	private static final long serialVersionUID = -4280645805106323556L;

	public ContentReviewProviderException() {
		super();
	}

	public ContentReviewProviderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ContentReviewProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContentReviewProviderException(String message) {
		super(message);
	}

	public ContentReviewProviderException(Throwable cause) {
		super(cause);
	}

}
