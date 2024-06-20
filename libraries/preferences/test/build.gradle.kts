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

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.preferences.test"

    dependencies {
        api(projects.libraries.preferences.api)
        implementation(projects.libraries.matrix.api)
        implementation(projects.tests.testutils)
        implementation(libs.coroutines.core)
        implementation(libs.androidx.datastore.preferences)
    }
}
