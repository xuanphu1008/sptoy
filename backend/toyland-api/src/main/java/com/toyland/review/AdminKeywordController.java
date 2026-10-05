package com.toyland.review;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.common.dto.MessageResponse;
import com.toyland.review.KeywordService.KeywordDto;
import com.toyland.review.dto.ReviewDtos.KeywordRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Tu khoa cam trong danh gia (chi ADMIN). */
@Tag(name = "13. Đánh giá sản phẩm")
@RestController
@RequestMapping("/api/v1/admin/banned-keywords")
public class AdminKeywordController {

	private final KeywordService keywordService;
	private final ReviewService reviewService;

	public AdminKeywordController(KeywordService keywordService, ReviewService reviewService) {
		this.keywordService = keywordService;
		this.reviewService = reviewService;
	}

	@GetMapping
	public List<KeywordDto> all() {
		return keywordService.all();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public KeywordDto add(@Valid @RequestBody KeywordRequest r) {
		return keywordService.add(r.keyword());
	}

	@DeleteMapping("/{id}")
	public MessageResponse delete(@PathVariable Long id) {
		keywordService.delete(id);
		return new MessageResponse("Đã xoá từ khoá");
	}

	/** Quet lai toan bo danh gia dang hien theo danh sach tu khoa moi nhat. */
	@PostMapping("/rescan")
	public MessageResponse rescan() {
		int n = reviewService.rescan();
		return new MessageResponse(n == 0 ? "Không phát hiện đánh giá vi phạm" : "Đã ẩn " + n + " đánh giá vi phạm, chờ duyệt");
	}
}
