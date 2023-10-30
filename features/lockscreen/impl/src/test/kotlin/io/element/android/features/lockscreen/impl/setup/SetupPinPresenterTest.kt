/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.lockscreen.impl.setup

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.LockScreenConfig
import io.element.android.features.lockscreen.impl.fixtures.aLockScreenConfig
import io.element.android.features.lockscreen.impl.fixtures.aPinCodeManager
import io.element.android.features.lockscreen.impl.pin.DefaultPinCodeManagerCallback
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.pin.model.assertEmpty
import io.element.android.features.lockscreen.impl.pin.model.assertText
import io.element.android.features.lockscreen.impl.setup.pin.SetupPinEvents
import io.element.android.features.lockscreen.impl.setup.pin.SetupPinPresenter
import io.element.android.features.lockscreen.impl.setup.pin.validation.PinValidator
import io.element.android.features.lockscreen.impl.setup.pin.validation.SetupPinFailure
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SetupPinPresenterTest {

    private val blacklistedPin = "1234"
    private val halfCompletePin = "12"
    private val completePin = "1235"
    private val mismatchedPin = "1236"

    @Test
    fun `present - complete flow`() = runTest {
        val pinCodeCreated = CompletableDeferred<Unit>()
        val callback = object : DefaultPinCodeManagerCallback() {
            override fun onPinCodeCreated() {
                pinCodeCreated.complete(Unit)
            }
        }
        val presenter = createSetupPinPresenter(callback)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also { state ->
                state.choosePinEntry.assertEmpty()
                state.confirmPinEntry.assertEmpty()
                assertThat(state.setupPinFailure).isNull()
                assertThat(state.isConfirmationStep).isFalse()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(halfCompletePin))
            }
            awaitItem().also { state ->
                state.choosePinEntry.assertText(halfCompletePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.setupPinFailure).isNull()
                assertThat(state.isConfirmationStep).isFalse()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(blacklistedPin))
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(blacklistedPin)
                assertThat(state.setupPinFailure).isEqualTo(SetupPinFailure.PinBlacklisted)
                state.eventSink(SetupPinEvents.ClearFailure)
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertEmpty()
                assertThat(state.setupPinFailure).isNull()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(completePin))
            }
            consumeItemsUntilPredicate {
                it.isConfirmationStep
            }.last().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isTrue()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(mismatchedPin))
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertText(mismatchedPin)
                assertThat(state.setupPinFailure).isEqualTo(SetupPinFailure.PinsDontMatch)
                state.eventSink(SetupPinEvents.ClearFailure)
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertEmpty()
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isFalse()
                assertThat(state.setupPinFailure).isNull()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(completePin))
            }
            consumeItemsUntilPredicate {
                it.isConfirmationStep
            }.last().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isTrue()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(completePin))
            }
            awaitItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertText(completePin)
            }
            pinCodeCreated.await()
        }
    }

    private fun createSetupPinPresenter(
        callback: PinCodeManager.Callback,
        lockScreenConfig: LockScreenConfig = aLockScreenConfig(
            pinBlacklist = setOf(blacklistedPin)
        ),
    ): SetupPinPresenter {
        val pinCodeManager = aPinCodeManager()
        pinCodeManager.addCallback(callback)
        return SetupPinPresenter(
            lockScreenConfig = lockScreenConfig,
            pinValidator = PinValidator(lockScreenConfig),
            buildMeta = aBuildMeta(),
            pinCodeManager = pinCodeManager
        )
    }
}
