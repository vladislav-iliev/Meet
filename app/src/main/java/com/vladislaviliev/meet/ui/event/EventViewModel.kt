package com.vladislaviliev.meet.ui.event

import androidx.lifecycle.ViewModel
import com.vladislaviliev.meet.network.repositories.event.EventRepository

internal class EventViewModel(val repo: EventRepository) : ViewModel()