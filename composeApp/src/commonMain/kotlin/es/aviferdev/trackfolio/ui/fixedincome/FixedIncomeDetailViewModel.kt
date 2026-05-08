package es.aviferdev.trackfolio.ui.fixedincome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import es.aviferdev.trackfolio.domain.model.FixedIncomeEventType
import es.aviferdev.trackfolio.domain.model.FixedIncomeRow
import es.aviferdev.trackfolio.domain.portfolio.FixedIncomeCalculator
import es.aviferdev.trackfolio.domain.portfolio.MaturitySimulation
import es.aviferdev.trackfolio.domain.portfolio.ScheduledCoupon
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import es.aviferdev.trackfolio.domain.usecase.fixedincome.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FixedIncomeDetailUiState(
    val row: FixedIncomeRow? = null,
    val events: List<FixedIncomeEvent> = emptyList(),
    val couponSchedule: List<ScheduledCoupon> = emptyList(),
    val maturitySimulation: MaturitySimulation? = null,
    val showRegisterCouponSheet: Boolean = false,
    val showCloseSheet: Boolean = false,
    val showDeleteEventDialog: Boolean = false,
    val selectedEventForDelete: FixedIncomeEvent? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class FixedIncomeDetailViewModel(
    private val positionId: String,
    private val getPositionDetail: GetFixedIncomePositionDetailUseCase,
    private val getCouponSchedule: GetCouponScheduleUseCase,
    private val registerCoupon: RegisterCouponUseCase,
    private val closeFixedIncome: CloseFixedIncomeUseCase,
    private val deleteFixedIncomeEvent: DeleteFixedIncomeEventUseCase,
    private val updatePosition: UpdateFixedIncomePositionUseCase,
    private val archivePosition: ArchiveFixedIncomePositionUseCase,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _showRegisterCouponSheet = MutableStateFlow(false)
    private val _showCloseSheet = MutableStateFlow(false)
    private val _showDeleteEventDialog = MutableStateFlow(false)
    private val _selectedEventForDelete = MutableStateFlow<FixedIncomeEvent?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FixedIncomeDetailUiState> = combine(
        getPositionDetail.getPosition(positionId),
        getPositionDetail.getEvents(positionId),
        _showRegisterCouponSheet,
        _showCloseSheet,
        _showDeleteEventDialog,
        _selectedEventForDelete,
        _error
    ) { values ->
        val row = values[0] as FixedIncomeRow?
        @Suppress("UNCHECKED_CAST")
        val events = values[1] as List<FixedIncomeEvent>
        val showCoupon = values[2] as Boolean
        val showClose = values[3] as Boolean
        val showDelete = values[4] as Boolean
        val eventForDelete = values[5] as FixedIncomeEvent?
        val error = values[6] as String?

        val schedule = row?.let {
            getCouponSchedule(it.position)
        } ?: emptyList()

        val simulation = row?.let {
            FixedIncomeCalculator.simulateMaturity(it.position, events)
        }

        FixedIncomeDetailUiState(
            row = row,
            events = events,
            couponSchedule = schedule,
            maturitySimulation = simulation,
            showRegisterCouponSheet = showCoupon,
            showCloseSheet = showClose,
            showDeleteEventDialog = showDelete,
            selectedEventForDelete = eventForDelete,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FixedIncomeDetailUiState(isLoading = true)
    )

    fun showRegisterCouponSheet() { _showRegisterCouponSheet.value = true }
    fun hideRegisterCouponSheet() { _showRegisterCouponSheet.value = false }

    fun showCloseSheet() { _showCloseSheet.value = true }
    fun hideCloseSheet() { _showCloseSheet.value = false }

    fun showDeleteEventDialog(event: FixedIncomeEvent) {
        _selectedEventForDelete.value = event
        _showDeleteEventDialog.value = true
    }
    fun hideDeleteEventDialog() {
        _showDeleteEventDialog.value = false
        _selectedEventForDelete.value = null
    }

    fun registerCouponEvent(event: FixedIncomeEvent) {
        viewModelScope.launch {
            val position = uiState.value.row?.position
            if (position == null) {
                _error.value = "Posición no encontrada"
                return@launch
            }
            registerCoupon(event)
                .onSuccess {
                    _showRegisterCouponSheet.value = false
                }
                .onFailure {
                    _error.value = it.message
                }
        }
    }

    fun closePosition(closeType: es.aviferdev.trackfolio.domain.model.FixedIncomeCloseType, closeDate: Long, settlementEvent: FixedIncomeEvent) {
        viewModelScope.launch {
            closeFixedIncome(positionId, closeType, closeDate, settlementEvent)
                .onSuccess { _showCloseSheet.value = false }
                .onFailure { _error.value = it.message }
        }
    }

    fun deleteEvent(event: FixedIncomeEvent) {
        viewModelScope.launch {
            val linkedTxId = "fi_${event.id}"
            deleteFixedIncomeEvent(event.id, linkedTxId)
                .onSuccess { hideDeleteEventDialog() }
                .onFailure { _error.value = it.message }
        }
    }

    fun archivePosition() {
        viewModelScope.launch {
            archivePosition(positionId)
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError() { _error.value = null }
}