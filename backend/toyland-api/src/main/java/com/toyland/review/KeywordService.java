package com.toyland.review;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toyland.audit.AuditService;
import com.toyland.auth.CurrentUserService;
import com.toyland.common.exception.ApiException;

/** Quan ly danh sach tu khoa cam (co bo nho dem, lam moi khi them/xoa). */
@Service
@Transactional
public class KeywordService {

	private final BannedKeywordRepository repo;
	private final CurrentUserService currentUser;
	private final AuditService audit;
	private volatile KeywordModeration cache;

	public KeywordService(BannedKeywordRepository repo, CurrentUserService currentUser, AuditService audit) {
		this.repo = repo;
		this.currentUser = currentUser;
		this.audit = audit;
	}

	public record KeywordDto(Long id, String keyword, String createdBy, LocalDateTime createdAt) {
	}

	@Transactional(readOnly = true)
	public List<KeywordDto> all() {
		return repo.findAllByOrderByKeywordAsc().stream()
				.map(k -> new KeywordDto(k.getId(), k.getKeyword(), k.getCreatedBy(), k.getCreatedAt())).toList();
	}

	/** Tu khoa cam co trong van ban. */
	@Transactional(readOnly = true)
	public List<String> check(String text) {
		KeywordModeration m = cache;
		if (m == null) {
			m = new KeywordModeration(repo.findAll().stream().map(BannedKeyword::getKeyword).toList());
			cache = m;
		}
		return m.find(text);
	}

	public KeywordDto add(String raw) {
		String k = KeywordModeration.normalizeKeyword(raw);
		if (k.length() < 2) {
			throw ApiException.badRequest("Từ khoá tối thiểu 2 ký tự");
		}
		if (repo.existsByKeyword(k)) {
			throw ApiException.conflict("Từ khoá \"" + k + "\" đã có trong danh sách");
		}
		BannedKeyword b = new BannedKeyword();
		b.setKeyword(k);
		b.setCreatedBy(currentUser.actorName());
		repo.save(b);
		cache = null;
		audit.log("CREATE", "BANNED_KEYWORD", b.getId(), "Thêm từ khoá cấm \"" + k + "\"");
		return new KeywordDto(b.getId(), b.getKeyword(), b.getCreatedBy(), b.getCreatedAt());
	}

	public void delete(Long id) {
		BannedKeyword b = repo.findById(id).orElseThrow(() -> ApiException.notFound("từ khoá", id));
		repo.delete(b);
		cache = null;
		audit.log("DELETE", "BANNED_KEYWORD", id, "Xoá từ khoá cấm \"" + b.getKeyword() + "\"");
	}
}
