package com.toyland.user;

/**
 * Su kien nghiep vu phat ra khi khach tu xoa tai khoan. Cac mien khac (gio hang, yeu thich)
 * tu don du lieu cua minh khi nhan su kien, mien tai khoan khong can biet ho la ai (lien ket long).
 * Listener chay dong bo trong cung giao dich: neu mot buoc loi thi toan bo viec xoa bi huy.
 */
public record UserAccountDeletedEvent(Long userId) {
}
