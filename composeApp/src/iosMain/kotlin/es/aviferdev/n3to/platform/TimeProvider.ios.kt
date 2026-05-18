package es.aviferdev.n3to.platform

import platform.Foundation.NSDate
import platform.Foundation.date
import platform.Foundation.timeIntervalSince1970

actual fun nowMillis(): Long = (NSDate.date().timeIntervalSince1970 * 1000).toLong()
