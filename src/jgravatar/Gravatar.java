package jgravatar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.model.atom.Entry;

/**
 * A gravatar is a dynamic image resource that is requested from the
 * gravatar.com server. This class calculates the gravatar url and fetches
 * gravatar images. See http://en.gravatar.com/site/implement/url .
 * 
 * This class is thread-safe, Gravatar objects can be shared.
 * 
 * Usage example:
 * 
 * <code>
 * Gravatar gravatar = new Gravatar();
 * gravatar.setSize(50);
 * gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
 * gravatar.setDefaultImage(GravatarDefaultImage.IDENTICON);
 * String url = gravatar.getUrl("iHaveAn@email.com");
 * byte[] jpg = gravatar.download("info@ralfebert.de");
 * </code>
 */
public final class Gravatar {

	Logger LOG = Logger.getLogger(Gravatar.class.getName());

	private final static int DEFAULT_SIZE = 80;
	private final static String GRAVATAR_URL = "http://www.gravatar.com/avatar/";
	private final static String GRAVATAR_BASIC_URL = "http://www.gravatar.com/";
	private static final GravatarRating DEFAULT_RATING = GravatarRating.GENERAL_AUDIENCES;
	private static final GravatarDefaultImage DEFAULT_DEFAULT_IMAGE = GravatarDefaultImage.HTTP_404;

	private int size = DEFAULT_SIZE;
	private GravatarRating rating = DEFAULT_RATING;
	private GravatarDefaultImage defaultImage = DEFAULT_DEFAULT_IMAGE;

	public Gravatar(){
		setSize(50);
		setRating(GravatarRating.GENERAL_AUDIENCES);
		setDefaultImage(GravatarDefaultImage.IDENTICON);
	}

	/**
	 * Specify a gravatar size between 1 and 512 pixels. If you omit this, a
	 * default size of 80 pixels is used.
	 */
	public void setSize(int sizeInPixels) {
		this.size = sizeInPixels;
	}

	/**
	 * Specify a rating to ban gravatar images with explicit content.
	 */
	public void setRating(GravatarRating rating) {
		this.rating = rating;
	}

	/**
	 * Specify the default image to be produced if no gravatar image was found.
	 */
	public void setDefaultImage(GravatarDefaultImage defaultImage) {
		this.defaultImage = defaultImage;
	}

	/**
	 * Returns the Gravatar URL for the given email address.
	 */
	public String getUrl(String email) {

		// hexadecimal MD5 hash of the requested user's lowercased email address
		// with all whitespace trimmed
		String emailHash = DigestUtils.md5Hex(email.toLowerCase().trim());
		String params = formatUrlParameters();
		return GRAVATAR_URL + emailHash + ".jpg" + params;
	}

	public Map<String,String> getProfile(String email) throws IOException, JSONException{
		LOG.info("Entering getProfile: " + email);
		String imageUrl = null;
		String name = null;
		String profileUrl = null;
		JSONObject json = null;
		JSONArray entryArr = null;
		JSONObject entry = null;
		String emailHash = DigestUtils.md5Hex(email.toLowerCase().trim());

		URL url = new URL(GRAVATAR_BASIC_URL + emailHash + ".json");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("GET");
		connection.connect();

		StringBuilder sb = new StringBuilder();
		try{
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				
			} else {
				LOG.log(Level.WARNING, "Response code: " + connection.getResponseCode());

			}
		}catch(java.io.IOException e){
			LOG.log(Level.WARNING, e.getMessage());
		}finally{
			connection.disconnect();
		}
		LOG.info("json profile for " + email + " : " + sb.toString());
		if(sb.length() > 2){
			json = new JSONObject(sb.toString());
			entryArr = new JSONArray(json.getString("entry")); 
			entry = entryArr.getJSONObject(0);
			imageUrl = entry.getString("thumbnailUrl");
			name = entry.getString("displayName") + "<" + email + ">";
			profileUrl = entry.getString("profileUrl");
		}else{
			imageUrl = getUrl(email);
			name = email;
			profileUrl = "";
		}

		Map<String,String> profile = new HashMap<String,String>();
		profile.put("imageUrl", imageUrl);
		profile.put("name", name);
		profile.put("profileUrl", profileUrl);
		return profile;
	}

	/**
	 * Downloads the gravatar for the given URL using Java {@link URL} and
	 * returns a byte array containing the gravatar jpg, returns null if no
	 * gravatar was found.
	 */
	public byte[] download(String email) throws GravatarDownloadException {
		InputStream stream = null;
		try {
			URL url = new URL(getUrl(email));
			stream = url.openStream();
			return IOUtils.toByteArray(stream);
		} catch (FileNotFoundException e) {
			return null;
		} catch (Exception e) {
			throw new GravatarDownloadException(e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	private String formatUrlParameters() {
		List<String> params = new ArrayList<String>();

		if (size != DEFAULT_SIZE)
			params.add("s=" + size);
		if (rating != DEFAULT_RATING)
			params.add("r=" + rating.getCode());
		if (defaultImage != GravatarDefaultImage.GRAVATAR_ICON)
			params.add("d=" + defaultImage.getCode());

		if (params.isEmpty())
			return "";
		else
			return "?" + StringUtils.join(params.iterator(), "&");
	}

}
