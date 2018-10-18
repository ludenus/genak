plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.0-rc-131")

    // Apply the application to add support for building a CLI application
    application

}

repositories {
    google()
    mavenCentral()
    jcenter()
    maven { setUrl("http://dl.bintray.com/kotlin/kotlin-eap") }
}


val test by tasks.getting(Test::class) {
    val testNGOptions = closureOf<TestNGOptions> {
        suites("src/test/resources/testng.xml")
    }

    useTestNG(testNGOptions)
    testLogging.showStandardStreams = true
}

dependencies {
    // Use the Kotlin JDK 8 standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.0-rc-131")

//    implementation(kotlin("stdlib", "1.2.60"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.0-rc-131")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.30.2")

    // log
    implementation("org.slf4j:slf4j-api:1.7.21")
    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // config
    implementation("com.typesafe:config:1.3.3")

    // http client
    implementation("com.github.kittinunf.fuel:fuel:1.15.0")

    // http client
    implementation("org.asynchttpclient:async-http-client:2.5.4")

    // json object mapper
    implementation("com.fasterxml.jackson.core:jackson-core:2.9.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.5")

    // postgres
    implementation("org.postgresql:postgresql:42.2.5")

    // influx
    implementation("org.influxdb:influxdb-java:2.13")

    // Use TestNG framework, also requires calling test.useTestNG() below
    testImplementation("org.testng:testng:6.14.3")

    // http://www.unitils.org/tutorial-reflectionassert.html
    testImplementation("org.unitils:unitils-core:3.4.6")

}


application {
    // Define the main class for the application
    mainClassName = "genak.AppKt"
}

