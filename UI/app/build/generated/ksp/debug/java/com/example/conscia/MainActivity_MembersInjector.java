package com.example.conscia;

import com.example.conscia.data.TrackedAppsDataStore;
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

  public MainActivity_MembersInjector(Provider<TrackedAppsDataStore> dataStoreProvider,
      Provider<RuleRepository> ruleRepositoryProvider) {
    this.dataStoreProvider = dataStoreProvider;
    this.ruleRepositoryProvider = ruleRepositoryProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<TrackedAppsDataStore> dataStoreProvider,
      Provider<RuleRepository> ruleRepositoryProvider) {
    return new MainActivity_MembersInjector(dataStoreProvider, ruleRepositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectDataStore(instance, dataStoreProvider.get());
    injectRuleRepository(instance, ruleRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.example.conscia.MainActivity.dataStore")
  public static void injectDataStore(MainActivity instance, TrackedAppsDataStore dataStore) {
    instance.dataStore = dataStore;
  }

  @InjectedFieldSignature("com.example.conscia.MainActivity.ruleRepository")
  public static void injectRuleRepository(MainActivity instance, RuleRepository ruleRepository) {
    instance.ruleRepository = ruleRepository;
  }
}
