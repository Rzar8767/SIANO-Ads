package com.siano.view.landing.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.siano.R
import com.siano.base.BaseActivity
import com.siano.dagger.annotations.DaggerAnnotation
import com.siano.dagger.annotations.Scope
import com.siano.dagger.module.BaseActivityModule
import com.siano.view.budgets.BudgetsActivity
import dagger.Binds
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_landing.*
import kotlinx.android.synthetic.main.login_activity.*
import javax.inject.Inject
import javax.inject.Named

class LoginActivity : BaseActivity() {

    companion object {
        fun newInstance(context: Context) = Intent(context, LoginActivity::class.java)
    }

    @Inject
    lateinit var presenter: LoginPresenter

    private val subscription = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        subscription.addAll(
            username_edit_text.textChanges()
                .subscribe { username_edit_text.error = null },
            password_edit_text.textChanges()
                .subscribe { password_edit_text.error = null },
            landing_activity_login_button.clicks()
                .switchMapSingle { presenter.loginSingle() }
            presenter.successObservable
                .subscribe {
                    finish()
                    startActivity(BudgetsActivity.newIntent(this))
                }
        )
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


        @dagger.Module
        companion object {

            @JvmStatic
            @Provides
            @Scope.Activity
            @Named("SignInClickObservable")
            fun provideUserCredentials(activity: LoginActivity): Observable<Pair<String, String>> =
                activity.findViewById<Button>(R.id.login_button).clicks()
                    .map {
                        Pair(
                            activity.findViewById<EditText>(R.id.username_edit_text).text.toString(),
                            activity.findViewById<EditText>(R.id.password_edit_text).text.toString()
                        )
                    }
                    .filter { credentials ->
                        when {
                            credentials.first.isEmpty() -> {
                                activity.findViewById<TextInputLayout>(R.id.username_edit_text).error =
                                    "Cannot be empty"
                                false
                            }
                            credentials.second.isEmpty() -> {
                                activity.findViewById<TextInputLayout>(R.id.password_edit_text).error =
                                    "Cannot be empty"
                                false
                            }
                            else -> true
                        }
                    }.share()
        }
    }
}