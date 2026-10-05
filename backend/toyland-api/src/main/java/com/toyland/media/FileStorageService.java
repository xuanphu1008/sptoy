package com.toyland.media;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.toyland.common.exception.ApiException;

/**
 * Luu anh upload: neu da cau hinh Cloudinary thi day len Cloudinary (giu vinh vien khi chay tren cloud),
 * nguoc lai luu vao thu muc uploads/ tren server va tra ve URL cong khai /uploads/...
 */
@Service
public class FileStorageService {

	private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");

	private final Path root;
	private final String publicBaseUrl;
	private final CloudinaryUploader cloudinary;

	public FileStorageService(@Value("${app.upload-dir}") String uploadDir, @Value("${app.public-base-url}") String publicBaseUrl,
			CloudinaryUploader cloudinary) throws IOException {
		this.cloudinary = cloudinary;
		this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
		Files.createDirectories(root);
		this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
	}

	public String store(MultipartFile file, String folder) {
		if (file == null || file.isEmpty()) {
			throw ApiException.badRequest("Chưa chọn file");
		}
		String ct = file.getContentType();
		if (ct == null || !ct.startsWith("image/")) {
			throw ApiException.badRequest("Chỉ chấp nhận file ảnh");
		}
		String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
		String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
		if (!ALLOWED_EXT.contains(ext)) {
			throw ApiException.badRequest("Định dạng ảnh không hỗ trợ (jpg, png, webp, gif)");
		}
		if (cloudinary.isEnabled()) {
			try {
				return cloudinary.upload(file.getBytes(), "anh." + ext, folder);
			} catch (IOException e) {
				throw ApiException.badRequest("Không đọc được file: " + e.getMessage());
			}
		}
		LocalDate d = LocalDate.now();
		String rel = folder + "/" + d.getYear() + "/" + String.format("%02d", d.getMonthValue()) + "/"
				+ UUID.randomUUID().toString().replace("-", "") + "." + ext;
		Path target = root.resolve(rel).normalize();
		if (!target.startsWith(root)) {
			throw ApiException.badRequest("Đường dẫn không hợp lệ");
		}
		try (InputStream in = file.getInputStream()) {
			Files.createDirectories(target.getParent());
			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw ApiException.badRequest("Không lưu được file: " + e.getMessage());
		}
		return publicBaseUrl + "/uploads/" + rel;
	}
}
