package com.example.capstone

import java.util.Date

class SensorAlert (
    val doorLock: Boolean,
    val gas: Boolean,
    val motion: Boolean,
    val temperature: Boolean,
    val timestamp: Date
)
