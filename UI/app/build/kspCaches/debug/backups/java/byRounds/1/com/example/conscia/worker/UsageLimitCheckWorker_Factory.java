package com.example.conscia.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.example.conscia.data.remote.RemoteUsageSyncRepository;
import com.example.conscia.data.weekly.WeeklySummaryManager;
import com.example.conscia.domain.usecase.CheckUsageLimitWarningsUseCase;
import dagger.internal.DaggerGenerated;
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
public final class UsageLimitCheckWorker_Factory {
  private final Provider<CheckUsageLimitWarningsUseCase> checkWarningsUseCaseProvider;

  private final Provider<WeeklySummaryManager> weeklySummaryManagerProvider;

  private final Provider<RemoteUsageSyncRepository> remoteSyncRepositoryProvider;

  public UsageLimitCheckWorker_Factory(
      Provider<CheckUsageLimitWarningsUseCase> checkWarningsUseCaseProvider,
      Provider<WeeklySummaryManager> weeklySummaryManagerProvider,
      Provider<RemoteUsageSyncRepository> remoteSyncRepositoryProvider) {
    this.checkWarningsUseCaseProvider = checkWarningsUseCaseProvider;
    this.weeklySummaryManagerProvider = weeklySummaryManagerProvider;
    this.remoteSyncRepositoryProvider = remoteSyncRepositoryProvider;
  }

  public UsageLimitCheckWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, checkWarningsUseCaseProvider.get(), weeklySummaryManagerProvider.get(), remoteSyncRepositoryProvider.get());
  }

  public static UsageLimitCheckWorker_Factory create(
      Provider<CheckUsageLimitWarningsUseCase> checkWarningsUseCaseProvider,
      Provider<WeeklySummaryManager> weeklySummaryManagerProvider,
      Provider<RemoteUsageSyncRepository> remoteSyncRepositoryProvider) {
    return new UsageLimitCheckWorker_Factory(checkWarningsUseCaseProvider, weeklySummaryManagerProvider, remoteSyncRepositoryProvider);
  }

  public static UsageLimitCheckWorker newInstance(Context context, WorkerParameters params,
      CheckUsageLimitWarningsUseCase checkWarningsUseCase,
      WeeklySummaryManager weeklySummaryManager, RemoteUsageSyncRepository remoteSyncRepository) {
    return new UsageLimitCheckWorker(context, params, checkWarningsUseCase, weeklySummaryManager, remoteSyncRepository);
  }
}
