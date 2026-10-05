import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipInputEvent, MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { AdminApi } from '../../core/admin-api.service';
import { ShopApi } from '../../core/api.service';
import { GENDER, PLACEHOLDER_IMG, PRODUCT_STATUS } from '../../core/labels';
import { AdminProduct, AdminVariant, Brand, CategoryNode, Franchise, Gender, ProductStatus } from '../../core/models';
import { NotifyService } from '../../core/notify.service';
import { flattenCategories } from './products.component';

const AGE_OPTIONS = [0, 3, 6, 12, 18, 24, 36, 48, 60, 72, 96, 120, 144, 168, 216];

function slugify(s: string): string {
  return s.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D')
    .toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');
}

function emptyProduct(): AdminProduct {
  return {
    name: '', slug: '', shortDescription: '', description: '', categoryId: null, brandId: null, franchiseIds: [], ageMinMonths: 36,
    ageMaxMonths: null, gender: 'UNISEX', material: '', brandOrigin: '', madeIn: '', manufactureYear: new Date().getFullYear(), battery: '',
    smallPartsWarning: false, safetyWarning: '', crCertified: true, importer: '', lengthCm: 20, widthCm: 15, heightCm: 10, videoUrl: '',
    status: 'DRAFT', featured: false, onlineExclusive: false, metaTitle: '', metaDescription: '', option1Name: null, option2Name: null,
    specs: [], images: [], variants: [{ id: null, sku: '', barcode: '', option1: null, option2: null, price: 0, compareAtPrice: null, costPrice: null, initialStock: 0, weightGram: 300, imageUrl: null, active: true }],
  };
}

@Component({
  selector: 'tl-product-edit',
  imports: [RouterLink, FormsModule, MatTabsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatCheckboxModule, MatSlideToggleModule,
    MatButtonModule, MatIconModule, MatChipsModule, MatTooltipModule],
  templateUrl: './product-edit.component.html',
  styleUrl: './product-edit.component.scss',
})
export class ProductEditComponent implements OnInit {
  id = input<string>();
  private admin = inject(AdminApi);
  private shop = inject(ShopApi);
  private notify = inject(NotifyService);
  private router = inject(Router);

  p = signal<AdminProduct>(emptyProduct());
  tree = signal<CategoryNode[]>([]);
  cats = computed(() => flattenCategories(this.tree()));
  brands = signal<Brand[]>([]);
  franchises = signal<Franchise[]>([]);
  values1 = signal<string[]>([]);
  values2 = signal<string[]>([]);
  saving = signal(false);
  uploading = signal(false);
  newImageUrl = '';
  statuses = Object.entries(PRODUCT_STATUS) as [ProductStatus, string][];
  genders = Object.entries(GENDER) as [Gender, string][];
  ages = AGE_OPTIONS;
  ph = PLACEHOLDER_IMG;
  isNew = computed(() => !this.p().id);

  ngOnInit(): void {
    this.admin.categories().subscribe((c) => this.tree.set(c));
    this.shop.brands().subscribe((b) => this.brands.set(b));
    this.shop.franchises().subscribe((f) => this.franchises.set(f));
    const id = this.id();
    if (id && id !== 'new') {
      this.admin.product(+id).subscribe((p) => {
        this.p.set({ ...p, specs: [...p.specs], images: [...p.images], variants: p.variants.map((v) => ({ ...v })) });
        this.values1.set([...new Set(p.variants.map((v) => v.option1).filter((x): x is string => !!x))]);
        this.values2.set([...new Set(p.variants.map((v) => v.option2).filter((x): x is string => !!x))]);
      });
    }
  }

  ageLabel(m: number): string {
    return m < 12 ? `${m} tháng` : m % 12 === 0 ? `${m / 12} tuổi` : `${(m / 12).toFixed(1)} tuổi`;
  }

  /** Tu tao slug + SKU goi y tu ten san pham */
  onName(): void {
    const p = this.p();
    if (this.isNew()) p.slug = slugify(p.name);
    if (!p.metaTitle) p.metaTitle = '';
  }

  // ---------------- anh
  addImageUrl(): void {
    const u = this.newImageUrl.trim();
    if (!u) return;
    this.p().images.push(u);
    this.newImageUrl = '';
  }

  upload(ev: Event): void {
    const el = ev.target as HTMLInputElement;
    const files = Array.from(el.files ?? []);
    el.value = '';
    if (!files.length) return;
    this.uploading.set(true);
    let left = files.length;
    for (const f of files) {
      this.shop.upload(f, 'products').subscribe({
        next: (r) => this.p().images.push(r.url),
        error: (e) => this.notify.error(e),
      }).add(() => { if (--left === 0) this.uploading.set(false); });
    }
  }

  moveImage(i: number, d: number): void {
    const imgs = this.p().images;
    const j = i + d;
    if (j < 0 || j >= imgs.length) return;
    [imgs[i], imgs[j]] = [imgs[j], imgs[i]];
  }

