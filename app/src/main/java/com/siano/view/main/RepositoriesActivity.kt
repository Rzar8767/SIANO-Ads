package com.siano.view.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jacekmarchwicki.universaladapter.ViewHolderManager
import com.siano.R
import com.siano.TokenPreferences
import com.siano.base.BaseViewHolderManager
import com.siano.base.Rx2UniversalAdapter
import com.siano.dagger.annotations.DaggerAnnotation
import com.siano.dagger.module.BaseActivityModule
import com.siano.layoutmanager.MyLinearLayoutManager
import com.siano.utils.ErrorHandler
import com.siano.view.BaseActivity
import com.siano.view.login.LoginActivity
import com.siano.view.transaction.TransactionActivity
import dagger.Binds
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class RepositoriesActivity : BaseActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, RepositoriesActivity::class.java)
    }

    @Inject
    lateinit var tokenPreferences: TokenPreferences
    @Inject
    lateinit var presenter: RepositoriesPresenter

    private val subscription = CompositeDisposable()

    private val adapter = Rx2UniversalAdapter(
        listOf<ViewHolderManager>(
            BaseViewHolderManager(R.layout.item_repository, ::RepositoryViewHolder, RepositoryAdapterItem::class.java)
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (tokenPreferences.getToken().isEmpty()) {
            finish()
            startActivity(LoginActivity.newInstance(this))
        }

        setUpRecyclerView()

        subscription.addAll(
            presenter.itemsObservable
                .subscribe(adapter),
            presenter.errorObservable
                .subscribe(ErrorHandler.show(repository_main_view)),
            presenter.itemClick().subscribe { startActivity(TransactionActivity.newIntent(this)) }
        )
    }

    private fun setUpRecyclerView() {
        recyclerView.layoutManager = MyLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription.clear()
    }

    @dagger.Module(includes = [(BaseActivityModule::class)])
    abstract class Module {

        @Binds
        @DaggerAnnotation.ForActivity
        abstract fun provideActivity(activity: LoginActivity): Activity
    }
}
