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
public final class GetRulesUseCase_Factory implements Factory<GetRulesUseCase> {
  private final Provider<RuleRepository> repositoryProvider;

  public GetRulesUseCase_Factory(Provider<RuleRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetRulesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetRulesUseCase_Factory create(Provider<RuleRepository> repositoryProvider) {
    return new GetRulesUseCase_Factory(repositoryProvider);
  }

  public static GetRulesUseCase newInstance(RuleRepository repository) {
    return new GetRulesUseCase(repository);
  }
}
