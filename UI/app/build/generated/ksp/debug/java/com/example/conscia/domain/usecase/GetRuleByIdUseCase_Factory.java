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
public final class GetRuleByIdUseCase_Factory implements Factory<GetRuleByIdUseCase> {
  private final Provider<RuleRepository> repositoryProvider;

  public GetRuleByIdUseCase_Factory(Provider<RuleRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetRuleByIdUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetRuleByIdUseCase_Factory create(Provider<RuleRepository> repositoryProvider) {
    return new GetRuleByIdUseCase_Factory(repositoryProvider);
  }

  public static GetRuleByIdUseCase newInstance(RuleRepository repository) {
    return new GetRuleByIdUseCase(repository);
  }
}
