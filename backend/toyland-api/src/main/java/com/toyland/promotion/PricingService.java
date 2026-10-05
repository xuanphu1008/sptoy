package com.toyland.promotion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.toyland.catalog.Category;
import com.toyland.catalog.Franchise;
import com.toyland.catalog.Product;
import com.toyland.catalog.ProductVariant;
import com.toyland.common.util.Money;

/**
 * Tinh gia ban cuoi cung cua 1 bien the:
 *  gia ban = variant.price, neu co chuong trinh giam gia (campaign) dang chay ap dung cho san pham
 *  thi giam them %. Gia goc (gach ngang) = compareAtPrice (neu lon hon) hoac price.
 */
@Service
public class PricingService {

	private final CampaignRepository campaignRepository;

	public PricingService(CampaignRepository campaignRepository) {
		this.campaignRepository = campaignRepository;
	}

	public record PriceInfo(BigDecimal price, BigDecimal originalPrice, int discountPercent, String badge) {
	}

	public List<Campaign> runningCampaigns() {
		return campaignRepository.findRunning(LocalDateTime.now());
	}

	/** Chuong trinh giam nhieu nhat dang ap dung cho san pham (null neu khong co). */
	public Campaign bestCampaign(Product p, List<Campaign> campaigns) {
		Campaign best = null;
		for (Campaign c : campaigns) {
			if (applies(c, p) && (best == null || c.getDiscountPercent() > best.getDiscountPercent())) {
				best = c;
			}
		}
		return best;
	}

	public boolean applies(Campaign c, Product p) {
		Long sid = c.getScopeId();
		switch (c.getScopeType()) {
		case ALL:
			return true;
		case PRODUCT:
			return sid != null && sid.equals(p.getId());
		case BRAND:
			return sid != null && p.getBrand() != null && sid.equals(p.getBrand().getId());
		case CATEGORY:
			for (Category cat = p.getCategory(); cat != null; cat = cat.getParent()) {
				if (sid != null && sid.equals(cat.getId())) {
					return true;
				}
			}
			return false;
		case FRANCHISE:
			for (Franchise f : p.getFranchises()) {
				if (f.getId().equals(sid)) {
					return true;
				}
			}
			return false;
		default:
			return false;
		}
	}

	public PriceInfo price(Product p, ProductVariant v, List<Campaign> campaigns) {
		return price(v, bestCampaign(p, campaigns));
	}

	public PriceInfo price(ProductVariant v, Campaign campaign) {
		BigDecimal base = v.getPrice();
		BigDecimal finalPrice = base;
		String badge = null;
		if (campaign != null) {
			// lam tron xuong 1.000d nhung khong bao gio cao hon gia goc
			finalPrice = Money.min(Money.roundThousandDown(base.subtract(Money.percentOf(base, campaign.getDiscountPercent()))), base);
			badge = campaign.getBadgeLabel();
		}
		BigDecimal original = null;
		if (v.getCompareAtPrice() != null && v.getCompareAtPrice().compareTo(finalPrice) > 0) {
			original = v.getCompareAtPrice();
		} else if (finalPrice.compareTo(base) < 0) {
			original = base;
		}
		int percent = 0;
		if (original != null && original.signum() > 0) {
			percent = original.subtract(finalPrice).multiply(BigDecimal.valueOf(100))
					.divide(original, 0, RoundingMode.HALF_UP).intValue();
		}
		return new PriceInfo(finalPrice, original, percent, badge);
	}
}
