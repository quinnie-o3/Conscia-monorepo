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
public final class DeleteRuleUseCase_Factory implements Factory<DeleteRuleUseCase> {
  private final Provider<RuleRepository> repositoryProvider;

  public DeleteRuleUseCase_Factory(Provider<RuleRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeleteRuleUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DeleteRuleUseCase_Factory create(Provider<RuleRepository> repositoryProvider) {
    return new DeleteRuleUseCase_Factory(repositoryProvider);
  }

  public static DeleteRuleUseCase newInstance(RuleRepository repository) {
    return new DeleteRuleUseCase(repository);
  }
}