  removeImage(i: number): void {
    this.p().images.splice(i, 1);
  }

  // ---------------- bien the (toi da 2 thuoc tinh)
  addValue(which: 1 | 2, ev: MatChipInputEvent): void {
    const v = (ev.value || '').trim();
    const sig = which === 1 ? this.values1 : this.values2;
    if (v && !sig().includes(v)) sig.set([...sig(), v]);
    ev.chipInput.clear();
  }

  removeValue(which: 1 | 2, v: string): void {
    const sig = which === 1 ? this.values1 : this.values2;
    sig.set(sig().filter((x) => x !== v));
  }

  /** Sinh to hop bien the tu gia tri thuoc tinh; giu nguyen bien the da co (so khop theo option1/option2) */
  generate(): void {
    const p = this.p();
    const o1 = p.option1Name?.trim() ? this.values1() : [];
    const o2 = p.option1Name?.trim() && p.option2Name?.trim() ? this.values2() : [];
    if (p.option1Name?.trim() && !o1.length) return this.notify.error(`Nhập ít nhất 1 giá trị cho "${p.option1Name}"`);
    if (p.option2Name?.trim() && !o2.length) return this.notify.error(`Nhập ít nhất 1 giá trị cho "${p.option2Name}"`);
    const combos: [string | null, string | null][] = !o1.length ? [[null, null]] : o2.length ? o1.flatMap((a) => o2.map((b) => [a, b] as [string, string])) : o1.map((a) => [a, null]);
    const base = p.variants[0];
    const skuBase = (base?.sku?.replace(/-[^-]*$/, '') || slugify(p.name).toUpperCase().slice(0, 16) || 'SP').toUpperCase();
    const next: AdminVariant[] = combos.map(([a, b], i) => {
      const found = p.variants.find((v) => (v.option1 ?? null) === a && (v.option2 ?? null) === b);
      if (found) return found;
      const suffix = [a, b].filter(Boolean).map((x) => slugify(x!).toUpperCase().slice(0, 6)).join('-') || String(i + 1);
      return {
        id: null, sku: `${skuBase}-${suffix}`, barcode: '', option1: a, option2: b, price: base?.price ?? 0, compareAtPrice: base?.compareAtPrice ?? null,
        costPrice: base?.costPrice ?? null, initialStock: 0, weightGram: base?.weightGram ?? 300, imageUrl: null, active: true,
      };
    });
    const dropped = p.variants.filter((v) => v.id && !next.includes(v));
    // Bien the cu khong con trong to hop -> giu lai nhung tat (backend khong xoa bien the da co don)
    dropped.forEach((v) => (v.active = false));
    p.variants = [...next, ...dropped];
    this.notify.info(`Đã tạo ${next.length} biến thể${dropped.length ? `, ${dropped.length} biến thể cũ được tắt` : ''}`);
  }

  applyAll(field: 'price' | 'compareAtPrice' | 'costPrice' | 'weightGram'): void {
    const first = this.p().variants[0];
    if (!first) return;
    this.p().variants.forEach((v) => ((v as unknown as Record<string, unknown>)[field] = first[field]));
  }

  // ---------------- thong so
  addSpec(): void {
    this.p().specs.push({ key: '', value: '' });
  }

  removeSpec(i: number): void {
    this.p().specs.splice(i, 1);
  }

  save(): void {
    const p = this.p();
    if (!p.name.trim() || !p.categoryId) return this.notify.error('Nhập tên sản phẩm và chọn danh mục');
    if (!p.variants.some((v) => v.active)) return this.notify.error('Sản phẩm cần ít nhất 1 biến thể đang bán');
    if (p.variants.some((v) => !v.sku.trim())) return this.notify.error('SKU của biến thể không được để trống');
    const body = {
      ...p,
      option1Name: p.option1Name?.trim() || null,
      option2Name: p.option1Name?.trim() ? p.option2Name?.trim() || null : null,
      specs: p.specs.filter((s) => s.key.trim() && s.value.trim()),
      variants: p.variants.map((v) => ({
        id: v.id ?? null, sku: v.sku.trim(), barcode: v.barcode || null, option1: v.option1 ?? null, option2: v.option2 ?? null, price: v.price,
        compareAtPrice: v.compareAtPrice || null, costPrice: v.costPrice || null, initialStock: v.id ? null : v.initialStock ?? 0,
        weightGram: v.weightGram, imageUrl: v.imageUrl || null, active: v.active,
      })),
    };
    this.saving.set(true);
    this.admin.saveProduct(p.id, body).subscribe({
      next: (r) => {
        this.saving.set(false);
        this.notify.success('Đã lưu sản phẩm');
        if (!p.id) this.router.navigate(['/admin/products', r.id], { replaceUrl: true });
        this.p.set({ ...r, specs: [...r.specs], images: [...r.images], variants: r.variants.map((v) => ({ ...v })) });
      },
      error: (e) => {
        this.saving.set(false);
        this.notify.error(e);
      },
    });
  }
}
