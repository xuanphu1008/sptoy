import { Validators } from '@angular/forms';

/**
 * Email hop le: phan truoc @ gom chu, so, dau . _ % + -; ten mien co it nhat 1 dau cham
 * va duoi 2+ chu cai (vd: ten.ho2004@gmail.com, a_b@truong.edu.vn).
 * Validators.email cua Angular chap nhan ca "a@b" (khong co duoi ten mien) nen can them mau nay.
 * Giong het mau ValidationPatterns.EMAIL o backend.
 */
export const EMAIL_PATTERN = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$/;

export const emailValidators = [Validators.required, Validators.maxLength(150), Validators.pattern(EMAIL_PATTERN)];

/** SĐT Viet Nam: 10-11 so, bat dau bang 0 */
export const PHONE_PATTERN = /^0\d{9,10}$/;
