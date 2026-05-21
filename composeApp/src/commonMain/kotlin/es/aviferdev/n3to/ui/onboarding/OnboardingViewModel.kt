package es.aviferdev.n3to.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.usecase.onboarding.MarkOnboardingCompletedUseCase
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val markCompleted: MarkOnboardingCompletedUseCase
) : ViewModel() {
    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            markCompleted()
            onDone()
        }
    }
}
