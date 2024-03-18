package tukano.api;

/**
 * Represents a Short video uploaded by an user.
 * 
 * A short has an unique shortId and is owned by a given user; 
 * Comprises of a short video, stored as a binary blob at some bloburl;.
 * A post also has a number of likes, which can increase or decrease over time. It is the only piece of information that is mutable.
 * A short is timestamped when it is created.
 *
 */
public class Short {
	
	String shortId;
	String ownerId;
	String blobUrl;
	long timestamp;
	int totalLikes;

	public Short() {}
}