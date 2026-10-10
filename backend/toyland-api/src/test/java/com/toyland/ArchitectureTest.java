package com.toyland;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * Kiem thu kien truc: bao dam ranh gioi giua cac mien dich vu.
 * Moi mien (bounded context) so huu du lieu cua minh; mien khac chi duoc truy cap qua interface cong bo
 * (UserApi, CatalogApi, OrderQueryApi, VoucherUsageApi, ReviewStatsApi...), khong duoc dung repository cua mien khac.
 */
class ArchitectureTest {

	/** Goi ma nguon -> mien dich vu. */
	static final Map<String, String> CONTEXT = Map.ofEntries(
			Map.entry("auth", "identity"), Map.entry("user", "identity"),
			Map.entry("catalog", "catalog"), Map.entry("inventory", "catalog"), Map.entry("supplier", "catalog"),
			Map.entry("cart", "sales"), Map.entry("order", "sales"), Map.entry("wishlist", "sales"),
			Map.entry("payment", "payment"),
			Map.entry("promotion", "promotion"), Map.entry("loyalty", "promotion"),
			Map.entry("review", "engagement"), Map.entry("notification", "engagement"),
			Map.entry("report", "reporting"), Map.entry("audit", "reporting"),
			Map.entry("partner", "partner"),
			Map.entry("shipping", "integration"), Map.entry("media", "integration"));

	static final JavaClasses CLASSES = new ClassFileImporter()
			.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages("com.toyland");

	static String contextOf(JavaClass c) {
		String pkg = c.getPackageName();
		if (!pkg.startsWith("com.toyland.")) {
			return null;
		}
		String first = pkg.substring("com.toyland.".length()).split("\\.")[0];
		return CONTEXT.get(first);
	}

	static boolean isRepository(JavaClass c) {
		return c.getSimpleName().endsWith("Repository") && c.getPackageName().startsWith("com.toyland.");
	}

	@Test
	void khongDungRepositoryCuaMienKhac() {
		ArchCondition<JavaClass> notCrossContext = new ArchCondition<>("khong phu thuoc repository cua mien khac") {
			@Override
			public void check(JavaClass origin, ConditionEvents events) {
				String own = contextOf(origin);
				if (own == null) {
					return;
				}
				origin.getDirectDependenciesFromSelf().forEach(d -> {
					JavaClass target = d.getTargetClass();
					String other = contextOf(target);
					if (isRepository(target) && other != null && !other.equals(own)) {
						events.add(SimpleConditionEvent.violated(d, origin.getName() + " (" + own + ") dung "
								+ target.getSimpleName() + " cua mien " + other));
					}
				});
			}
		};
		com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
				.that(DescribedPredicate.describe("thuoc mot mien nghiep vu", (JavaClass c) -> contextOf(c) != null))
				.should(notCrossContext).check(CLASSES);
	}

	@Test
	void controllerKhongGoiThangRepository() {
		noClasses().that().haveSimpleNameEndingWith("Controller")
				.should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
				.because("controller chi tiep nhan yeu cau, nghiep vu va truy cap du lieu nam o tang service")
				.check(CLASSES);
	}
}
