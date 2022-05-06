# Tinkoff Merchant API

Kotlin implementation of [Tinkoff Merchant API](https://oplata.tinkoff.ru/develop/api/payments/).

## Installation
Kotlin DSL
```kotlin
// Append repository
repositories {
    mavenCentral()
}

// Append dependency
implementation("com.icerockdev.service:tinkoff-merchant-api:0.3.0")
```

## Usage
With [koin](https://github.com/InsertKoinIO/koin) DI or simple create an instances of following classes.
```kotlin
    single {
        TinkoffCredential(
            terminalKey = "<terminalKey>",
            secretKey = "<secretKey>"
        )
    }

    single {
        TinkoffUtils(get())
    }

    single {
        TinkoffClient(credential = get(), utils = get())
    }
```

## Notifications
If you are need to use [notifications](https://oplata.tinkoff.ru/develop/api/notifications/) map received data 
to `TinkoffPayload`, generate SHA-256 sign and compare with sign in request.
```kotlin
val payload = TinkoffPayload().apply {
    data["TerminalKey"] = TerminalKey
    data["ExpDate"] = ExpDate
}

val token = tinkoffUtils.generateToken(payload)
```
 
## Contributing
All development (both new features and bug fixes) is performed in the `develop` branch. This way `master` always contains the sources of the most recently released version. Please send PRs with bug fixes to the `develop` branch. Documentation fixes in the markdown files are an exception to this rule. They are updated directly in `master`.

The `develop` branch is pushed to `master` on release.

For more details on contributing please see the [contributing guide](CONTRIBUTING.md).

## License
        
    Copyright 2020 IceRock MAG Inc.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
