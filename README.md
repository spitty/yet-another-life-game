# Yet Another Game of Life

[Living demo](https://game-of-life.maxlog.dev/).

This repository contains implementation of [Game of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life).

The aim was to create Kotlin application compiled to JavaScript.

Inpired by [this tweet](https://twitter.com/relizarov/status/946406735874584581) 
about using kotlin coroutines for JavaScript
which leads to [this example](https://kotlin.github.io/kotlinx.coroutines/example-frontend-js/index.html).

## How to build
`./gradlew clean bundle`

It complies all required js and bundle them here:
```
$ ls build/dist/
index.html     main.bundle.js
```

## References and  useful links
- https://stackoverflow.com/questions/55232286/idiomatic-kotlin2js-gradle-setup, https://github.com/eggeral/kotlin-single-js-file-lib
- https://kotlinlang.org/docs/tutorials/javascript/setting-up.html
- https://kotlinlang.org/docs/reference/javascript-dce.html
- https://github.com/Kotlin/kotlin-examples/tree/master/gradle/js-dce
- https://github.com/Kotlin/kotlin-full-stack-application-demo
- https://github.com/barlog-m/kotlin-js-webpack-example (on webpack)
