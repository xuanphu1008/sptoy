package com.toyland.media;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;
import com.toyland.common.exception.ApiException;
import com.toyland.common.util.TextUtils;

/**
 * Luu anh len Cloudinary (dich vu luu tru anh ben thu 3) qua REST Upload API, ky SHA-1 theo tai lieu Cloudinary.
 * Dung khi trien khai len cloud (Render): o dia cua may chu mien phi bi xoa moi lan khoi dong lai, con anh tren
 * Cloudinary thi giu vinh vien. Chua cau hinh -> FileStorageService luu vao thu muc uploads/ nhu cu.
 * <p>
 * Cau hinh bang bien moi truong (khong can sua application.properties):
 * CLOUDINARY_URL=cloudinary://API_KEY:API_SECRET@CLOUD_NAME (chuoi Cloudinary cap san), hoac rieng le
 * APP_CLOUDINARY_CLOUD_NAME / APP_CLOUDINARY_API_KEY / APP_CLOUDINARY_API_SECRET. Thu muc goc: APP_CLOUDINARY_FOLDER (mac dinh sptoy).
 */
@Component
public class CloudinaryUploader {

	private static final Logger log = LoggerFactory.getLogger(CloudinaryUploader.class);

	private final String cloudName;
	private final String apiKey;
	private final String apiSecret;
	private final String rootFolder;
	private final RestClient client;

	public CloudinaryUploader(@Value("${CLOUDINARY_URL:}") String cloudinaryUrl,
			@Value("${app.cloudinary.cloud-name:}") String cloudName,
			@Value("${app.cloudinary.api-key:}") String apiKey,
			@Value("${app.cloudinary.api-secret:}") String apiSecret,
			@Value("${app.cloudinary.folder:sptoy}") String rootFolder) {
		String cn = cloudName.trim(), key = apiKey.trim(), secret = apiSecret.trim();
		if (!TextUtils.isBlank(cloudinaryUrl) && (cn.isEmpty() || key.isEmpty() || secret.isEmpty())) {
			// cloudinary://API_KEY:API_SECRET@CLOUD_NAME
			try {
				URI u = URI.create(cloudinaryUrl.trim());
				String[] cred = u.getRawUserInfo().split(":", 2);
				key = cred[0];
				secret = cred[1];
				cn = u.getHost();
			} catch (RuntimeException e) {
				log.warn("CLOUDINARY_URL khong dung dinh dang cloudinary://API_KEY:API_SECRET@CLOUD_NAME");
			}
		}
		this.cloudName = cn;
		this.apiKey = key;
		this.apiSecret = secret;
		this.rootFolder = rootFolder.trim().replaceAll("^/+|/+$", "");
		SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
		rf.setConnectTimeout(10_000);
		rf.setReadTimeout(60_000);
		this.client = RestClient.builder().requestFactory(rf).build();
		log.info("Luu anh: {}", isEnabled() ? "Cloudinary (cloud " + this.cloudName + ", thu muc " + this.rootFolder + ")"
				: "o dia may chu (thu muc uploads/)");
	}

	public boolean isEnabled() {
		return !cloudName.isEmpty() && !apiKey.isEmpty() && !apiSecret.isEmpty();
	}

	/** Chu ky Cloudinary: SHA-1 hex cua "a=1&b=2" (tham so xep theo ten, bo file/api_key) noi them api_secret. */
	public static String sign(Map<String, String> params, String apiSecret) {
		StringBuilder sb = new StringBuilder();
		new TreeMap<>(params).forEach((k, v) -> {
			if (v != null && !v.isEmpty()) {
				if (sb.length() > 0) {
					sb.append('&');
				}
				sb.append(k).append('=').append(v);
			}
		});
		try {
			byte[] d = MessageDigest.getInstance("SHA-1").digest((sb + apiSecret).getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(d);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/** Tai anh len thu muc {rootFolder}/{folder}, tra ve link https cong khai. */
	public String upload(byte[] bytes, String filename, String folder) {
		String targetFolder = rootFolder.isEmpty() ? folder : rootFolder + "/" + folder;
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
		Map<String, String> signed = new TreeMap<>();
		signed.put("folder", targetFolder);
		signed.put("timestamp", timestamp);

		MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
		form.add("file", new ByteArrayResource(bytes) {
			@Override
			public String getFilename() {
				return filename;
			}
		});
		form.add("api_key", apiKey);
		form.add("timestamp", timestamp);
		form.add("folder", targetFolder);
		form.add("signature", sign(signed, apiSecret));
		try {
			JsonNode res = client.post().uri("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload")
					.contentType(MediaType.MULTIPART_FORM_DATA).body(form).retrieve().body(JsonNode.class);
			String url = res == null ? "" : res.path("secure_url").asText("");
			if (url.isEmpty()) {
				throw new ApiException(HttpStatus.BAD_GATEWAY, "UPLOAD_FAILED", "Cloudinary không trả về link ảnh");
			}
			return url;
		} catch (RestClientException e) {
			log.warn("Upload Cloudinary loi: {}", e.getMessage());
			throw new ApiException(HttpStatus.BAD_GATEWAY, "UPLOAD_FAILED", "Không tải được ảnh lên Cloudinary, vui lòng thử lại");
		}
	}
}
