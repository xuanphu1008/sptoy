package com.toyland.common.util;

/** Mau kiem tra dinh dang dung chung (giong file core/validators.ts o frontend). */
public final class ValidationPatterns {

	/**
	 * Email: phan truoc @ gom chu, so, . _ % + -; ten mien co it nhat 1 dau cham, duoi 2+ chu cai.
	 * (@Email mac dinh cua Hibernate chap nhan ca "a@b" nen can them mau nay.)
	 */
	public static final String EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$";

	private ValidationPatterns() {
	}
}
