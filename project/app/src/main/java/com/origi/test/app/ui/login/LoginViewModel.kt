package com.origi.test.app.ui.login


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gaming.core.GameSDK
import com.gaming.core.extensions.setData
import com.origi.test.app.data.LoginRepository

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    val status = MutableLiveData<Status>(None)
    private val _status by this::status

    fun login(context: LoginActivity, chn: String, brd: String, domain: String) {
        context.setData("_chn", chn)
        context.setData("_brd", brd)
        context.setData("_domain", domain)
        GameSDK.start(context) {
            _status.value = Invalid
        }
    }
}


sealed class Status
object Invalid : Status()
object None : Status()
