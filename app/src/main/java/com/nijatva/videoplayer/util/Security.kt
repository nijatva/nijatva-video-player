package com.nijatva.videoplayer.util

import java.security.MessageDigest

/** Hashes a PIN so the plain value is never stored. */
fun hashPin(pin: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(pin.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
