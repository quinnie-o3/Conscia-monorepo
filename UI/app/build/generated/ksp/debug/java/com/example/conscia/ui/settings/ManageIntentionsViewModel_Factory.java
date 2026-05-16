package com.example.conscia.ui.settings;

import com.example.conscia.data.remote.api.ConsciaApiService;
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
public final class ManageIntentionsViewModel_Factory implements Factory<ManageIntentionsViewModel> {
  private final Provider<ConsciaApiService> apiServiceProvider;

  public ManageIntentionsViewModel_Factory(Provider<ConsciaApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public ManageIntentionsViewModel get() {
    return newInstance(apiServiceProvider.get());
  }

  public static ManageIntentionsViewModel_Factory create(
      Provider<ConsciaApiService> apiServiceProvider) {
    return new ManageIntentionsViewModel_Factory(apiServiceProvider);
  }

  public static ManageIntentionsViewModel newInstance(ConsciaApiService apiService) {
    return new ManageIntentionsViewModel(apiService);
  }
}
