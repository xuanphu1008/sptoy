package com.toyland.promotion;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.toyland.common.dto.MessageResponse;
import com.toyland.promotion.dto.PromotionDtos.BannerDto;
import com.toyland.promotion.dto.PromotionDtos.BannerRequest;
import com.toyland.promotion.dto.PromotionDtos.CampaignDto;
import com.toyland.promotion.dto.PromotionDtos.CampaignRequest;
import com.toyland.promotion.dto.PromotionDtos.VoucherDto;
import com.toyland.promotion.dto.PromotionDtos.VoucherRequest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "07. Khuyến mãi (voucher, chương trình giảm giá, banner)")
@RestController
public class PromotionController {

	private final VoucherService voucherService;
	private final CampaignService campaignService;
	private final BannerService bannerService;

	public PromotionController(VoucherService voucherService, CampaignService campaignService, BannerService bannerService) {
		this.voucherService = voucherService;
		this.campaignService = campaignService;
		this.bannerService = bannerService;
	}

	// ---------------- cong khai
	@GetMapping("/api/v1/vouchers/available")
	public List<VoucherDto> availableVouchers() {
		return voucherService.available();
	}

	@GetMapping("/api/v1/banners")
	public List<BannerDto> banners() {
		return bannerService.visible();
	}

	// ---------------- quan tri: voucher
	@GetMapping("/api/v1/admin/vouchers")
	public List<VoucherDto> vouchers() {
		return voucherService.all();
	}

	@PostMapping("/api/v1/admin/vouchers")
	@ResponseStatus(HttpStatus.CREATED)
	public VoucherDto createVoucher(@Valid @RequestBody VoucherRequest r) {
		return voucherService.save(null, r);
	}

	@PutMapping("/api/v1/admin/vouchers/{id}")
	public VoucherDto updateVoucher(@PathVariable Long id, @Valid @RequestBody VoucherRequest r) {
		return voucherService.save(id, r);
	}

	@DeleteMapping("/api/v1/admin/vouchers/{id}")
	public MessageResponse deleteVoucher(@PathVariable Long id) {
		voucherService.delete(id);
		return new MessageResponse("Đã xoá / ngừng voucher");
	}

	// ---------------- quan tri: chuong trinh giam gia
	@GetMapping("/api/v1/admin/campaigns")
	public List<CampaignDto> campaigns() {
		return campaignService.all();
	}

	@PostMapping("/api/v1/admin/campaigns")
	@ResponseStatus(HttpStatus.CREATED)
	public CampaignDto createCampaign(@Valid @RequestBody CampaignRequest r) {
		return campaignService.save(null, r);
	}

	@PutMapping("/api/v1/admin/campaigns/{id}")
	public CampaignDto updateCampaign(@PathVariable Long id, @Valid @RequestBody CampaignRequest r) {
		return campaignService.save(id, r);
	}

	@DeleteMapping("/api/v1/admin/campaigns/{id}")
	public MessageResponse deleteCampaign(@PathVariable Long id) {
		campaignService.delete(id);
		return new MessageResponse("Đã xoá chương trình");
	}

	// ---------------- quan tri: banner
	@GetMapping("/api/v1/admin/banners")
	public List<BannerDto> allBanners() {
		return bannerService.all();
	}

	@PostMapping("/api/v1/admin/banners")
	@ResponseStatus(HttpStatus.CREATED)
	public BannerDto createBanner(@Valid @RequestBody BannerRequest r) {
		return bannerService.save(null, r);
	}

	@PutMapping("/api/v1/admin/banners/{id}")
	public BannerDto updateBanner(@PathVariable Long id, @Valid @RequestBody BannerRequest r) {
		return bannerService.save(id, r);
	}

	@DeleteMapping("/api/v1/admin/banners/{id}")
	public MessageResponse deleteBanner(@PathVariable Long id) {
		bannerService.delete(id);
		return new MessageResponse("Đã xoá banner");
	}
}
