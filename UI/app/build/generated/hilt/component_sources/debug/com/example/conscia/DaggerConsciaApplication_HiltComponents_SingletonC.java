package com.example.conscia;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.example.conscia.data.AppDatabase;
import com.example.conscia.data.AppRepository;
import com.example.conscia.data.TrackedAppsDataStore;
import com.example.conscia.data.remote.DeviceRegistrationRepository;
import com.example.conscia.data.remote.RemoteUsageSyncRepository;
import com.example.conscia.data.remote.api.ConsciaApiService;
import com.example.conscia.data.rule.RuleDao;
import com.example.conscia.data.rule.RuleRepository;
import com.example.conscia.data.usage.UsageStatsRepository;
import com.example.conscia.data.warning.WarningHistoryStore;
import com.example.conscia.data.weekly.WeeklySummaryManager;
import com.example.conscia.data.weekly.WeeklySummaryStore;
import com.example.conscia.di.DataStoreModule_ProvideTrackedAppsDataStoreFactory;
import com.example.conscia.di.DatabaseModule_ProvideAppDatabaseFactory;
import com.example.conscia.di.DatabaseModule_ProvideRuleDaoFactory;
import com.example.conscia.di.NetworkModule_ProvideAuthInterceptorFactory;
import com.example.conscia.di.NetworkModule_ProvideConsciaApiServiceFactory;
import com.example.conscia.di.NetworkModule_ProvideOkHttpClientFactory;
import com.example.conscia.di.NetworkModule_ProvideRetrofitFactory;
import com.example.conscia.domain.usecase.CheckUsageLimitWarningsUseCase;
import com.example.conscia.domain.usecase.DeleteRuleUseCase;
import com.example.conscia.domain.usecase.EvaluateTrackedAppsUsageUseCase;
import com.example.conscia.domain.usecase.GetRuleByIdUseCase;
import com.example.conscia.domain.usecase.GetRulesUseCase;
import com.example.conscia.domain.usecase.GetTodayUsageUseCase;
import com.example.conscia.domain.usecase.GetWeeklyUsageUseCase;
import com.example.conscia.domain.usecase.UpsertRuleUseCase;
import com.example.conscia.monitoring.AccessibilityForegroundAppService;
import com.example.conscia.monitoring.AccessibilityForegroundAppService_MembersInjector;
import com.example.conscia.notification.ConsciaNotificationManager;
import com.example.conscia.ui.auth.AuthViewModel;
import com.example.conscia.ui.auth.AuthViewModel_HiltModules;
import com.example.conscia.ui.dashboard.DashboardViewModel;
import com.example.conscia.ui.dashboard.DashboardViewModel_HiltModules;
import com.example.conscia.ui.insights.InsightsViewModel;
import com.example.conscia.ui.insights.InsightsViewModel_HiltModules;
import com.example.conscia.ui.intention.SessionHistoryViewModel;
import com.example.conscia.ui.intention.SessionHistoryViewModel_HiltModules;
import com.example.conscia.ui.onboarding.ChooseAppsViewModel;
import com.example.conscia.ui.onboarding.ChooseAppsViewModel_HiltModules;
import com.example.conscia.ui.onboarding.StarterRulesViewModel;
import com.example.conscia.ui.onboarding.StarterRulesViewModel_HiltModules;
import com.example.conscia.ui.rules.CreateEditRuleViewModel;
import com.example.conscia.ui.rules.CreateEditRuleViewModel_HiltModules;
import com.example.conscia.ui.settings.ManageIntentionsViewModel;
import com.example.conscia.ui.settings.ManageIntentionsViewModel_HiltModules;
import com.example.conscia.ui.settings.ProfileViewModel;
import com.example.conscia.ui.settings.ProfileViewModel_HiltModules;
import com.example.conscia.ui.tracked.TrackedAppDetailViewModel;
import com.example.conscia.ui.tracked.TrackedAppDetailViewModel_HiltModules;
import com.example.conscia.ui.tracked.TrackedAppsViewModel;
import com.example.conscia.ui.tracked.TrackedAppsViewModel_HiltModules;
import com.example.conscia.worker.UsageLimitCheckWorker;
import com.example.conscia.worker.UsageLimitCheckWorker_AssistedFactory;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideApplicationFactory;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerConsciaApplication_HiltComponents_SingletonC {
  private DaggerConsciaApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public ConsciaApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements ConsciaApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public ConsciaApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements ConsciaApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public ConsciaApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements ConsciaApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public ConsciaApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements ConsciaApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public ConsciaApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements ConsciaApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public ConsciaApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements ConsciaApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public ConsciaApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements ConsciaApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public ConsciaApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends ConsciaApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends ConsciaApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends ConsciaApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends ConsciaApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(11).put(LazyClassKeyProvider.com_example_conscia_ui_auth_AuthViewModel, AuthViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_conscia_ui_onboarding_ChooseAppsViewModel, ChooseAppsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_conscia_ui_rules_CreateEditRuleViewModel, CreateEditRuleViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_conscia_ui_dashboard_DashboardViewModel, DashboardViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_conscia_ui_insights_InsightsViewModel, InsightsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_conscia_ui_settings_ManageIntentionsViewModel, ManageIntentionsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_conscia_ui_settings_ProfileViewModel, ProfileViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_conscia_ui_intention_SessionHistoryViewModel, SessionHistoryViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_conscia_ui_onboarding_StarterRulesViewModel, StarterRulesViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_conscia_ui_tracked_TrackedAppDetailViewModel, TrackedAppDetailViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_example_conscia_ui_tracked_TrackedAppsViewModel, TrackedAppsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectDataStore(instance, singletonCImpl.provideTrackedAppsDataStoreProvider.get());
      MainActivity_MembersInjector.injectRuleRepository(instance, singletonCImpl.ruleRepositoryProvider.get());
      return instance;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_example_conscia_ui_rules_CreateEditRuleViewModel = "com.example.conscia.ui.rules.CreateEditRuleViewModel";

      static String com_example_conscia_ui_insights_InsightsViewModel = "com.example.conscia.ui.insights.InsightsViewModel";

      static String com_example_conscia_ui_auth_AuthViewModel = "com.example.conscia.ui.auth.AuthViewModel";

      static String com_example_conscia_ui_settings_ManageIntentionsViewModel = "com.example.conscia.ui.settings.ManageIntentionsViewModel";

      static String com_example_conscia_ui_settings_ProfileViewModel = "com.example.conscia.ui.settings.ProfileViewModel";

      static String com_example_conscia_ui_intention_SessionHistoryViewModel = "com.example.conscia.ui.intention.SessionHistoryViewModel";

      static String com_example_conscia_ui_tracked_TrackedAppsViewModel = "com.example.conscia.ui.tracked.TrackedAppsViewModel";

      static String com_example_conscia_ui_tracked_TrackedAppDetailViewModel = "com.example.conscia.ui.tracked.TrackedAppDetailViewModel";

      static String com_example_conscia_ui_dashboard_DashboardViewModel = "com.example.conscia.ui.dashboard.DashboardViewModel";

      static String com_example_conscia_ui_onboarding_StarterRulesViewModel = "com.example.conscia.ui.onboarding.StarterRulesViewModel";

      static String com_example_conscia_ui_onboarding_ChooseAppsViewModel = "com.example.conscia.ui.onboarding.ChooseAppsViewModel";

      @KeepFieldType
      CreateEditRuleViewModel com_example_conscia_ui_rules_CreateEditRuleViewModel2;

      @KeepFieldType
      InsightsViewModel com_example_conscia_ui_insights_InsightsViewModel2;

      @KeepFieldType
      AuthViewModel com_example_conscia_ui_auth_AuthViewModel2;

      @KeepFieldType
      ManageIntentionsViewModel com_example_conscia_ui_settings_ManageIntentionsViewModel2;

      @KeepFieldType
      ProfileViewModel com_example_conscia_ui_settings_ProfileViewModel2;

      @KeepFieldType
      SessionHistoryViewModel com_example_conscia_ui_intention_SessionHistoryViewModel2;

      @KeepFieldType
      TrackedAppsViewModel com_example_conscia_ui_tracked_TrackedAppsViewModel2;

      @KeepFieldType
      TrackedAppDetailViewModel com_example_conscia_ui_tracked_TrackedAppDetailViewModel2;

      @KeepFieldType
      DashboardViewModel com_example_conscia_ui_dashboard_DashboardViewModel2;

      @KeepFieldType
      StarterRulesViewModel com_example_conscia_ui_onboarding_StarterRulesViewModel2;

      @KeepFieldType
      ChooseAppsViewModel com_example_conscia_ui_onboarding_ChooseAppsViewModel2;
    }
  }

  private static final class ViewModelCImpl extends ConsciaApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<ChooseAppsViewModel> chooseAppsViewModelProvider;

    private Provider<CreateEditRuleViewModel> createEditRuleViewModelProvider;

    private Provider<DashboardViewModel> dashboardViewModelProvider;

    private Provider<InsightsViewModel> insightsViewModelProvider;

    private Provider<ManageIntentionsViewModel> manageIntentionsViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private Provider<SessionHistoryViewModel> sessionHistoryViewModelProvider;

    private Provider<StarterRulesViewModel> starterRulesViewModelProvider;

    private Provider<TrackedAppDetailViewModel> trackedAppDetailViewModelProvider;

    private Provider<TrackedAppsViewModel> trackedAppsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private GetRuleByIdUseCase getRuleByIdUseCase() {
      return new GetRuleByIdUseCase(singletonCImpl.ruleRepositoryProvider.get());
    }

    private UpsertRuleUseCase upsertRuleUseCase() {
      return new UpsertRuleUseCase(singletonCImpl.ruleRepositoryProvider.get());
    }

    private DeleteRuleUseCase deleteRuleUseCase() {
      return new DeleteRuleUseCase(singletonCImpl.ruleRepositoryProvider.get());
    }

    private GetTodayUsageUseCase getTodayUsageUseCase() {
      return new GetTodayUsageUseCase(singletonCImpl.usageStatsRepositoryProvider.get());
    }

    private GetWeeklyUsageUseCase getWeeklyUsageUseCase() {
      return new GetWeeklyUsageUseCase(singletonCImpl.usageStatsRepositoryProvider.get());
    }

    private GetRulesUseCase getRulesUseCase() {
      return new GetRulesUseCase(singletonCImpl.ruleRepositoryProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.chooseAppsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.createEditRuleViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.dashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.insightsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.manageIntentionsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.sessionHistoryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.starterRulesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.trackedAppDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.trackedAppsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(11).put(LazyClassKeyProvider.com_example_conscia_ui_auth_AuthViewModel, ((Provider) authViewModelProvider)).put(LazyClassKeyProvider.com_example_conscia_ui_onboarding_ChooseAppsViewModel, ((Provider) chooseAppsViewModelProvider)).put(LazyClassKeyProvider.com_example_conscia_ui_rules_CreateEditRuleViewModel, ((Provider) createEditRuleViewModelProvider)).put(LazyClassKeyProvider.com_example_conscia_ui_dashboard_DashboardViewModel, ((Provider) dashboardViewModelProvider)).put(LazyClassKeyProvider.com_example_conscia_ui_insights_InsightsViewModel, ((Provider) insightsViewModelProvider)).put(LazyClassKeyProvider.com_example_conscia_ui_settings_ManageIntentionsViewModel, ((Provider) manageIntentionsViewModelProvider)).put(LazyClassKeyProvider.com_example_conscia_ui_settings_ProfileViewModel, ((Provider) profileViewModelProvider)).put(LazyClassKeyProvider.com_example_conscia_ui_intention_SessionHistoryViewModel, ((Provider) sessionHistoryViewModelProvider)).put(LazyClassKeyProvider.com_example_conscia_ui_onboarding_StarterRulesViewModel, ((Provider) starterRulesViewModelProvider)).put(LazyClassKeyProvider.com_example_conscia_ui_tracked_TrackedAppDetailViewModel, ((Provider) trackedAppDetailViewModelProvider)).put(LazyClassKeyProvider.com_example_conscia_ui_tracked_TrackedAppsViewModel, ((Provider) trackedAppsViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_example_conscia_ui_insights_InsightsViewModel = "com.example.conscia.ui.insights.InsightsViewModel";

      static String com_example_conscia_ui_settings_ProfileViewModel = "com.example.conscia.ui.settings.ProfileViewModel";

      static String com_example_conscia_ui_tracked_TrackedAppDetailViewModel = "com.example.conscia.ui.tracked.TrackedAppDetailViewModel";

      static String com_example_conscia_ui_settings_ManageIntentionsViewModel = "com.example.conscia.ui.settings.ManageIntentionsViewModel";

      static String com_example_conscia_ui_onboarding_ChooseAppsViewModel = "com.example.conscia.ui.onboarding.ChooseAppsViewModel";

      static String com_example_conscia_ui_onboarding_StarterRulesViewModel = "com.example.conscia.ui.onboarding.StarterRulesViewModel";

      static String com_example_conscia_ui_tracked_TrackedAppsViewModel = "com.example.conscia.ui.tracked.TrackedAppsViewModel";

      static String com_example_conscia_ui_rules_CreateEditRuleViewModel = "com.example.conscia.ui.rules.CreateEditRuleViewModel";

      static String com_example_conscia_ui_intention_SessionHistoryViewModel = "com.example.conscia.ui.intention.SessionHistoryViewModel";

      static String com_example_conscia_ui_auth_AuthViewModel = "com.example.conscia.ui.auth.AuthViewModel";

      static String com_example_conscia_ui_dashboard_DashboardViewModel = "com.example.conscia.ui.dashboard.DashboardViewModel";

      @KeepFieldType
      InsightsViewModel com_example_conscia_ui_insights_InsightsViewModel2;

      @KeepFieldType
      ProfileViewModel com_example_conscia_ui_settings_ProfileViewModel2;

      @KeepFieldType
      TrackedAppDetailViewModel com_example_conscia_ui_tracked_TrackedAppDetailViewModel2;

      @KeepFieldType
      ManageIntentionsViewModel com_example_conscia_ui_settings_ManageIntentionsViewModel2;

      @KeepFieldType
      ChooseAppsViewModel com_example_conscia_ui_onboarding_ChooseAppsViewModel2;

      @KeepFieldType
      StarterRulesViewModel com_example_conscia_ui_onboarding_StarterRulesViewModel2;

      @KeepFieldType
      TrackedAppsViewModel com_example_conscia_ui_tracked_TrackedAppsViewModel2;

      @KeepFieldType
      CreateEditRuleViewModel com_example_conscia_ui_rules_CreateEditRuleViewModel2;

      @KeepFieldType
      SessionHistoryViewModel com_example_conscia_ui_intention_SessionHistoryViewModel2;

      @KeepFieldType
      AuthViewModel com_example_conscia_ui_auth_AuthViewModel2;

      @KeepFieldType
      DashboardViewModel com_example_conscia_ui_dashboard_DashboardViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.example.conscia.ui.auth.AuthViewModel 
          return (T) new AuthViewModel(singletonCImpl.provideTrackedAppsDataStoreProvider.get(), singletonCImpl.deviceRegistrationRepositoryProvider.get(), singletonCImpl.provideConsciaApiServiceProvider.get(), singletonCImpl.ruleRepositoryProvider.get());

          case 1: // com.example.conscia.ui.onboarding.ChooseAppsViewModel 
          return (T) new ChooseAppsViewModel(singletonCImpl.appRepositoryProvider.get(), singletonCImpl.provideTrackedAppsDataStoreProvider.get());

          case 2: // com.example.conscia.ui.rules.CreateEditRuleViewModel 
          return (T) new CreateEditRuleViewModel(viewModelCImpl.getRuleByIdUseCase(), viewModelCImpl.upsertRuleUseCase(), viewModelCImpl.deleteRuleUseCase(), singletonCImpl.ruleRepositoryProvider.get(), singletonCImpl.provideConsciaApiServiceProvider.get());

          case 3: // com.example.conscia.ui.dashboard.DashboardViewModel 
          return (T) new DashboardViewModel(ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule), viewModelCImpl.getTodayUsageUseCase(), viewModelCImpl.getWeeklyUsageUseCase(), viewModelCImpl.getRulesUseCase(), new EvaluateTrackedAppsUsageUseCase(), singletonCImpl.ruleRepositoryProvider.get(), singletonCImpl.provideConsciaApiServiceProvider.get(), singletonCImpl.provideTrackedAppsDataStoreProvider.get());

          case 4: // com.example.conscia.ui.insights.InsightsViewModel 
          return (T) new InsightsViewModel(ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule), singletonCImpl.usageStatsRepositoryProvider.get(), singletonCImpl.ruleRepositoryProvider.get(), singletonCImpl.remoteUsageSyncRepositoryProvider.get());

          case 5: // com.example.conscia.ui.settings.ManageIntentionsViewModel 
          return (T) new ManageIntentionsViewModel(singletonCImpl.provideConsciaApiServiceProvider.get());

          case 6: // com.example.conscia.ui.settings.ProfileViewModel 
          return (T) new ProfileViewModel(singletonCImpl.provideConsciaApiServiceProvider.get());

          case 7: // com.example.conscia.ui.intention.SessionHistoryViewModel 
          return (T) new SessionHistoryViewModel(singletonCImpl.provideConsciaApiServiceProvider.get(), singletonCImpl.remoteUsageSyncRepositoryProvider.get(), singletonCImpl.provideTrackedAppsDataStoreProvider.get());

          case 8: // com.example.conscia.ui.onboarding.StarterRulesViewModel 
          return (T) new StarterRulesViewModel(singletonCImpl.appRepositoryProvider.get(), singletonCImpl.provideTrackedAppsDataStoreProvider.get(), singletonCImpl.ruleRepositoryProvider.get(), singletonCImpl.provideConsciaApiServiceProvider.get());

          case 9: // com.example.conscia.ui.tracked.TrackedAppDetailViewModel 
          return (T) new TrackedAppDetailViewModel(singletonCImpl.ruleRepositoryProvider.get(), viewModelCImpl.deleteRuleUseCase(), viewModelCImpl.getTodayUsageUseCase());

          case 10: // com.example.conscia.ui.tracked.TrackedAppsViewModel 
          return (T) new TrackedAppsViewModel(viewModelCImpl.getRulesUseCase(), viewModelCImpl.getTodayUsageUseCase(), new EvaluateTrackedAppsUsageUseCase());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends ConsciaApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends ConsciaApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectAccessibilityForegroundAppService(
        AccessibilityForegroundAppService accessibilityForegroundAppService) {
      injectAccessibilityForegroundAppService2(accessibilityForegroundAppService);
    }

    @CanIgnoreReturnValue
    private AccessibilityForegroundAppService injectAccessibilityForegroundAppService2(
        AccessibilityForegroundAppService instance) {
      AccessibilityForegroundAppService_MembersInjector.injectRuleRepository(instance, singletonCImpl.ruleRepositoryProvider.get());
      AccessibilityForegroundAppService_MembersInjector.injectUsageRepository(instance, singletonCImpl.usageStatsRepositoryProvider.get());
      AccessibilityForegroundAppService_MembersInjector.injectNotificationManager(instance, singletonCImpl.consciaNotificationManagerProvider.get());
      AccessibilityForegroundAppService_MembersInjector.injectWarningHistoryStore(instance, singletonCImpl.warningHistoryStoreProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends ConsciaApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AppDatabase> provideAppDatabaseProvider;

    private Provider<TrackedAppsDataStore> provideTrackedAppsDataStoreProvider;

    private Provider<Interceptor> provideAuthInterceptorProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<ConsciaApiService> provideConsciaApiServiceProvider;

    private Provider<RuleRepository> ruleRepositoryProvider;

    private Provider<UsageStatsRepository> usageStatsRepositoryProvider;

    private Provider<WarningHistoryStore> warningHistoryStoreProvider;

    private Provider<ConsciaNotificationManager> consciaNotificationManagerProvider;

    private Provider<WeeklySummaryStore> weeklySummaryStoreProvider;

    private Provider<WeeklySummaryManager> weeklySummaryManagerProvider;

    private Provider<RemoteUsageSyncRepository> remoteUsageSyncRepositoryProvider;

    private Provider<UsageLimitCheckWorker_AssistedFactory> usageLimitCheckWorker_AssistedFactoryProvider;

    private Provider<DeviceRegistrationRepository> deviceRegistrationRepositoryProvider;

    private Provider<AppRepository> appRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private RuleDao ruleDao() {
      return DatabaseModule_ProvideRuleDaoFactory.provideRuleDao(provideAppDatabaseProvider.get());
    }

    private CheckUsageLimitWarningsUseCase checkUsageLimitWarningsUseCase() {
      return new CheckUsageLimitWarningsUseCase(ruleRepositoryProvider.get(), usageStatsRepositoryProvider.get(), warningHistoryStoreProvider.get(), consciaNotificationManagerProvider.get(), new EvaluateTrackedAppsUsageUseCase());
    }

    private Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return Collections.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>singletonMap("com.example.conscia.worker.UsageLimitCheckWorker", ((Provider) usageLimitCheckWorker_AssistedFactoryProvider));
    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 2));
      this.provideTrackedAppsDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<TrackedAppsDataStore>(singletonCImpl, 7));
      this.provideAuthInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<Interceptor>(singletonCImpl, 6));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 5));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 4));
      this.provideConsciaApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<ConsciaApiService>(singletonCImpl, 3));
      this.ruleRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RuleRepository>(singletonCImpl, 1));
      this.usageStatsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<UsageStatsRepository>(singletonCImpl, 8));
      this.warningHistoryStoreProvider = DoubleCheck.provider(new SwitchingProvider<WarningHistoryStore>(singletonCImpl, 9));
      this.consciaNotificationManagerProvider = DoubleCheck.provider(new SwitchingProvider<ConsciaNotificationManager>(singletonCImpl, 10));
      this.weeklySummaryStoreProvider = DoubleCheck.provider(new SwitchingProvider<WeeklySummaryStore>(singletonCImpl, 12));
      this.weeklySummaryManagerProvider = DoubleCheck.provider(new SwitchingProvider<WeeklySummaryManager>(singletonCImpl, 11));
      this.remoteUsageSyncRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RemoteUsageSyncRepository>(singletonCImpl, 13));
      this.usageLimitCheckWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<UsageLimitCheckWorker_AssistedFactory>(singletonCImpl, 0));
      this.deviceRegistrationRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DeviceRegistrationRepository>(singletonCImpl, 14));
      this.appRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AppRepository>(singletonCImpl, 15));
    }

    @Override
    public void injectConsciaApplication(ConsciaApplication consciaApplication) {
      injectConsciaApplication2(consciaApplication);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private ConsciaApplication injectConsciaApplication2(ConsciaApplication instance) {
      ConsciaApplication_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.example.conscia.worker.UsageLimitCheckWorker_AssistedFactory 
          return (T) new UsageLimitCheckWorker_AssistedFactory() {
            @Override
            public UsageLimitCheckWorker create(Context context, WorkerParameters params) {
              return new UsageLimitCheckWorker(context, params, singletonCImpl.checkUsageLimitWarningsUseCase(), singletonCImpl.weeklySummaryManagerProvider.get(), singletonCImpl.remoteUsageSyncRepositoryProvider.get());
            }
          };

          case 1: // com.example.conscia.data.rule.RuleRepository 
          return (T) new RuleRepository(singletonCImpl.ruleDao(), singletonCImpl.provideConsciaApiServiceProvider.get(), singletonCImpl.provideTrackedAppsDataStoreProvider.get());

          case 2: // com.example.conscia.data.AppDatabase 
          return (T) DatabaseModule_ProvideAppDatabaseFactory.provideAppDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.example.conscia.data.remote.api.ConsciaApiService 
          return (T) NetworkModule_ProvideConsciaApiServiceFactory.provideConsciaApiService(singletonCImpl.provideRetrofitProvider.get());

          case 4: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get());

          case 5: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.provideAuthInterceptorProvider.get());

          case 6: // okhttp3.Interceptor 
          return (T) NetworkModule_ProvideAuthInterceptorFactory.provideAuthInterceptor(singletonCImpl.provideTrackedAppsDataStoreProvider.get());

          case 7: // com.example.conscia.data.TrackedAppsDataStore 
          return (T) DataStoreModule_ProvideTrackedAppsDataStoreFactory.provideTrackedAppsDataStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // com.example.conscia.data.usage.UsageStatsRepository 
          return (T) new UsageStatsRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 9: // com.example.conscia.data.warning.WarningHistoryStore 
          return (T) new WarningHistoryStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 10: // com.example.conscia.notification.ConsciaNotificationManager 
          return (T) new ConsciaNotificationManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 11: // com.example.conscia.data.weekly.WeeklySummaryManager 
          return (T) new WeeklySummaryManager(singletonCImpl.usageStatsRepositoryProvider.get(), singletonCImpl.weeklySummaryStoreProvider.get());

          case 12: // com.example.conscia.data.weekly.WeeklySummaryStore 
          return (T) new WeeklySummaryStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 13: // com.example.conscia.data.remote.RemoteUsageSyncRepository 
          return (T) new RemoteUsageSyncRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.usageStatsRepositoryProvider.get(), singletonCImpl.ruleRepositoryProvider.get(), singletonCImpl.provideTrackedAppsDataStoreProvider.get(), singletonCImpl.provideConsciaApiServiceProvider.get());

          case 14: // com.example.conscia.data.remote.DeviceRegistrationRepository 
          return (T) new DeviceRegistrationRepository(singletonCImpl.provideConsciaApiServiceProvider.get(), singletonCImpl.provideTrackedAppsDataStoreProvider.get());

          case 15: // com.example.conscia.data.AppRepository 
          return (T) new AppRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
