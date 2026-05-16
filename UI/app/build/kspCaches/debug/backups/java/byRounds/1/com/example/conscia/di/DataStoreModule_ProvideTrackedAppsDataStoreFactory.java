package com.example.conscia.di;

import android.content.Context;
import com.example.conscia.data.TrackedAppsDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DataStoreModule_ProvideTrackedAppsDataStoreFactory implements Factory<TrackedAppsDataStore> {
  private final Provider<Context> contextProvider;

  public DataStoreModule_ProvideTrackedAppsDataStoreFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public TrackedAppsDataStore get() {
    return provideTrackedAppsDataStore(contextProvider.get());
  }

  public static DataStoreModule_ProvideTrackedAppsDataStoreFactory create(
      Provider<Context> contextProvider) {
    return new DataStoreModule_ProvideTrackedAppsDataStoreFactory(contextProvider);
  }

  public static TrackedAppsDataStore provideTrackedAppsDataStore(Context context) {
    return Preconditions.checkNotNullFromProvides(DataStoreModule.INSTANCE.provideTrackedAppsDataStore(context));
  }
}
