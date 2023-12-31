package com.tyron.code.desktop.ui

import com.tyron.code.desktop.CodeAssistApplication
import com.tyron.code.desktop.cdi.mainModule
import javafx.application.Application
import org.koin.core.context.startKoin

fun main() {

    startKoin {
        modules(mainModule)
    }

    Application.launch(CodeAssistApplication::class.java)
}
