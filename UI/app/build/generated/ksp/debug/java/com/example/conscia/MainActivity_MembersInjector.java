package com.example.conscia;

import com.example.conscia.data.TrackedAppsDataStore;
import com.example.conscia.data.remote.RemoteUsageSyncRepository;
import com.example.conscia.data.remote.api.ConsciaApiService;
import com.example.conscia.data.rule.RuleRepository;
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<TrackedAppsDataStore> dataStoreProvider;

  private final Provider<RuleRepository> ruleRepositoryProvider;

  private final Provider<ConsciaApiService> apiServiceProvider;

  private final Provider<RemoteUsageSyncRepository> remoteUsageSyncRepositoryProvider;

  public MainActivity_MembersInjector(Provider<TrackedAppsDataStore> dataStoreProvider,
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<ConsciaApiService> apiServiceProvider,
      Provider<RemoteUsageSyncRepository> remoteUsageSyncRepositoryProvider) {
    this.dataStoreProvider = dataStoreProvider;
    this.ruleRepositoryProvider = ruleRepositoryProvider;
    this.apiServiceProvider = apiServiceProvider;
    this.remoteUsageSyncRepositoryProvider = remoteUsageSyncRepositoryProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<TrackedAppsDataStore> dataStoreProvider,
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<ConsciaApiService> apiServiceProvider,
      Provider<RemoteUsageSyncRepository> remoteUsageSyncRepositoryProvider) {
    return new MainActivity_MembersInjector(dataStoreProvider, ruleRepositoryProvider, apiServiceProvider, remoteUsageSyncRepositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectDataStore(instance, dataStoreProvider.get());
    injectRuleRepository(instance, ruleRepositoryProvider.get());
    injectApiService(instance, apiServiceProvider.get());
    injectRemoteUsageSyncRepository(instance, remoteUsageSyncRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.example.conscia.MainActivity.dataStore")
  public static void injectDataStore(MainActivity instance, TrackedAppsDataStore dataStore) {
    instance.dataStore = dataStore;
  }

  @InjectedFieldSignature("com.example.conscia.MainActivity.ruleRepository")
  public static void injectRuleRepository(MainActivity instance, RuleRepository ruleRepository) {
    instance.ruleRepository = ruleRepository;
  }

  @InjectedFieldSignature("com.example.conscia.MainActivity.apiService")
  public static void injectApiService(MainActivity instance, ConsciaApiService apiService) {
    instance.apiService = apiService;
  }

  @InjectedFieldSignature("com.example.conscia.MainActivity.remoteUsageSyncRepository")
  public static void injectRemoteUsageSyncRepository(MainActivity instance,
      RemoteUsageSyncRepository remoteUsageSyncRepository) {
    instance.remoteUsageSyncRepository = remoteUsageSyncRepository;
  }
}
