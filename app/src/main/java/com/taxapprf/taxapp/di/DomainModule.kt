package com.taxapprf.taxapp.di

import com.taxapprf.data.AccountRepositoryImpl
import com.taxapprf.data.CurrencyRepositoryImpl
import com.taxapprf.data.UserRepositoryImpl
import com.taxapprf.data.ReportRepositoryImpl
import com.taxapprf.data.TransactionRepositoryImpl
import com.taxapprf.domain.account.SwitchAccountUseCase
import com.taxapprf.domain.currency.GetCurrencyRateTodayFromCBRUseCase
import com.taxapprf.domain.report.DeleteReportUseCase
import com.taxapprf.domain.report.GetReportsUseCase
import com.taxapprf.domain.report.SaveReportsFromExcelUseCase
import com.taxapprf.domain.transaction.DeleteTransactionUseCase
import com.taxapprf.domain.transaction.GetTransactionsUseCase
import com.taxapprf.domain.transaction.SaveTransactionUseCase
import com.taxapprf.domain.account.GetAccountsUseCase
import com.taxapprf.domain.excel.SendExcelReportUseCase
import com.taxapprf.domain.user.GetUserUseCase
import com.taxapprf.domain.user.IsSignInUseCase
import com.taxapprf.domain.user.SignInUseCase
import com.taxapprf.domain.user.SignOutUseCase
import com.taxapprf.domain.user.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object DomainModule {
    @Provides
    fun provideSignInUseCase(repositoryImpl: UserRepositoryImpl) =
        SignInUseCase(repositoryImpl)

    @Provides
    fun provideSignUpUseCase(repositoryImpl: UserRepositoryImpl) =
        SignUpUseCase(repositoryImpl)

    @Provides
    fun provideSignOutUseCase(repositoryImpl: UserRepositoryImpl) =
        SignOutUseCase(repositoryImpl)

    @Provides
    fun provideIsSignInUseCase(repositoryImpl: UserRepositoryImpl) =
        IsSignInUseCase(repositoryImpl)

    @Provides
    fun provideGetUserUseCase(repositoryImpl: UserRepositoryImpl) =
        GetUserUseCase(repositoryImpl)

    @Provides
    fun provideGetAccountsUseCase(repositoryImpl: AccountRepositoryImpl) =
        GetAccountsUseCase(repositoryImpl)

    @Provides
    fun provideChangeAccountUseCase(repositoryImpl: AccountRepositoryImpl) =
        SwitchAccountUseCase(repositoryImpl)

    @Provides
    fun provideGetTodayCBRRateUseCase(repositoryImpl: CurrencyRepositoryImpl) =
        GetCurrencyRateTodayFromCBRUseCase(repositoryImpl)

    @Provides
    fun provideGetTransactionsUseCase(repositoryImpl: TransactionRepositoryImpl) =
        GetTransactionsUseCase(repositoryImpl)

    @Provides
    fun provideSaveTransactionUseCase(repositoryImpl: TransactionRepositoryImpl) =
        SaveTransactionUseCase(repositoryImpl)

    @Provides
    fun provideDeleteTransactionUseCase(repositoryImpl: TransactionRepositoryImpl) =
        DeleteTransactionUseCase(repositoryImpl)

    @Provides
    fun provideGetTaxesUseCase(repositoryImpl: ReportRepositoryImpl) =
        GetReportsUseCase(repositoryImpl)

    @Provides
    fun provideSaveTaxesFromExcelUseCase(repositoryImpl: ReportRepositoryImpl) =
        SaveReportsFromExcelUseCase(repositoryImpl)

    @Provides
    fun provideDeleteTaxUseCase(repositoryImpl: ReportRepositoryImpl) =
        DeleteReportUseCase(repositoryImpl)

    @Provides
    fun provide(reportRepositoryImpl: ReportRepositoryImpl) =
        SendExcelReportUseCase(reportRepositoryImpl)

}