package com.toyland.media;

import java.util.Map;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.toyland.auth.CurrentUserService;
import com.toyland.common.exception.ApiException;
import com.toyland.user.Role;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "16. Upload ảnh")
@RestController
public class UploadController {

	private static final Set<String> ADMIN_FOLDERS = Set.of("products", "categories", "brands", "franchises", "banners");
	private static final Set<String> CUSTOMER_FOLDERS = Set.of("reviews", "returns");

	private final FileStorageService storage;
	private final CurrentUserService currentUser;

	public UploadController(FileStorageService storage, CurrentUserService currentUser) {
		this.storage = storage;
		this.currentUser = currentUser;
	}

	/** Upload 1 anh (multipart/form-data, field "file"). Khach hang chi upload vao reviews/returns. */
	@PostMapping(value = "/api/v1/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Map<String, String> upload(@RequestPart("file") MultipartFile file,
			@RequestParam(defaultValue = "reviews") String folder) {
		Role role = currentUser.require().getRole();
		boolean staff = role == Role.ADMIN || role == Role.STAFF;
		if (!(CUSTOMER_FOLDERS.contains(folder) || (staff && ADMIN_FOLDERS.contains(folder)))) {
			throw ApiException.forbidden("Không được upload vào thư mục " + folder);
		}
		return Map.of("url", storage.store(file, folder));
	}
}
