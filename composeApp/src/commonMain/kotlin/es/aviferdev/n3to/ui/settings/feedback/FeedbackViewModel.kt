package es.aviferdev.n3to.ui.settings.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.platform.FeedbackSender
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FeedbackCategory(val displayName: String) {
    NEW_FEATURE("Nueva funcionalidad"),
    IMPROVEMENT("Mejora de existente"),
    BUG("Reportar un error"),
    OTHER("Otro");
}

fun FeedbackCategory.toKey(): String = name.lowercase()

data class FeedbackUiState(
    val selectedCategory: FeedbackCategory = FeedbackCategory.NEW_FEATURE,
    val title: String = "",
    val description: String = "",
    val isSending: Boolean = false,
    val isSent: Boolean = false
)

sealed class FeedbackEvent {
    data object Success : FeedbackEvent()
    data class Error(val message: String) : FeedbackEvent()
}

class FeedbackViewModel(
    private val feedbackSender: FeedbackSender
) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FeedbackEvent>()
    val events: SharedFlow<FeedbackEvent> = _events.asSharedFlow()

    fun onCategorySelected(category: FeedbackCategory) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun onTitleChanged(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun onDescriptionChanged(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun onSendFeedback() {
        val current = _state.value
        if (current.title.isBlank() || current.description.isBlank()) {
            viewModelScope.launch {
                _events.emit(FeedbackEvent.Error("Por favor, completa el título y la descripción."))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            val result = feedbackSender.sendFeedback(
                category = current.selectedCategory.toKey(),
                title = current.title.trim(),
                description = current.description.trim()
            )
            result
                .onSuccess {
                    _state.update { it.copy(isSending = false, isSent = true) }
                    _events.emit(FeedbackEvent.Success)
                }
                .onFailure { error ->
                    _state.update { it.copy(isSending = false) }
                    _events.emit(
                        FeedbackEvent.Error(
                            error.message ?: "Error al enviar la sugerencia. Comprueba tu conexión."
                        )
                    )
                }
        }
    }

    fun resetForm() {
        _state.update {
            FeedbackUiState()
        }
    }
}
