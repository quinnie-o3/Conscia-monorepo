package com.example.conscia.monitoring;

import com.example.conscia.data.remote.RemoteUsageSyncRepository;
import com.example.conscia.data.rule.RuleRepository;
import com.example.conscia.data.usage.UsageStatsRepository;
import com.example.conscia.data.warning.WarningHistoryStore;
import com.example.conscia.notification.ConsciaNotificationManager;
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

  private final Provider<ConsciaNotificationManager> notificationManagerProvider;

  private final Provider<WarningHistoryStore> warningHistoryStoreProvider;

  private final Provider<RemoteUsageSyncRepository> remoteUsageSyncRepositoryProvider;

  public AccessibilityForegroundAppService_MembersInjector(
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<UsageStatsRepository> usageRepositoryProvider,
      Provider<ConsciaNotificationManager> notificationManagerProvider,
      Provider<WarningHistoryStore> warningHistoryStoreProvider,
      Provider<RemoteUsageSyncRepository> remoteUsageSyncRepositoryProvider) {
    this.ruleRepositoryProvider = ruleRepositoryProvider;
    this.usageRepositoryProvider = usageRepositoryProvider;
    this.notificationManagerProvider = notificationManagerProvider;
    this.warningHistoryStoreProvider = warningHistoryStoreProvider;
    this.remoteUsageSyncRepositoryProvider = remoteUsageSyncRepositoryProvider;
  }

  public static MembersInjector<AccessibilityForegroundAppService> create(
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<UsageStatsRepository> usageRepositoryProvider,
      Provider<ConsciaNotificationManager> notificationManagerProvider,
      Provider<WarningHistoryStore> warningHistoryStoreProvider,
      Provider<RemoteUsageSyncRepository> remoteUsageSyncRepositoryProvider) {
    return new AccessibilityForegroundAppService_MembersInjector(ruleRepositoryProvider, usageRepositoryProvider, notificationManagerProvider, warningHistoryStoreProvider, remoteUsageSyncRepositoryProvider);
  }

  @Override
  public void injectMembers(AccessibilityForegroundAppService instance) {
    injectRuleRepository(instance, ruleRepositoryProvider.get());
    injectUsageRepository(instance, usageRepositoryProvider.get());
    injectNotificationManager(instance, notificationManagerProvider.get());
    injectWarningHistoryStore(instance, warningHistoryStoreProvider.get());
    injectRemoteUsageSyncRepository(instance, remoteUsageSyncRepositoryProvider.get());
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

  @InjectedFieldSignature("com.example.conscia.monitoring.AccessibilityForegroundAppService.notificationManager")
  public static void injectNotificationManager(AccessibilityForegroundAppService instance,
      ConsciaNotificationManager notificationManager) {
    instance.notificationManager = notificationManager;
  }

  @InjectedFieldSignature("com.example.conscia.monitoring.AccessibilityForegroundAppService.warningHistoryStore")
  public static void injectWarningHistoryStore(AccessibilityForegroundAppService instance,
      WarningHistoryStore warningHistoryStore) {
    instance.warningHistoryStore = warningHistoryStore;
  }

  @InjectedFieldSignature("com.example.conscia.monitoring.AccessibilityForegroundAppService.remoteUsageSyncRepository")
  public static void injectRemoteUsageSyncRepository(AccessibilityForegroundAppService instance,
      RemoteUsageSyncRepository remoteUsageSyncRepository) {
    instance.remoteUsageSyncRepository = remoteUsageSyncRepository;
  }
}
