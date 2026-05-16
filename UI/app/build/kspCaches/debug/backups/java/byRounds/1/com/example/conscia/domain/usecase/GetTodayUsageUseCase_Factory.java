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
public final class GetTodayUsageUseCase_Factory implements Factory<GetTodayUsageUseCase> {
  private final Provider<UsageStatsRepository> repositoryProvider;

  public GetTodayUsageUseCase_Factory(Provider<UsageStatsRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetTodayUsageUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetTodayUsageUseCase_Factory create(
      Provider<UsageStatsRepository> repositoryProvider) {
    return new GetTodayUsageUseCase_Factory(repositoryProvider);
  }

  public static GetTodayUsageUseCase newInstance(UsageStatsRepository repository) {
    return new GetTodayUsageUseCase(repository);
  }
}
