package es.aviferdev.n3to.platform

import platform.Foundation.NSDate

actual fun nowMillis(): Long = (NSDate.date().timeIntervalSince1970 * 1000).toLong()
