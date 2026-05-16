package com.example.conscia.domain.usecase;

import com.example.conscia.data.rule.RuleRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class UpsertRuleUseCase_Factory implements Factory<UpsertRuleUseCase> {
  private final Provider<RuleRepository> repositoryProvider;

  public UpsertRuleUseCase_Factory(Provider<RuleRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public UpsertRuleUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static UpsertRuleUseCase_Factory create(Provider<RuleRepository> repositoryProvider) {
    return new UpsertRuleUseCase_Factory(repositoryProvider);
  }

  public static UpsertRuleUseCase newInstance(RuleRepository repository) {
    return new UpsertRuleUseCase(repository);
  }
}
