package com.example.conscia.domain.usecase;

import com.example.conscia.data.usage.UsageStatsRepository;
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
public final class GetWeeklyUsageUseCase_Factory implements Factory<GetWeeklyUsageUseCase> {
  private final Provider<UsageStatsRepository> repositoryProvider;

  public GetWeeklyUsageUseCase_Factory(Provider<UsageStatsRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetWeeklyUsageUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetWeeklyUsageUseCase_Factory create(
      Provider<UsageStatsRepository> repositoryProvider) {
    return new GetWeeklyUsageUseCase_Factory(repositoryProvider);
  }

  public static GetWeeklyUsageUseCase newInstance(UsageStatsRepository repository) {
    return new GetWeeklyUsageUseCase(repository);
  }
}
