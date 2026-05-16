package com.example.conscia.monitoring;

import com.example.conscia.data.rule.RuleRepository;
import com.example.conscia.data.usage.UsageStatsRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AccessibilityForegroundAppService_MembersInjector implements MembersInjector<AccessibilityForegroundAppService> {
  private final Provider<RuleRepository> ruleRepositoryProvider;

  private final Provider<UsageStatsRepository> usageRepositoryProvider;

  public AccessibilityForegroundAppService_MembersInjector(
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<UsageStatsRepository> usageRepositoryProvider) {
    this.ruleRepositoryProvider = ruleRepositoryProvider;
    this.usageRepositoryProvider = usageRepositoryProvider;
  }

  public static MembersInjector<AccessibilityForegroundAppService> create(
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<UsageStatsRepository> usageRepositoryProvider) {
    return new AccessibilityForegroundAppService_MembersInjector(ruleRepositoryProvider, usageRepositoryProvider);
  }

  @Override
  public void injectMembers(AccessibilityForegroundAppService instance) {
    injectRuleRepository(instance, ruleRepositoryProvider.get());
    injectUsageRepository(instance, usageRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.example.conscia.monitoring.AccessibilityForegroundAppService.ruleRepository")
  public static void injectRuleRepository(AccessibilityForegroundAppService instance,
      RuleRepository ruleRepository) {
    instance.ruleRepository = ruleRepository;
  }

  @InjectedFieldSignature("com.example.conscia.monitoring.AccessibilityForegroundAppService.usageRepository")
  public static void injectUsageRepository(AccessibilityForegroundAppService instance,
      UsageStatsRepository usageRepository) {
    instance.usageRepository = usageRepository;
  }
}
