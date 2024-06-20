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

package io.element.android.appnav.loggedin

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData

open class LoggedInStateProvider : PreviewParameterProvider<LoggedInState> {
    override val values: Sequence<LoggedInState>
        get() = sequenceOf(
            aLoggedInState(),
            aLoggedInState(showSyncSpinner = true),
            aLoggedInState(pusherRegistrationState = AsyncData.Failure(PusherRegistrationFailure.NoDistributorsAvailable())),
        )
}

fun aLoggedInState(
    showSyncSpinner: Boolean = false,
    pusherRegistrationState: AsyncData<Unit> = AsyncData.Uninitialized,
) = LoggedInState(
    showSyncSpinner = showSyncSpinner,
    pusherRegistrationState = pusherRegistrationState,
    ignoreRegistrationError = false,
    eventSink = {},
)
